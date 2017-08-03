import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SprinklersPageComponent } from './sprinklers-page.component';

describe('SprinklersPageComponent', () => {
  let component: SprinklersPageComponent;
  let fixture: ComponentFixture<SprinklersPageComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SprinklersPageComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SprinklersPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should be created', () => {
    expect(component).toBeTruthy();
  });
});
