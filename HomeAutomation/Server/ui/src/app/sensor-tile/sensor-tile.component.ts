import {Component, ViewEncapsulation, ChangeDetectionStrategy, ElementRef} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {SensorType} from '../common/common';

let d3 = require("assets/javascript/d3");

@Component({
    selector: 'sensor-tile',
    templateUrl: './sensor-tile.component.html',
    styleUrls: ['./sensor-tile.component.css'],
    encapsulation: ViewEncapsulation.None,
    changeDetection: ChangeDetectionStrategy.OnPush
})


export class SensorTileComponent {
    private type: SensorType;
    private sensorData: any;
    private propertyName: string;

    private title: string;
    private valueJson: string;
    private currentValueHtml: string;


    public constructor() {
        // $http.get('http://rest-service.guides.spring.io/greeting').
        // then(function(response) {
        //     $scope.valueJson = response.data;
        // });
    }
    // public constructor(private activatedRoute: ActivatedRoute, private elementref: ElementRef) {
    // }
    //
    // public updateUI(sensorData: any, type: SensorType): void {
    //     this.sensorData = sensorData;
    //     if (!sensorData || !type) return;
    //     this.type = type;
    //     this.title = this.getTitle(type);
    //     this.currentValueHtml = this.getCurrentValueHtml(sensorData, type);
    // }
    //
    // private getTitle(type: SensorType) {
    //     let title: string;
    //     if (type == SensorType.TEMPERATURE) title ="Temperature";
    //     else if (type == SensorType.HUMIDITY) title ="Humidity";
    //     else if (type == SensorType.BAROMETER) title ="Barometer";
    //     else title = "Unknown title";
    //     return title;
    // }
    //
    // private getPropertyName(type: SensorType) {
    //     let property: string;
    //     if (type == SensorType.TEMPERATURE) property ="tempC";
    //     else if (type == SensorType.HUMIDITY) property ="humPct";
    //     else if (type == SensorType.BAROMETER) property ="baroHpa";
    //     else property = "Unknown type";
    //     return property;
    // }
    //
    // private getSensorUnit(type: SensorType) {
    //     let unit: string;
    //     if (type == SensorType.TEMPERATURE) unit ="&deg; C";
    //     else if (type == SensorType.HUMIDITY) unit ="%";
    //     else if (type == SensorType.BAROMETER) unit ="Hpa";
    //     else unit = "Unknown type";
    //     return unit;
    // }
    //
    // private getCurrentValueHtml(sensorData: any, sensorType: SensorType) {
    //     let sensorUnit: string = this.getSensorUnit(sensorType);
    //     let returnHtmlStr: string = "";
    //
    //     let that = this;
    //     sensorData.forEach(function(sensor) {
    //         if (!sensor.data || sensor.data.length < 1) return;
    //         let sensorName: string = sensor.name;
    //         this.propertyName = that.getPropertyName(sensorType);
    //         let currentValue: string = sensor.data[sensor.data.length-1][this.propertyName];
    //         returnHtmlStr += sensorName + "  " + currentValue + " " + sensorUnit + "&nbsp;&nbsp;&nbsp;";
    //     });
    //     return returnHtmlStr;
    // }
}
