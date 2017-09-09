import {BrowserModule} from "@angular/platform-browser";
import {NgModule} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {HttpModule} from "@angular/http";
import {RouterModule, Routes} from "@angular/router";
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

import {AppComponent} from "./app.component";
import {MainComponentComponent} from "./main-component/main-component.component";
import {MainMenuComponent} from "./main-menu/main-menu.component";
import {HomePageComponent} from "./sections/home-page/home-page.component";
import {SprinklersPageComponent} from "./sections/sprinklers-page/sprinklers-page.component";
import {HvacPageComponent} from "./sections/hvac-page/hvac-page.component";
import {WatergatePageComponent } from './sections/watergate-page/watergate-page.component';
import {SensorsPageComponent} from "./sections/sensors-page/sensors-page.component";
import {ChartsPageComponent} from "./sections/charts-page/charts-page.component";
import {FeedCardComponent } from './newsReader/feed-card/feed-card.component';
import {FeedService} from "./newsReader/feed.service";
import {FeedDeckComponent } from './newsReader/feed-deck/feed-deck.component';

const appRoutes: Routes = [
    {path: 'home', component: HomePageComponent, data: {inMainMenu: true, label: "Home"}},
    {path: 'sprinklers', component: SprinklersPageComponent, data: {inMainMenu: true, label: "Sprinklers"}},
    {path: 'hvac', component: HvacPageComponent, data: {inMainMenu: true, label: "HVAC"}},
    {path: 'watergate', component: WatergatePageComponent, data: {inMainMenu: true, label: "WaterGate"}},
    {path: 'sensors', component: SensorsPageComponent, data: {inMainMenu: true, label: "Sensors"}},
    {path: 'charts', component: ChartsPageComponent, data: {inMainMenu: true, label: "Charts"}},
    // {path: 'main:showPortfolio', component: MainPageComponent},
    // {path: 'wheel', component: WheelDiagramPageComponent},
    // {path: 'loom', component: LoomDiagramPageComponent},
    // {path: 'map', component: MapViewComponent},
    // {path: 'graph', component: RtgPageComponent},
    // {path: 'threed', component: ThreeDMapComponent},
    // {
    //     path: 'WaterGate', component: ProjectPageComponent,
    //     data: {
    //         title: 'WaterGate Project',
    //         description: waterGateDescription,
    //         photos: [
    //             {title: "Installed", imgUrl: "assets/images/WaterGate/installed.jpg"},
    //             {title: "Front Panel", imgUrl: "assets/images/WaterGate/FrontPanel.jpg"},
    //             {title: "Main Valve", imgUrl: "assets/images/WaterGate/valve.jpg"},
    //             {title: "Controller Front", imgUrl: "assets/images/WaterGate/controllerFront.jpg"},
    //             {title: "Controller Back", imgUrl: "assets/images/WaterGate/controllerBack.jpg"},
    //             {title: "Sensor Module Front", imgUrl: "assets/images/WaterGate/sensorModuleFront.jpg"},
    //             {title: "Sensor Module Back", imgUrl: "assets/images/WaterGate/sensorModuleBack.jpg"}
    //         ]
    //     }
    // },
    // {
    //     path: 'deck', component: ProjectPageComponent,
    //     data: {
    //         title: 'Deck Project',
    //         description: deckDescription,
    //         photos: [
    //             {title: "Post CAD", imgUrl: "assets/images/Deck/deckPost.png"},
    //             {title: "Post Finite Element Analysis", imgUrl: "assets/images/Deck/deckPostFea.png"}
    //         ]
    //     }
    // },
    {path: '', redirectTo: '/home', pathMatch: 'full'},
    {path: '**', redirectTo: '/home', pathMatch: 'full'}
];

@NgModule({
    declarations: [
        AppComponent,
        MainMenuComponent,
        MainComponentComponent,
        HomePageComponent,
        SprinklersPageComponent,
        HvacPageComponent,
        WatergatePageComponent,
        SensorsPageComponent,
        ChartsPageComponent,
        FeedCardComponent,
        FeedDeckComponent
    ],
    imports: [
        NgbModule.forRoot(),
        BrowserModule,
        BrowserAnimationsModule,
        FormsModule,
        HttpModule,
        RouterModule.forRoot(appRoutes)
    ],
    providers: [FeedService],
    bootstrap: [AppComponent]
})
export class AppModule {
}
