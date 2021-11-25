import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-geomarks-home',
  templateUrl: './geomarks-home.component.html',
  styleUrls: ['./geomarks-home.component.css']
})
export class GeomarksHomeComponent {

  navLinks = [
    {
      path: 'all',
      label: 'Geomarks'
    },
    {
      path: 'expired',
      label: 'Expired'
    },
    {
      path: 'temporary',
      label: 'Temporary'
    },
  ];

}
