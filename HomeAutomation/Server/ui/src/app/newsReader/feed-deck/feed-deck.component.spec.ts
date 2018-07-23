import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { FeedDeckComponent } from './feed-deck.component';

describe('FeedDeckComponent', () => {
  let component: FeedDeckComponent;
  let fixture: ComponentFixture<FeedDeckComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ FeedDeckComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(FeedDeckComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
