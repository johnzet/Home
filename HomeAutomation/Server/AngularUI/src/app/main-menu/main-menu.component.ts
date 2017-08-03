import {Component, ElementRef, ViewEncapsulation, OnInit} from '@angular/core';
import {NavigationEnd, NavigationStart, Route, Router} from '@angular/router';

@Component({
    selector: 'app-main-menu',
    templateUrl: './main-menu.component.html',
    styleUrls: ['./main-menu.component.css'],
    encapsulation: ViewEncapsulation.None
})

export class MainMenuComponent implements OnInit {
    constructor(private router: Router, private elementRef: ElementRef) {
    }

    ngOnInit(): void {
        let that = this;
        this.router.config.forEach(function(route) {
            if (route.data && route.data.inMainMenu) {
                that.addItem(route);
            }
        });
        this.router.events.subscribe(event => this.decorateRoute(event));
    }

    addItem(route: Route): void {
        let that = this;
        let container: Element = document.getElementById("topnavContainer");
        if (container) {
            let item:HTMLDivElement = document.createElement('div');
            item.classList.add("topnavItem");
            item.innerHTML = route.data.label;
            item.onclick = function() {that.router.navigate([route.path]);};
            item.setAttribute("routePath", "/" + route.path);
            container.appendChild(item);
        }
    }

    decorateRoute(event: any): void {
        let container: Element = document.getElementById("topnavContainer");
        if (container) {
            let items: NodeListOf<HTMLDivElement> = this.getAllItems();
            for (let i=0; i<items.length; i++) {
                let item: HTMLDivElement = items[i];
                let routePath: String = item.getAttribute("routePath");
                if (event instanceof NavigationStart) {
                    if (routePath === event.url) {
                        item.classList.add("selected");
                    } else {
                        item.classList.remove("selected");
                    }
                }
            }
            //event.target.classList.add("selected");
        }
    }

    getAllItems(): NodeListOf<HTMLDivElement> {
        let container: Element = document.getElementById("topnavContainer");
        if (container) return container.getElementsByTagName("div");
        return null;
    }
}
