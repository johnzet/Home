import {Injectable} from '@angular/core';
import {Http, Response} from '@angular/http';
import {Observable} from 'rxjs/Rx';
import {Feed} from './model/feed';
import {forkJoin} from "rxjs/observable/forkJoin";

@Injectable()
export class FeedService {

    private rssToJsonServiceBaseUrl: string = 'https://rss2json.com/api.json?rss_url=';

    constructor(private http: Http) {
    }

    getFeedContent(urls: string[]): Observable<Feed[]> {
        if (!urls) return;
        let that = this;
        let feedObservables: Observable<Feed>[] = [];
        urls.forEach(function (url: string) {
            feedObservables.push(that.http.get(that.rssToJsonServiceBaseUrl + url)
                .map(that.extractFeeds)
                .catch(that.handleError)
        )});
        return Observable.forkJoin(feedObservables);
    }

    private extractFeeds(res: Response): Feed {
        let feed = res.json();
        return feed || {};
    }

    private handleError(error: any) {
        // In a real world app, we might use a remote logging infrastructure
        // We'd also dig deeper into the error to get a better message
        let errMsg = (error.message) ? error.message :
            error.status ? `${error.status} - ${error.statusText}` : 'Server error';
        console.error(errMsg); // log to console instead
        return Observable.throw(errMsg);
    }
}