import {BrowserModule} from "@angular/platform-browser";
import {NgModule} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {HttpModule} from "@angular/http";
import {RouterModule, Routes} from "@angular/router";

import {AppComponent} from "./app.component";
import {MainMenuComponent} from "./main-menu/main-menu.component";
import {RtgPageComponent} from "./rtg-page/rtg-page.component";

const appRoutes: Routes = [
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
    // {path: 'home', component: HomeConditionsPageComponent},
    // {path: '**', component: MainPageComponent}
];

@NgModule({
    declarations: [
        AppComponent,
        MainMenuComponent,
        RtgPageComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        RouterModule.forRoot(appRoutes),

    ],
    providers: [],
    bootstrap: [AppComponent]
})
export class AppModule {
}
