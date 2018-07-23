import {Component, ElementRef} from '@angular/core';


@Component({
  selector: 'app-home-page',
  templateUrl: './home-page.component.html',
  styleUrls: ['./home-page.component.css']
})
export class HomePageComponent {

    constructor(private elementRef:ElementRef) { }

    loadScript(fileName: string) {
//        require(fileName);

        let s = document.createElement("script");
        s.type = "text/javascript";
        s.src = fileName;
        this.elementRef.nativeElement.appendChild(s);

    }
}
