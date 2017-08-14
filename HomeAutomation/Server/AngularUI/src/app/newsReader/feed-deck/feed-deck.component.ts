import { Component, OnInit } from '@angular/core';
import {Feed} from "../model/feed";
import {FeedService} from "../feed.service";

@Component({
  selector: 'app-feed-deck',
  templateUrl: './feed-deck.component.html',
  styleUrls: ['./feed-deck.component.css']
})
export class FeedDeckComponent implements OnInit {

    private feeds: Feed[];

    constructor (private feedService: FeedService) {}

    ngOnInit() {
        this.feeds = [];
        this.refreshFeed();
    }

    private refreshFeed() {
        let deck:string[] = ["http://feeds.bbci.co.uk/news/world/rss.xml", "http://rss.cnn.com/rss/cnn_topstories.rss"];

        this.feedService.getFeedContent(deck)
            .subscribe(
                f => this.feeds = f,
                error => console.log(error));
    }
}
