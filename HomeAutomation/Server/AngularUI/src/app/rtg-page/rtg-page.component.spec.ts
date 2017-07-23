/* tslint:disable:no-unused-variable */
import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {DebugElement} from '@angular/core';

import {RtgPageComponent} from './rtg-page.component';
import {RouterTestingModule} from "@angular/router/testing";

describe('RtgPageComponent', () => {
    let component: RtgPageComponent;
    let fixture: ComponentFixture<RtgPageComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            declarations: [RtgPageComponent],
            imports: [
                RouterTestingModule
            ]
        })
            .compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(RtgPageComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
