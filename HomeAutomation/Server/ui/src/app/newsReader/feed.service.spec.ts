import { TestBed, inject } from '@angular/core/testing';

import { FeedService } from './feed.service';

describe('NewsFeedServiceService', () => {
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [FeedService]
    });
  });

  it('should be created', inject([FeedService], (service: FeedService) => {
    expect(service).toBeTruthy();
  }));
});
