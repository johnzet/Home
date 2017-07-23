import {Component, AfterContentInit, ElementRef, ViewEncapsulation, HostListener} from '@angular/core';
import {Router} from '@angular/router';

@Component({
    selector: 'app-main-menu',
    templateUrl: './main-menu.component.html',
    styleUrls: ['./main-menu.component.css'],
    encapsulation: ViewEncapsulation.None
})

export class MainMenuComponent {

    constructor(private router: Router, private elementRef: ElementRef) {
    }

    onBackBtnClick() {
        this.router.navigate(["/"]);
    }
}
