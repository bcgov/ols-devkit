import { Component, } from '@angular/core';

@Component({
  selector: 'app-admin-geomark-group-home',
  templateUrl: './admin-geomark-group-home.component.html',
  styleUrls: ['./admin-geomark-group-home.component.css']
})
export class AdminGeomarkGroupHomeComponent {

  navLinks = [
    {
      path: 'all',
      label: 'Geomark Groups'
    },
    {
      path: 'report',
      label: 'Report'
    },
  ];

}
