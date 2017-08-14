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
        // let s = document.createElement("script");
        // s.type = "text/javascript";
        // s.src = "http://nmp.newsgator.com/NGBuzz/buzz.ashx?buzzId=81527&apiToken=DA1E5112812A448DA83D0CB5637BFAF8&trkP=&trkM=8715231F-62B9-1BC0-6F52-8AE9CAE98AD6";
        // this.elementRef.nativeElement.appendChild(s);
    }
}
