function drawCharts(parentElement) {
    var flattenedData = flattenData(graphData);

    this.parentWidth = parentElement.clientWidth - 10;

    var timeStart = flattenedData[0].timeS;
    var timeEnd = flattenedData[flattenedData.length-1].timeS;
    this.avgTimeStep = (timeEnd-timeStart)/graphData[0].data.length;
    this.binSize = getBinSize(graphData);

    var ndx = crossfilter(flattenedData);

    var tempChart = drawChart(ndx, "tempChart", "tempC", "Temperature", "°C", 2);
    var humChart = drawChart(ndx, "humtyChart", "humPct", "Humidity", "%", 2);
    var baroChart = drawChart(ndx, "baroChart", "baroHpa", "Barometer", "hPa", 4);
    var overview = drawOverview(ndx, "overview", "tempC");

    //tempChart
    //    .rangeChart(overview)
    //    //.transitionDuration(1000)
    //;
    //humChart
    //    .rangeChart(overview)
    //    .transitionDuration(1000)
    //;
    //baroChart
    //    .rangeChart(overview)
    //    .transitionDuration(1000)
    //;


    //dc.EVENT_DELAY = 500;  // possibly speed up brush dragging with large data sets

    dc.renderAll();
    document.getElementById("stats").innerHTML = flattenedData.length + " samples per line "
        + Math.floor(this.binSize) + " samples per bin";
}

function flattenData(nestedData) {
    var flattenedData = [];
    this.maxFieldValue = [];
    this.minFieldValue = [];
    for (var sensorKey in nestedData) {
        if (!nestedData.hasOwnProperty(sensorKey)) continue;
        var sensorDataSet = nestedData[sensorKey];
        for (var sensorRowKey in sensorDataSet.data) {
            if (!sensorDataSet.data.hasOwnProperty(sensorRowKey)) continue;
            var sample = sensorDataSet.data[sensorRowKey];

            var row = {};
            row.timeS = sample.timeS;

            for (var propKey in sample) {
                if (!sample.hasOwnProperty(propKey)) continue;
                var propVal = sample[propKey];
                if (propVal != "timeS") {
                    row[propKey] = propVal;
                    this.maxFieldValue[propKey] = Math.max(this.maxFieldValue[propKey]||-1000 /*Number.NEGATIVE_INFINITY*/ , propVal);
                    this.minFieldValue[propKey] = Math.min(this.minFieldValue[propKey]||1000/*Number.POSITIVE_INFINITY*/, propVal);
                }
            }
            row.sensorName = sensorDataSet.metadata.sensorId;
            flattenedData.push(row);
        }
    }
    flattenedData.sort(function(a,b) {
        return a.timeS-b.timeS;
    });
    return flattenedData;
}

function drawChart(ndx, chartId, field, yAxisLabel, unit, precision) {

    var chartDiv = document.getElementById(chartId);
    var height = this.parentWidth / 5.0;

    var dateDim = createDimension(ndx, this.avgTimeStep, this.binSize);
//    var minDate = dateDim.bottom(1)[0].timeS;
//    var maxDate = dateDim.top(1)[0].timeS;

    var chart = dc.seriesChart(chartDiv);


    var group = dateDim.group().reduce(reduceFieldAdd(field), reduceFieldRemove(field), reduceFieldInitial());


    chart
        .width(this.parentWidth)
        .height(height)
        .margins({top: 20, right: 20, bottom: 30, left: 50})
        .mouseZoomable(false)
        .renderHorizontalGridLines(true)
        .renderVerticalGridLines(true)
        .legend(dc.legend().x(this.parentWidth-100).y(10).itemHeight(13).gap(5))
        .brushOn(false)
        .elasticX(true)
        .x(d3.time.scale())
        .yAxisLabel(yAxisLabel)
        .elasticY(false)
        .y(d3.scale.linear().domain([this.minFieldValue[field],this.maxFieldValue[field]]))
        .chart(function (c) {
            return dc.lineChart(c)/*.interpolate('basis')*/;
        })
        .dimension(dateDim)
        .group(group)
        .seriesAccessor(function (d) {
            return d.key[1];
        })
        .seriesSort(d3.ascending)
        .keyAccessor(function (d) {
            return +d.key[0];
        })
        .valueAccessor(function (d) {
            return getValue(d);
        })
        .title(function (d) {
            return d.key[1] + "\n"
                + d3.time.format('%A %I:%M:%S %p')(new Date(d.key[0])) + '\n'
                + d3.format('.' + precision + 'f')(getValue(d)) + " " + unit;
        })
//        .on('postRedraw', function (chart) {
//            adjustDimension(chart);
//        })
    ;


    return chart;
}

function rangesEqual(range1, range2) {
    if (!range1 && !range2) {
        return true;
    } else if (!range1 || !range2) {
        return false;
    } else if (range1.length ==0 && range2.length == 0) {
        return true;
    } else if (range1[0].valueOf() === range2[0].valueOf() &&
        range1[1].valueOf() === range2[1].valueOf()) {
        return true;
    }
    return false;
}

function drawOverview(ndx, chartId, field) {
    var chartDiv = document.getElementById(chartId);
    var height = Math.max(80, this.parentWidth / 15.0);

    var dateDim = createDimension(ndx, this.avgTimeStep, this.binSize);

    var overview = dc.seriesChart(chartDiv);

    var group = dateDim.group().reduce(reduceFieldAdd(field), reduceFieldRemove(field), reduceFieldInitial());

    overview
        .width(this.parentWidth)
        .height(height)
        .chart(function(c, unused1, unused2, i) {
            var cht = dc.lineChart(c);
            if (i==0)
                cht.on("filtered", function(chart) {
                    if (!cht.filter()) {
                        dc.events.trigger(function() {
                            overview.focusChart().x().domain(overview.focusChart().xOriginalDomain());
                            overview.focusChart().redraw();
                        });
                    } else if (!rangesEqual(chart.filter(), overview.focusChart().filter())) {
                        dc.events.trigger(function() {
                            overview.focusChart().focus(cht.filter());
                        })
                    }
                });
            return cht;
        })
        .margins({top: 20, right: 20, bottom: 30, left: 50})
        .mouseZoomable(true)
        .brushOn(true)
        .elasticX(true)
        .x(d3.time.scale())
        .elasticY(false)
        .y(d3.scale.linear().domain([this.minFieldValue[field],this.maxFieldValue[field]]))
        .dimension(dateDim)
        .group(group)
        .seriesAccessor(function(d) {
            return d.key[1];
        })
        .seriesSort(d3.ascending)
        .keyAccessor(function(d) {
            return +d.key[0];
        })
        .valueAccessor(function(d) {
            return getValue(d);
        })
    ;
    overview.yAxis().ticks(0);

    return overview;
}

function getValue(d) {
    var vals =  d.value[d.key[1]];
    if (this.reducerType == "min") return vals.min;
    if (this.reducerType == "max") return vals.max;
    return vals.average;
}

function reduceFieldAdd(field) {
    return function(p,v) {
        sensorName = v.sensorName;
        var value = Number(v[field]);
        //if (!p[sensorName]) p[sensorName] = {count:0, sum:0, average:0, max:Number.NEGATIVE_INFINITY, min:Number.POSITIVE_INFINITY};
        if (!p[sensorName]) p[sensorName] = {count:0, sum:0, average:0, max:-1000, min:1000};
        if (!value || isNaN(value)) value = p[sensorName].average || 0;
        p[sensorName].count++;
        p[sensorName].sum += value;
        p[sensorName].average = (p[sensorName].count === 0)? 0 : p[sensorName].sum/ p[sensorName].count;
        p[sensorName].max = Math.max(p[sensorName].max, value);
        p[sensorName].min = Math.min(p[sensorName].min, value);
        return p;
    }
}

function reduceFieldRemove(field) {
    return function(p,v) {
        sensorName = v.sensorName;
        var value = Number(v[field]);
        //if (!p[sensorName]) p[sensorName] = {count:0, sum:0, average:0, max:Number.NEGATIVE_INFINITY, min:Number.POSITIVE_INFINITY};
        if (!p[sensorName]) p[sensorName] = {count:0, sum:0, average:0, max:-1000, min:1000};
        if (!value || isNaN(value)) value = p[sensorName].average || 0;
        p[sensorName].count--;
        p[sensorName].sum -= value;
        p[sensorName].average = (p[sensorName].count === 0)? 0 : p[sensorName].sum/ p[sensorName].count;
        p[sensorName].max = Math.max(p[sensorName].max, value);
        p[sensorName].min = Math.min(p[sensorName].min, value);
        return p;
    }
}
function reduceFieldInitial() {
    return  function() {return {};};
}
function reducerGroupChange(domElement) {
    this.reducerType = domElement.defaultValue;
    dc.renderAll();
}

function getBinSize(graphData) {
    return Math.max(1, graphData[0].data.length * 2.0 / this.parentWidth);
}


function createDimension(ndx, avgTimeStep, binSize) {
    var binningMultiplier = binSize * avgTimeStep;
    return ndx.dimension(function(d) {
        return [(Math.floor(d.timeS / binningMultiplier))*binningMultiplier*1000, d.sensorName];
    });
}

function adjustDimension(chart) {
    var startTime = chart.xAxis().scale().domain().values().next().value.getTime();
    var endTime   = chart.xAxis().scale().domain().values().next().value.getTime();

    //chart.dimension = createDimension(endTime-startTime, getBinSize(startTime, endTime));

}
