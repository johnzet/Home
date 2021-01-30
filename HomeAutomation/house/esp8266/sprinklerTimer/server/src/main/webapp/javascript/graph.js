function drawChart() {

    var ndx = crossfilter(graphData[0].data);
    var dateDim = ndx.dimension(function(d) {return d.timeS*1000;});
    var minDate = dateDim.bottom(1)[0].timeS*1000;
    var maxDate = dateDim.top(1)[0].timeS*1000;
    var tempCDim = ndx.dimension(function(d) {return d.tempC;});
    var minTemp = tempCDim.bottom(1)[0].tempC;
    var maxTemp = tempCDim.top(1)[0].tempC;
    var tempCGroup = dateDim.group().reduceSum(function(d) {return d.tempC;});
    var humPctDim = ndx.dimension(function(d) {return d.humPct;});
    var minHum = humPctDim.bottom(1)[0].humPct;
    var maxHum = humPctDim.top(1)[0].humPct;
    var humGroup = dateDim.group().reduceSum(function(d) {return d.humPct;});
    
    var chart = dc.compositeChart('#chart');

    chart
        .width(990)
        .height(200)
        .transitionDuration(0)
        .margins({top: 30, right: 50, bottom: 25, left: 40})
        .mouseZoomable(true)
        .elasticY(false)
        .renderHorizontalGridLines(true)
        .legend(dc.legend().x(800).y(10).itemHeight(13).gap(5))
        .brushOn(false)
        .x(d3.time.scale().domain([minDate,maxDate]))
        .yAxisLabel("Temperature")
        .y(d3.scale.linear().domain([minTemp-2,maxTemp+2]))
        .rightYAxisLabel("Humidity")
        .rightY(d3.scale.linear().domain([minHum-5,maxHum+5]))
        .title(function (d) {
           return sensorName + '\n' + d3.time.format('%A %I:%M:%S %p')(new Date(d.key)) + '\n' + d3.format('.2f')(d.value); // degC = \xB0C
        })
        .dimension(dateDim)
        .compose([
            dc.lineChart(chart)
                .colors("red")
                .group(tempCGroup, sensorName + " Temperature")
            ,
            dc.lineChart(chart)
                .useRightYAxis(true)
                .colors("blue")
                .group(humGroup, sensorName + " Humidity")
        ]);
    chart.render();

    document.getElementById("stats").innerHTML = graphData[0].data.length + " data samples";
}
