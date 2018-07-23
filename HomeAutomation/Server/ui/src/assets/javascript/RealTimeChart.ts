import {Injectable} from '@angular/core';
import { RealTimeGraphService } from 'assets/javascript/RealTimeGraphController';

let d3 = require("assets/javascript/d3");

@Injectable()
export class RealTimeChart {

    private rtgService: RealTimeGraphService;
    private seriesCount: number = 0;
    private labelTable: string[];
    private domNode: HTMLElement;
    private totalDataPointCount: number = 0;
    private dataSet: any;
    private sensorIdMap: number[] = null;
    private isLogScale: boolean;
    private svgContainer: HTMLElement;
    private mostRecentDataDate: number;
    private brushing: boolean = false;
    private updateNeeded: boolean = false;
    // private brushedNeeded: boolean = false;
    private minYValue: number[] = null;
    private maxYValue: number[] = null;
    private width: number = 0;
    private width2: number = 0;
    private height: number = 0;
    private height2: number = 0;
    private x: any;
    private x2: any;
    private y: any;
    private y2: any;

    private aperture: number;
    private xAxis: any;
    private xAxis2: any;
    private yAxisL: any;
    private yAxisR: any;
    private yAxis2: any;
    private brush: any;
    private svg: any;
    private clipRect: any;
    private mousePointer: any;
    private focus: any;
    private context: any;
    private hoverDataPoints: any;
    private legend: any;
    private color: any;
    private animationDelay: number = 1000;

    constructor() {
        this.isLogScale = false;
    }

    init(rtgService: RealTimeGraphService, domNode: HTMLElement) {
        this.rtgService = rtgService;
        this.domNode = domNode;
        this.domNode.classList.add("RealTimeChart");
        this.svgContainer = domNode;
    }

    loadThenRender() {
        let that = this;
        let dataSet;
        this.rtgService.load()
            .subscribe(
                next => dataSet = next,
                err => that.rtgService.handleError,
                function() {
                    // load complete
                    that.handleData(dataSet);
                    that.render();
                    that.lineSetChanged();
                    that.handleResize();
                    that.update();
                    that.registerAdditionalHandlers();
                }
            );
    }

    handleData(dataModel: any) {
        let that = this;
        let metaData = {};
        this.dataSet = [];
        if (dataModel) {
            let metaData = dataModel.metaData;
            let mixedData = dataModel.mixedData;
            if (metaData) this.initSensorIdMap(metaData);
            if (mixedData) mixedData.forEach(function(lineData) {

                that.setData(
                    that._getIndexFromSensorId(lineData.sensorId),
                    lineData.data);
            });
        }
    }

    initSensorIdMap(metaData: any) {
        let that = this;
        this.sensorIdMap = [];
        this.seriesCount = 0;
        this.labelTable = [];
        let index = 0;
        metaData.forEach(function(sensorHost: any) {
            sensorHost.sensors.forEach(function(sensor:any) {
                that.labelTable[index] = sensorHost.location + " " + sensor.shortName;
                that.sensorIdMap[sensor.id] = index++;
                that.seriesCount++;
            });
        });
    }

    setData(index: number, sensorData: number[]) {
        let that = this;
        this.dataSet[index] = [];
        if (sensorData) sensorData.forEach(function(dataPoint){
            let point: any = {};
            point.value = dataPoint[1];
            point.date = dataPoint[0];
            that.mostRecentDataDate = Math.max(that.mostRecentDataDate, point.date);
            that.dataSet[index].push(point);
            that._accumulateMinAndMaxValues(index, point.value);
        });

        // if (this.brushing) {
        //     this.updateNeeded = true;
        // } else {
        //     this.update();
        // }
    }

    registerAdditionalHandlers() {
        let that = this;
        this.svg.selectAll("rect.background")
            .on("onkeypress", function() {console.log("that.rtgService.reload");})
        ;
        this.svg.selectAll("rect.extent")
            .on("click", function() {let ext = that.brush.extent(); that.rtgService.reload(ext[0], ext[1])})
        ;
    }

    _getIndexFromSensorId(sensorId: number) {
        return this.sensorIdMap[sensorId];
    }

    _accumulateMinAndMaxValues(index, value) {
        if (!this.minYValue) this.minYValue = [];
        if (!this.maxYValue) this.maxYValue = [];
        if (!this.minYValue[index]) this.minYValue[index] = this._getMinY();
        if (!this.maxYValue[index]) this.maxYValue[index] = this._getMinY();
        this.minYValue[index] = Math.min(this.minYValue[index], value);
        this.maxYValue[index] = Math.max(this.maxYValue[index], value);
    }

    _getMaxValue() {
        let max = this._getMinY();
        for (let index = 0; index < this.seriesCount; index++) {
            if (!this.maxYValue[index]) this.maxYValue[index] = max;
            max = Math.max(max, this.maxYValue[index]);
        }
        return max;
    }

    _getMinValue() {
        let min = this._getMinY();
        for (let index = 0; index < this.seriesCount; index++) {
            if (!this.minYValue[index]) this.minYValue[index] = min;
            min = Math.min(min, this.minYValue[index]);
        }
        return min;
    }

    _getMinY() {
        return (this.isLogScale ? 0.1 : 0);
    }

    _getBinSize(narrowedDataSet, isFocusChart) {
        let binSize = 1;
        if (!narrowedDataSet) return binSize;

        let count = this._getTotalDataPoints(narrowedDataSet);
        if (!isFocusChart) this.totalDataPointCount = count;

        binSize = Math.max(1, Math.floor(count / this.seriesCount / (this.width)));
        if (!isFocusChart) this._showTotalDataPointCount(count);
        return binSize;
    }

    _getTotalDataPoints(narrowedDataSet) {
        if (!narrowedDataSet) return 0;
        let count = this.seriesCount * this.dataSet[0].length;

        return Math.max(1, count);
    }

    // setLogScale(isLogScale) {
    //     this.isLogScale = isLogScale;
    //     this.handleResize(null);
    //     this.update();
    // }

    render() {
        let that = this;

        this.aperture = 10;

        this.xAxis = d3.svg.axis()
            .orient("bottom");

        this.xAxis2 = d3.svg.axis()
            .orient("bottom");

        this.yAxisL = d3.svg.axis()
            .ticks(15, ",.1s")
            .tickSize(6, 0)
            .orient("left");

        this.yAxisR = d3.svg.axis()
            .ticks(15, ",.1s")
            .tickSize(6, 0)
            .orient("right")
        ;

        this.yAxis2 = d3.svg.axis()
            .tickFormat("")
            .innerTickSize(0)
            .outerTickSize(0)
            .orient("left");

        this.brush = d3.svg.brush()
            .on("brushstart", function () {
                that.brushStart.call(that);
            })
            .on("brush", function () {
                that.brushed.call(that);
            })
            .on("brushend", function () {
                that.brushEnd.call(that);
            })
        ;

        let svg = d3.select(this.svgContainer).append("svg")
            .attr("width", 500)
            .attr("height", 500);

        this.svg = svg;

        this.clipRect = svg.append("defs").append("clipPath")
            .attr("id", "clip")
            .append("rect")
        ;

        let focus = svg.append("g")
            .attr("class", "focus");

        focus
            .append("rect")
            .attr("class", "eventTarget")
            .attr("fill", "none")
            .attr('pointer-events', 'all')
            .on("mousemove", function () {
                that.chartMousemove.call(that, this);
            });

        this.mousePointer = focus.append("circle")
            .attr("stroke", "#ccc")
            .attr("fill", "none")
            .attr("visibility", "hidden")
            .attr("pointer-events", "none")
            .attr("r", this.aperture);

        this.focus = focus;

        let context = svg.append("g")
                .attr("class", "context")
            ;

        context.append("g")
            .attr("class", "x brush")
            .selectAll("rect")
            .attr("y", 0)
        ;

        this.context = context;

        this.hoverDataPoints = svg
            .append("g")
            .attr("class", "hoverData")
        ;

        let toggleHoverDataPoints = this.hoverDataPoints
            .append("g")
            .classed("hoverPointsToggle", true)
            .classed("hoverPointsOff", true);

        toggleHoverDataPoints
            .append("rect")
            .attr("width", 12)
            .attr("height", 12)
            .attr("stroke", "#999")
            .attr("fill", "white")
            .attr("stroke-width", 1)
            .attr("x", 0)
            .attr("y", -12)
            .on("click", function () {
                that.removeHoverPoints.call(that);
            });

        toggleHoverDataPoints
            .append("line")
            .attr("x1", "12")
            .attr("y1", "0")
            .attr("x2", "0")
            .attr("y2", "-12")
            .attr("stroke", "#999")
            .attr("stroke-width", 1)
            .on("click", function () {
                that.removeHoverPoints.call(that);
            });

        this.hoverDataPoints
            .append("text")
            .attr("class", "hoverTime")
            .attr("x", 20)
            .attr("y", 0)
            .text("");

        this.hoverDataPoints
            .append("g")
            .attr("class", "hoverSeries")
            .attr("x", 100)
            .attr("y", 0)
        ;

        this.legend = svg.append("g")
            .attr("class", "legend");

        focus.append("g")
            .attr("class", "x axis")
        ;

        focus.append("g")
            .attr("class", "y axis yAxisL")
        ;

        focus.append("g")
            .attr("class", "y axis yAxisR")
        ;

        context.append("g")
            .attr("class", "y axis yAxis2");

        context.append("g")
            .attr("class", "x2 x axis");

        this.color = d3.scale.category20()
            .domain([0, this.seriesCount]);

        let lowerButtonPane = svg.append("g")
            .attr("class", "lowerButtonPane")
            .attr("fill", "#f00")
        ;


        lowerButtonPane.append("rect")
             .attr("class", "button")
            .attr("fill", "#f00")
            .attr("stroke", "#000")
            .attr("width", 100)
            .attr("height", 20)
            // .text("All Time")
        ;
    }

    lineSetChanged() {
        let that = this;


        let labels = this.getLabels();
        let legendItem = this.legend.selectAll('.series')
            .data(labels, function (d, i) {
                return i;
            })
            .enter().append("g")
            .attr("class", "series legend")
            .on("mouseover", function (d) {
                that.mouseover(d.label);
            })
            .on("touchstart", function (d) {
                that.mouseover(d.label);
            })
            .on("mouseout", function () {
                that.mouseout();
            })
            .on("touchend", function () {
                that.mouseout();
            });

        legendItem.append("rect")
            .attr("width", 13)
            .attr("height", 13)
            .attr("fill", function (d, i) {
                    return that.color(i);
                }
            );

        legendItem.append("text")
            .attr("class", "legendItemText")
            .attr("x", 15)
            .attr("y", 10.5)
            .text(function (d) {
                return d;
            });

        this.legend.selectAll('.series')
            .data(labels, function (d,i) {
                return i;
            })
            .exit()
            .remove();
    }

    removeHoverPoints() {
        this.svg.selectAll(".hoverSeriesItem")
            .remove();
        this.svg.select(".hoverTime").text("");
        this.svg.select(".hoverPointsToggle")
            .classed("hoverPointsOff", true);
        this.svg.selectAll(".legendItemText")
            .attr("fill", "#000")
    }

    handleResize(size?: any) {
        if (!this.svg) return;  // resize() was called before render()
        let svg_main_width = this.domNode.clientWidth || 100;
        let svg_main_height = this.domNode.clientHeight || 100;
        if (size) {
            svg_main_width = size.w;
            svg_main_height = size.h;
        }

        // Edit this to change the context to focus proportions
        let context_scale = 1 / 8;

        let leftMargin = 60;
        let rightMargin = 50;
        let topMargin = 10;
        let bottomMargin = 60;
        let chartGap = 80;

        let legendMargin = {
            top: topMargin,
            left: leftMargin,
            right: svg_main_width - rightMargin,
            bottom: svg_main_height - (topMargin) - 10
        };

        let that = this;
        let labels = this.getLabels();
        if (!labels) return;

        let maxLabelWidth = this._getWidestLabelWidth(labels);
        this.legend.selectAll('.series.legend')
            .data(labels)
            .attr("transform", function (d, i) {
                let availableWidth = legendMargin.right - legendMargin.left;
                let legendItemWidth = 18 + maxLabelWidth + 5;
                let itemsPerRow = Math.max(1, Math.floor(availableWidth / legendItemWidth));
                let index = (i % itemsPerRow);
                let x = index * legendItemWidth;
                let y = 20 * Math.floor(i / itemsPerRow);
                return "translate(" + x + ", " + y + ")";
            });
        let legendBB = this.legend.node().getBBox();
        let legendHeight = legendBB.height;
        legendMargin.bottom -= legendHeight;


        let combined_render_height = legendMargin.bottom - bottomMargin - chartGap;
        let contextHeight = context_scale * combined_render_height;
        let focusHeight = combined_render_height - contextHeight;
        let margin: any = {
            top: svg_main_height - legendHeight - legendMargin.bottom,
            right: 80,
            bottom: bottomMargin + contextHeight
        };
        let margin2: any = {top: topMargin + legendHeight + focusHeight + chartGap, bottom: bottomMargin, left: 60};
        let combined_render_width = svg_main_width - margin.right + 10 - margin2.left * 2 - 10;

        margin2.right = svg_main_width -
            Math.round(combined_render_width * (context_scale)) -
            margin2.left;

        margin.left = leftMargin;

        let width = svg_main_width - margin.left - margin.right;
        let width2 = width;
        let height = focusHeight;
        let height2 = contextHeight;

        this.width = width;
        this.width2 = width2;
        this.height = height;
        this.height2 = height2;

        this.x = d3.time.scale().range([0, this.width]);
        this.x2 = d3.time.scale().range([0, this.width2]);

        this.y = (this.isLogScale ?
            d3.scale.log().base(10).clamp(true).range([this.height, 0.1]) :
            d3.scale.linear().range([this.height, 0]));
        this.y2 = (this.isLogScale ?
            d3.scale.log().base(10).clamp(true).range([this.height2, 0.1]) :
            d3.scale.linear().range([this.height2, 0]));

        this.xAxis.scale(this.x);
        this.xAxis2.scale(this.x2);
        this.yAxisL.scale(this.y);
        this.yAxisR.scale(this.y);
        this.yAxis2.scale(this.y2);

        this.brush.x(that.x2);

        this.context.select(".x.brush")
            .call(that.brush)
        ;

        this.svg
            .attr("width", svg_main_width)
            .attr("height", svg_main_height);

        let ieCorrection = (this._isIE() ? 19 : 0);
        this.clipRect
            .attr("width", width)
            .attr("height", height + 2 + ieCorrection);  // +2 so that lines appear on top of the x-axis

        this.focus.attr("transform", "translate(" + margin.left + "," + (margin.top + legendHeight) + ")");

        this.context.attr("transform", "translate(" + margin2.left + "," + margin2.top + ")");

        this.context
            .selectAll("rect")
            .attr("height", height2);


        this.hoverDataPoints.attr("transform", "translate(" + margin.left + "," + (margin2.top - 15) + ")");

        this.legend.attr("transform", "translate(" + legendMargin.left + "," + legendMargin.top + ")");


        this.focus.select(".x.axis")
            .attr("transform", "translate(0," + height + ")")
            .call(this.xAxis);

        this.focus.select(".y.axis.yAxisL")
            .call(this.yAxisL);

        this.focus.select(".y.axis.yAxisR")
            .attr("transform", "translate(" + this.width + ", 0)")
            .call(this.yAxisR);
        this.focus.select(".eventTarget")
            .attr("width", this.width)
            .attr("height", this.height);

        this.context.select(".y.axis.yAxis2")
            .call(this.yAxis2);

        this.context.select(".x2.x.axis")
            .attr("transform", "translate(0," + height2 + ")")
            .call(this.xAxis2);

        this.clipMouseOvers();

        // this.svg.selectAll(".lowerButtonPane button")
        //     .attr("fill", "#f00")
        //     .attr("width", 100)
        //     .attr("height", 20)
        // ;

        this.svg.selectAll(".lowerButtonPane")
            .attr("transform", "translate(" + margin2.left + ", " + ( 50) + ")")
        ;
    }

    resize(size) {
        if (!size || size.w == 0 || size.h == 0) return;
        if (this.brush) this.brush.clear();
        this.handleResize(size);
        this.update();
    }

    mouseover(a) {
        let labels = this.getLabels();
        if (!this.dataSet || !labels) return;
        function isMatch(d) {
            return a == (labels[d.key] as any).label;
        }

        // class lines to highlight and fadeout selected series
        this.svg.selectAll('path.line')
            .classed("highlight", function (d) {
                return isMatch(d);
            })
            .classed("fadeout", function (d) {
                return !isMatch(d);
            });
    }

    // remove highlighting classes
    mouseout() {
        this.svg.selectAll('path.line')
            .classed("highlight", false)
            .classed("fadeout", false);
    }

    hoverSeriesMouseover() {
console.log("hover mouse over");
    }

    hoverSeriesMouseout() {
console.log("hover mouse out");
    }

    getLabels() {
        return this.labelTable;
    }

    brushStart() {
        this.brushing = true;
    }

    brushEnd() {
        this.brushing = false;
        if (this.updateNeeded) {
            this.updateNeeded = false;
            this.update();
        }
        // if (this.() {
        //     this.brushedNeeded = false;
        //     if (this._isBigData()) {
        //         this._brushed();
        //     }
        // }
    }

    brushed() {
        this.mousePointer
            .attr("visibility", "hidden");

        if (this.brush.empty()) {
            this.x.domain(this.x2.domain());
        } else {
            this.x.domain(this.brush.extent());
        }
        // this.brushedNeeded = true;

        if (!this._isBigData()) {
            this._brushed();
        }
    }

    _brushed() {

        let that = this;
        this.focus.select(".x.axis")
            .call(that.xAxis);


        let mappedDataSet = this._getMappedDataSet((this.brush.empty() ? null : this.brush.extent()), true);
        this.focus.selectAll("path.line")
            .data(mappedDataSet, this._getKey)
            .attr("d", function (d) {
            return that.focusLine(d.data, that._getMinValue());
        });

    }

    _getKey(d) {
        return d.key;
    }

    _isBigData() {
        if (!this.totalDataPointCount) return false;
        let widthThreshold = 2000;
        let pointsPerLine = this.dataSet[0].length;
        return (this.width > widthThreshold && pointsPerLine > widthThreshold);
    }

    focusLine(data, minYValue) {
        return this.eitherLine(data, minYValue, true);
    }

    contextLine(data, minYValue) {
        return this.eitherLine(data, minYValue, false);
    }

    eitherLine(data, minYValue, isFocusLine) {

        function isValidPoint(point) {
            return typeof point.value == 'number' && point.date;
        }

        let path = "";
        let isFirstPoint = true;

        let x = (isFocusLine ? this.x : this.x2);
        let y = (isFocusLine ? this.y : this.y2);

        let veryFirstPoint;
        if (data && data.length >= 1) data.forEach(function (point, index) {

            if (isValidPoint(point)) {
                if (isFirstPoint && ((index == data.length - 1) || !isValidPoint(data[index + 1]))) {
                    // is a lonely point

                    path += "M";
                    path += x(point.date);
                    path += ",";
                    path += y(point.value == 0 ? minYValue / 10.0 : point.value);
                    path += "a 5,5 0 1 0 0,10";
                    path += "a 5,5 0 1 0 0,-10";
                    path += "m 0,2.5";
                    path += "a 2.5,2.5 0 1 0 0,5";
                    path += "a 2.5,2.5 0 1 0 0,-5";

                } else {
                    path += (isFirstPoint ? "M" : "L");
                    path += x(point.date);
                    path += ",";
                    path += y(point.value == 0 ? minYValue / 10.0 : point.value);
                }
                if (!veryFirstPoint) veryFirstPoint = "0, " + (y(point.value == 0 ? minYValue / 10.0 : point.value));
                isFirstPoint = false;
            } else {
                if (!this.isStackedGraph) isFirstPoint = true;
            }

            // ********************* Make sure this is block commented-out in released code ****************************
            // This block is good for debugging NaNs, but it kills performance.
            //                        if (path.indexOf("NaN") >= 0) {
            //                            throw new Error('Object is not a Number');
            //                        }
        });
        return path;
    }

    _getFocusExtentOrNull() {
        if (this.brush.empty()) {
            return null;
        } else {
            let brushExtent = this.brush.extent();
            let isMaxRight = (this.brush.extent()[1].getTime() >= this.mostRecentDataDate);
            if (isMaxRight) {
                let interval = this.mostRecentDataDate - brushExtent[0].getTime();
                let newStartMs = this.mostRecentDataDate - interval;
                brushExtent = [new Date(newStartMs), new Date(this.mostRecentDataDate)]
            }
            return brushExtent;
        }
    }

    _getFocusExtent() {
        let extent = this._getFocusExtentOrNull();
        if (!extent) {
            extent = this._getContextExtents();
        }
        return extent;
    }

    _setBrush(brushExtent) {
        if (!this.brush.empty()) {
            let isMaxRight = (this.brush.extent()[1].getTime() >= this.mostRecentDataDate);
            if (isMaxRight) {
                this.x.domain(brushExtent);
                this.brush.extent(brushExtent);
            }
        }
    }

    update() {
        if (!this.dataSet || this.width <= 0) return;

        let brushExtent = this._getFocusExtentOrNull();
        let focusDataSet = this._getMappedDataSet(brushExtent, true);
        this._setBrush(brushExtent);
        let contextDataSet = this._getMappedDataSet(null, false);

        let that = this;

        let xDomain = this._getFocusExtent();

        let x2Domain = this._getContextExtents();
        let isMaxRight = (this.brush.extent()[1].getTime() == this.x2.domain()[1].getTime());
        this.x2.domain(x2Domain);

        if (this.brush.empty()) {
            this.x.domain(xDomain);
        } else {
            if (isMaxRight) {
                let interval = this.x.domain()[1].getTime() - this.x.domain()[0].getTime();
                let newStartMs = this.x2.domain()[1].getTime() - interval;
                this.x.domain([new Date(newStartMs), this.x2.domain()[1]]);
                this.brush.extent(this.x.domain());
            } else {
                this.brush.extent(this.x.domain());
            }
            this.brush(this.svg.select(".x.brush"));
        }

        let series = this.focus.selectAll(".series")
                .data(focusDataSet.reverse(), this._getKey)
                .enter().append("g")
                .attr("class", "series")
                //                        .attr("pointer-events", "none")
                .attr("visible", "")
            ;

        series
            .append("path")
            .attr("class", "line")
            .attr("clip-path", "url(" + location.href + "#clip)")
            .attr("pointer-events", "none")
            .attr("d", "")
            .style("stroke", function (d) {
                return that.color(d.key);
            })
            .style("fill", "none")
        ;

        let series2 = this.context.selectAll(".series")
            .data(contextDataSet, this._getKey)
            .enter().append("g")
            .attr("class", "series");

        series2.append("path")
            .attr("class", "line")
            .attr("clip-path", "url(" + location.href + "#clip)")
            .attr("pointer-events", "none")
            .attr("d", "")
            .style("stroke", function (d) {
                return that.color(d.key);
            })
            .style("fill", "none");

        // make sure the mouse pointe rappears on top of path elements.
        let mousePointerNode = this.mousePointer[0][0];
        let mousePointerParent = mousePointerNode.parentNode;
        mousePointerParent.removeChild(mousePointerNode);
        mousePointerParent.appendChild(mousePointerNode);

        this.mousePointer
            .attr("visibility", "hidden");

        this.focus.selectAll(".series")
            .data(focusDataSet, this._getKey)
            .exit()
            .remove();

        this.context.selectAll(".series")
            .data(contextDataSet, this._getKey)
            .exit()
            .remove();

        this.y.domain([this._getMinValue()-1, this._getMaxValue() +5]);

        this.y2.domain(this.y.domain());

        this.svg.select(".x.axis")
            .transition()
            .duration(this.animationDelay)
            .call(this.xAxis);
        this.svg.select(".x2.axis")
            .transition()
            .duration(this.animationDelay)
            .call(this.xAxis2);

        this.svg.select("g.yAxisL")
            .transition().delay(this.animationDelay)
            .duration(this.animationDelay)
            .call(that.yAxisL);

        this.svg.select("g.yAxisR")
            .transition()
            .duration(this.animationDelay)
            .call(that.yAxisR);

        this.svg.select("g.yAxis2")
            .transition()
            .duration(this.animationDelay)
            .call(that.yAxis2);

        this.focus.selectAll("path.line")
            .data(focusDataSet, this._getKey)
            .transition()
            .attr("d", function (d) {
                return that.focusLine(d.data, that._getMinValue());
            })
            .duration(this.animationDelay);

        this.context.selectAll("path.line")
            .data(contextDataSet, this._getKey)
            .transition()
            .attr("d", function (d) {
                return that.contextLine(d.data, that._getMinValue());
            })
            .duration(this.animationDelay);


        // if (this._isIE()) {
        //     // Internet Explorer will not draw the path elements without dropping a bus on it.
        //     let parent = this.svg[0][0].parentNode;
        //     parent.removeChild(this.svg[0][0]);
        //     parent.appendChild(this.svg[0][0]);
        // }
    }

    _isIE() {
        return (window.navigator && window.navigator.userAgent && window.navigator.userAgent.indexOf("Trident") >= 0);
    }

    _getMappedDataSet(extent, isFocusChart) {
        let narrowedDataSet = [];

        for (let index: number = 0; index < this.seriesCount; index++) {
            narrowedDataSet[index] = (extent == null ?
                this.dataSet[index]
                : this._getNarrowedSeries(this.dataSet[index], extent));
        }

        let data: any[] = [this.seriesCount];
        let binSize = this._getBinSize(narrowedDataSet, isFocusChart);
        this._showBinSize(binSize, (extent != null));
        for (let index: number = 0; index < this.seriesCount; index++) {
            data[index] = {key: index, data: this._reduceDataSeries(narrowedDataSet[index], binSize)};
        }
        return data;
    }

    _getPointClosest(series, timePos, timeGranularity, yPos, valueGranularity, key) {
        if (!series || series.length < 2) return null;

        let closestIndex = this._search(series, timePos);

        let closestPoint = series[closestIndex];

        if (typeof closestPoint.value !== 'number') return null;

        if (Math.abs(closestPoint.date - timePos) > timeGranularity) return null;

        if (Math.abs(closestPoint.value - yPos) > valueGranularity) return null;

        closestPoint.key = key;

        return closestPoint;
    }

    _search(series, target) {

        function interpolate(series, left, right, target) {
            let leftDate = series[left].date;
            let rightDate = series[right].date;
            let targetIndex = left + (right - left) * (target - leftDate) / (rightDate - leftDate);
            targetIndex = Math.round(targetIndex);
            targetIndex = Math.max(0, targetIndex);
            targetIndex = Math.min(right, targetIndex);

            return targetIndex;
        }

        let l = 0;
        let r = series.length - 1;

        let index = interpolate(series, l, r, target);

        function calculateLeftMargin(index) {
            return (index == 0 ? 0 : series[index].date - series[index - 1].date - 1);
        }

        function calculateRightMargin(index) {
            return (index == series.length - 1 ? 0 : series[index + 1].date - series[index].date - 1);
        }

        let seriesDateLeftMargin = calculateLeftMargin(index);
        let seriesDateRightMargin = calculateRightMargin(index);
        let previousIndex1 = -1;
        let previousIndex2 = -1;

        while (series[index].date - seriesDateLeftMargin / 2 > target || series[index].date + seriesDateRightMargin / 2 < target) {
            if (series[index].date > target) index = Math.max(0, --index);
            else if (series[index].date < target) index = Math.min(series.length - 1, ++index);
            if (index == previousIndex2 || index == previousIndex1) break;
            previousIndex2 = previousIndex1;
            previousIndex1 = index;

            seriesDateLeftMargin = calculateLeftMargin(index);
            seriesDateRightMargin = calculateRightMargin(index);
        }
        return index;
    }

    _getNarrowedSeries(series, extent) {
        if (series.length <= 2) return series;


        let startTime = (typeof extent[0] == 'object' ? extent[0].getTime() : extent[0]);
        let endTime = (typeof extent[1] == 'object' ? extent[1].getTime() : extent[1]);
        let startIndex = this._search(series, startTime);
        let endIndex = this._search(series, endTime);

        if (startIndex > 0) startIndex -= 1;
        if (endIndex < (series.length - 1)) endIndex += 1;

        if (startIndex > 0) startIndex -= 1;  //  The binary search above is off a little. Widen the window here again to avoid discontinuities
        if (endIndex < (series.length - 1)) endIndex += 1;


        return series.slice(startIndex, endIndex + 1);
    }

    _getContextExtents() {
        let extents = [null, null];
        for (let index: number = 0; index < this.seriesCount; index++) {
            if (this.dataSet[index]) {
                let first = (this.dataSet[index][0] ? this.dataSet[index][0].date : extents[0]);
                let length = this.dataSet[index].length;
                let last = (this.dataSet[index][length - 1] ? this.dataSet[index][length - 1].date : extents[1]);
                extents[0] = Math.min(extents[0] || first, first);
                extents[1] = Math.max(extents[1] || last, last);
            }
        }
        let now: number = Math.max(new Date().getTime(), this.mostRecentDataDate);
        extents[0] = extents[0] || now;
        extents[1] = extents[1] || now;
        if (extents[0] == extents[1]) extents[0] = extents[1] - 15 * 1000;
        return extents;
    }

    _reduceDataSeries(series, binSize) {
        if (binSize == 1) return series;
        let pointCount = series.length;
        let bucketCount = Math.ceil(pointCount / binSize);
        let newSeries: any[] = [bucketCount];
        for (let i = 0; i < bucketCount; i++) {
            let slice = series.slice(i * binSize, (i + 1) * binSize);

            let bucket;
            if (slice.length == 1) {
                bucket = slice[0].value;
            } else {
                bucket = slice.reduce(function (a, b) {
                    if (a.value !== undefined) {
                        return a.value + b.value;
                    }
                    return a + b.value;
                });
                bucket /= binSize;
            }
            newSeries[i] = {date: slice[0].date, value: bucket};
        }
        return newSeries;
    }

    _showTotalDataPointCount(count) {
        let field = document.getElementById("totalDataPointCount");
        if (field) field.innerHTML = count;
    }

    _showBinSize(binSize, isForFocusChart) {
        let field = document.getElementById((isForFocusChart ? "focusBinSize" : "contextBinSize"));
        if (field) field.innerHTML = binSize;
    }

    chartMousemove(svgObject) {
        let that = this;
        if (!this.dataSet) return;
        let mousePos = d3.mouse(svgObject);
        let timePos = this.x.invert(mousePos[0]).getTime();
        let yPos = this.y.invert(mousePos[1]);

        let timeGranularity = timePos - this.x.invert(mousePos[0] - this.aperture);
        let valueGranularity = Math.max(.2, yPos - this.y.invert(mousePos[1] + this.aperture));


        let dataPoints: any = [];
        for (let index: number = 0; index < this.seriesCount; index++) {
            let series = this.dataSet[index];

            let closestPoint = this._getPointClosest(series, timePos, timeGranularity, yPos, valueGranularity, index);

            if (closestPoint) {
                dataPoints.push({
                    value: closestPoint.value,
                    label: this.getLabels()[index],
                    date: closestPoint.date,
                    key: closestPoint.key
                });
                dataPoints.date = closestPoint.date;
            }
        }
        if (!dataPoints.date) return;

        dataPoints.sort(function (a, b) {
            let aDiff = Math.abs(a.value - yPos);
            let bDiff = Math.abs(b.value - yPos);
            return aDiff - bDiff;
        });

        if (dataPoints.length > 0) {
            this.mousePointer
                .attr("cx", this.x(dataPoints[0].date))
                .attr("cy", this.y(dataPoints[0].value || 0))
                .attr("visibility", "visible");
        } else {
            this.mousePointer
                .attr("visibility", "hidden");
        }

        let hoverDataPoints: any = {};
        let spacing = this._getTextBoundingBox(new Date(dataPoints.date).toString(), "hoverTime").width - 10;
        let itemWidth = this._getTextBoundingBox(this._getFormattedHoverOverValueText(0.00000000001), "hoverSeriesItemText").width + 10;
        hoverDataPoints.date = dataPoints.date;
        hoverDataPoints.data = [];
        dataPoints.forEach(function (point) {
            point["spacing"] = spacing;
            spacing += itemWidth;
            hoverDataPoints.data.push(point);
        });

        if (hoverDataPoints.data.length > 0) {
            let date = new Date(hoverDataPoints.date).toLocaleString();
            let hoverData = this.svg.selectAll(".hoverData");
            let hoverSeries = this.svg.selectAll(".hoverSeries");

            hoverData.select(".hoverPointsToggle")
                .classed("hoverPointsOff", false);

            hoverData
                .selectAll(".hoverTime")
                .text(date);

            let hoverSeriesItem = hoverSeries
                    .selectAll(".hoverSeriesItem")
                    .data(hoverDataPoints.data, this._getKey)
                    .enter()
                    .append("g")
                    .attr("class", "hoverSeriesItem")
                    .on("mouseover", function (d) {
                        that.mouseover(d);
                        that.hoverSeriesMouseover();
                    })
                    .on("touchstart", function (d) {
                        that.mouseover(d);
                        that.hoverSeriesMouseover();
                    })
                    .on("mouseout", function () {
                        that.mouseout();
                        that.hoverSeriesMouseout();
                    })
                    .on("touchend", function () {
                        that.mouseout();
                        that.hoverSeriesMouseout();
                    })

                ;

            hoverSeriesItem
                .append("rect")
                .attr("class", "hoverSeriesItemRect")
                .attr("width", 13)
                .attr("height", 13)
                .attr("y", -12)
                .attr("fill", function (d) {
                    return that.color(d.key);
                })
            ;

            hoverSeriesItem
                .append("text")
                .attr("class", "hoverSeriesItemText")
                .attr("y", 0)
            ;


            hoverSeries
                .selectAll(".hoverSeriesItemText")
                .data(hoverDataPoints.data, this._getKey)
                .attr("x", function (d) {
                    return d.spacing + 20;
                })
                .text(function (d) {
                    return that._getFormattedHoverOverValueText(d.value);
                })
            ;

            hoverSeries
                .selectAll(".hoverSeriesItemRect")
                .data(hoverDataPoints.data, this._getKey)
                .attr("x", function (d) {
                    return d.spacing;
                })
            ;

            let labels = this.getLabels();

            this.svg.selectAll(".legendItemText")
                .data(labels)
                .attr("fill", function (d) {
                    let found = false;
                    dataPoints.forEach(function (dataPoint) {
                        if (dataPoint.label == d) found = true;
                    });
                    return (found ? "#000" : "#ccc");
                })
            ;

            hoverSeries
                .selectAll(".hoverSeriesItem")
                .data(hoverDataPoints.data, this._getKey)
                .exit()
                .remove()
            ;

            this.clipMouseOvers();

        }

    }

    clipMouseOvers() {
        let that = this;
        let hoverSeries = this.svg.selectAll(".hoverSeries");

        function getVisibility() {
            let bb = this.getBBox();
            let visible = bb.x + bb.width < that.width;
            return (visible ? "visible" : "hidden");
        }

        hoverSeries
            .selectAll(".hoverSeriesItem")
            .attr("visibility", getVisibility)
        ;
    }

    _getFormattedHoverOverValueText(value) {
        let retVal = (value == null ? "" : value.toPrecision(6));
        if (retVal.indexOf("0.0") == 0 && retVal.length > 7) {
            return retVal.substring(0, 7);
        }
        return retVal;
    }

    _getWidestLabelWidth(labels: string[]) {
        let that = this;
        let width = 0;

        if (labels) labels.forEach(function (label) {
            let bb = that._getTextBoundingBox(label, null);

            width = Math.max(width, bb.width);
        });
        return width;
    }

    _getTextBoundingBox(str, optCssClass) {
        if (!this.svg) return 0;
        let text = this.svg
                .append("text")
                .attr("class", optCssClass || "legendItemText")
                .attr("stroke", "none")
                .attr("fill", "#000")
                .text(str)
            ;
        let bb = text.node().getBBox();
        text.remove();
        return bb;
    }

    // addNanCheck() {
    //     (Object.prototype as any).originalValueOf = Object.prototype.valueOf;
    //
    //     Object.prototype.valueOf = function() {
    //         if (typeof this !== 'number') {
    //             throw new Error('Object is not a Number');
    //         }
    //
    //         return this.originalValueOf();
    //     };
    //
    // }

    // removeNanCheck() {
    //     Object.prototype.valueOf = (Object.prototype as any).originalValueOf;
    //
    //
    // }
}
    
    
