/* tslint:disable:no-unused-variable */
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {DebugElement} from '@angular/core';

import {HomeConditionsPageComponent} from './home-conditions-page.component';
import {RouterTestingModule} from "@angular/router/testing";

describe('HomeConditionsPageComponent', () => {
    let component: HomeConditionsPageComponent;
    let fixture: ComponentFixture<HomeConditionsPageComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [
                HomeConditionsPageComponent,
            ],
            imports: [
                RouterTestingModule
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(HomeConditionsPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
