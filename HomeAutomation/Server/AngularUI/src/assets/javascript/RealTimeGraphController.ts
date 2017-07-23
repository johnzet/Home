import {Http, RequestOptions, Headers} from '@angular/http';
import {Observable} from 'rxjs/Rx';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';
import {Injectable} from '@angular/core';
import {ObservableInput} from "rxjs/Observable";

let d3 = require("assets/javascript/d3");

@Injectable()
export class RealTimeGraphService {

    private sensorHosts: any;
    private data: any;
    private parseData: any;
    private restRoot: string = "http://localhost:8004/house";

    constructor (private http: Http) {
        this.data = undefined;
        /* "parse" is a function pointer */
        this.parseData = (d3.time.format("%Y%m%d") as any).parse;
    }

    buildMetadataObservable() : Observable<any> {
        let that = this;
        let headers:Headers = new Headers({ 'Content-Type': 'application/json' });
        let options = new RequestOptions({ headers: headers });
        return this.http
            .get(that.restRoot + "/sensor/list", options)
            .map(res =>  res.json())
            //.catch((error:any) => Observable.throw(error.json().error || 'Server error'));
            .catch(that.handleError)
    }

    buildDataObservable(): Observable<any> {
        let dataObservables = [];

        let that = this;
        let headers:Headers = new Headers({ 'Content-Type': 'application/json' });
        let options = new RequestOptions({ headers: headers });

        this.sensorHosts.forEach(function(host) {
            if (host.sensors) host.sensors.forEach(function(sensor) {
                dataObservables.push(
                    that.http.get(that.restRoot + "/sensor/data/" + sensor.id, options)
                        .map(res => res.json())
                        .catch(that.handleWarning)
                );
            })
        });
        return Observable.forkJoin(dataObservables);
    }


    load(): Observable<any> {
        let that = this;
        let lineDatas = [];
        this.data = [];
        this.sensorHosts = [];

        return Observable.create(function (observer) {

            that.buildMetadataObservable()
                .subscribe(
                    sensorHosts => that.sensorHosts = sensorHosts,
                    err => {that.handleError(err); observer.error(err);},
                    function () {
                        that.buildDataObservable()
                            .subscribe(
                                lineData => lineDatas = lineData,
                                err => that.handleWarning,
                                function () {
                                    // data load complete
                                    observer.next({metaData: that.sensorHosts, mixedData: lineDatas});
                                    observer.complete();
                                }
                            );
                    }
                );
        });
    }

    handleWarning(err: any, caught?: Observable<any>) : ObservableInput<{}> {
        console.log(err);
        return caught;
    }

    handleError(err: any, caught?: Observable<any>) : any {
        console.log(err);
        alert(err);
        return caught;
    }
}


