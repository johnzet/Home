import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { HvacPageComponent } from './hvac-page.component';

describe('HvacPageComponent', () => {
  let component: HvacPageComponent;
  let fixture: ComponentFixture<HvacPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ HvacPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HvacPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
