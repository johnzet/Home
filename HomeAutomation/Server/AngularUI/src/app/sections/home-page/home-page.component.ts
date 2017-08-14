import {Component, OnInit, AfterViewInit, ElementRef} from '@angular/core';

@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.css']
})
export class HomePageComponent implements OnInit, AfterViewInit {

    constructor(private elementRef:ElementRef) { }

    ngOnInit() {
    }

    ngAfterViewInit() {
        let s = document.createElement("script");
        s.type = "text/javascript";
        s.src = "https://oap.accuweather.com/launch.js";
        this.elementRef.nativeElement.appendChild(s);
    }
}
