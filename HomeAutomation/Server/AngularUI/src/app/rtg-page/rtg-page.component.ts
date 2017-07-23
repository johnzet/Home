import {Component, AfterContentInit, ElementRef, ViewEncapsulation, HostListener} from '@angular/core';
import {Router} from '@angular/router';
import {RealTimeGraphService} from 'assets/javascript/RealTimeGraphController';
import {RealTimeChart} from 'assets/javascript/RealTimeChart';

@Component({
    selector: 'app-rtg-page',
    templateUrl: './rtg-page.component.html',
    styleUrls: ['./rtg-page.component.css'],
    encapsulation: ViewEncapsulation.None,
    providers: [RealTimeGraphService, RealTimeChart]
})

export class RtgPageComponent implements AfterContentInit {

    constructor(private router: Router, private elementRef: ElementRef,
                private rtgService: RealTimeGraphService, private rtg: RealTimeChart) {
    }

    ngAfterContentInit() {
        let node = this.elementRef.nativeElement;
        this.rtg.init(this.rtgService, node);
        this.rtg.loadThenRender();
    }

    @HostListener('window:resize', ['$event'])
    onResize(event) {
        if (this.rtg) {
            this.rtg.resize({w: event.target.innerWidth, h: event.target.innerHeight});
        }
    }

    onBackBtnClick() {
        this.router.navigate(["/"]);
    }
}
