import { Component, OnInit } from '@angular/core';
import {Feed} from "../model/feed";
import {FeedService} from "../feed.service";

@Component({
  selector: 'app-feed-deck',
  templateUrl: './feed-deck.component.html',
  styleUrls: ['./feed-deck.component.css']
})
export class FeedDeckComponent implements OnInit {

    private feed: Feed;

    constructor (private feedService: FeedService) {}

    ngOnInit() {
        this.refreshFeed();
    }

    private refreshFeed() {
        this.feedService.getFeedContent("http://feeds.bbci.co.uk/news/world/rss.xml")
            .subscribe(
                f => this.feed = f,
                // feed => this.feed = feed.items,
                error => console.log(error));
    }
}
