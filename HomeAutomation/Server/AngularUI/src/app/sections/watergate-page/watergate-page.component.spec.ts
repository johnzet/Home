import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WatergatePageComponent } from './watergate-page.component';

describe('WatergatePageComponent', () => {
  let component: WatergatePageComponent;
  let fixture: ComponentFixture<WatergatePageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WatergatePageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WatergatePageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
