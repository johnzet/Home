// import {Component, ElementRef, ViewEncapsulation, OnInit} from '@angular/core';
// import {NavigationEnd, NavigationStart, Route, Router} from '@angular/router';
//
// @Component({
//     selector: 'app-main-menu',
//     templateUrl: './main-menu.component.html',
//     styleUrls: ['./main-menu.component.css'],
//     encapsulation: ViewEncapsulation.None
// })
//
// export class MainMenuComponent implements OnInit {
//     constructor(private router: Router, private elementRef: ElementRef) {
//     }
//
//     ngOnInit(): void {
//         let that = this;
//         this.router.config.forEach(function(route) {
//             if (route.data && route.data.inMainMenu) {
//                 that.addItem(route);
//             }
//         });
//         this.router.events.subscribe(event => this.decorateRoute(event));
//     }
//
//     addItem(route: Route): void {
//         let that = this;
//         let container: Element = document.getElementById("topnavContainer");
//         if (container) {
//             let item:HTMLDivElement = document.createElement('div');
//             item.classList.add("topnavItem");
//             item.innerHTML = route.data.label;
//             item.onclick = function() {that.router.navigate([route.path]);};
//             item.setAttribute("routePath", "/" + route.path);
//             container.appendChild(item);
//         }
//     }
//
//     decorateRoute(event: any): void {
//         let container: Element = document.getElementById("topnavContainer");
//         if (container) {
//             let items: NodeListOf<HTMLDivElement> = this.getAllItems();
//             for (let i=0; i<items.length; i++) {
//                 let item: HTMLDivElement = items[i];
//                 let routePath: String = item.getAttribute("routePath");
//                 if (event instanceof NavigationStart) {
//                     if (routePath === event.url) {
//                         item.classList.add("selected");
//                     } else {
//                         item.classList.remove("selected");
//                     }
//                 }
//             }
//             //event.target.classList.add("selected");
//         }
//     }
//
//     getAllItems(): NodeListOf<HTMLDivElement> {
//         let container: Element = document.getElementById("topnavContainer");
//         if (container) return container.getElementsByTagName("div");
//         return null;
//     }
// }


import {MediaMatcher} from '@angular/cdk/layout';
import {ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {Router} from "@angular/router";

@Component({
    selector: 'app-main-menu',
    templateUrl: './main-menu.component.html',
    styleUrls: ['./main-menu.component.css'],
})
export class MainMenuComponent implements OnInit, OnDestroy {
    mobileQuery: MediaQueryList;

    navItems = [];
    contentItems = [];

    private _mobileQueryListener: () => void;

    constructor(private router: Router, changeDetectorRef: ChangeDetectorRef, media: MediaMatcher) {
        this.mobileQuery = media.matchMedia('(max-width: 600px)');
        this._mobileQueryListener = () => changeDetectorRef.detectChanges();
        this.mobileQuery.addListener(this._mobileQueryListener);
    }

    ngOnInit(): void {
        let self = this;
        this.router.config.forEach(function(route) {
            if (route.data && route.data.inMainMenu) {
                let item = {"routerLink": route.path, "label": route.data.label};
                self.navItems.push(item);
                self.contentItems.push(route.data.label);
            }
        });
    }

    ngOnDestroy(): void {
        this.mobileQuery.removeListener(this._mobileQueryListener);
    }

}