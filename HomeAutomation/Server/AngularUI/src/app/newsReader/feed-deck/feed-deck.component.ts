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
        let deck:string[] = [
            "http://feeds.reuters.com/Reuters/worldNews",
            "http://feeds.reuters.com/Reuters/businessNews",
            "http://feeds.bbci.co.uk/news/world/rss.xml",
            "http://www.democracynow.org/democracynow.rss"
        ];

        this.feedService.getFeedContent(deck)
            .subscribe(
                f => this.feeds = f,
                error => console.log(error));
    }
}
