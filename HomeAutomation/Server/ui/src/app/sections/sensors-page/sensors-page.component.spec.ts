import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SensorsPageComponent } from './sensors-page.component';

describe('SensorsPageComponent', () => {
  let component: SensorsPageComponent;
  let fixture: ComponentFixture<SensorsPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SensorsPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SensorsPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
