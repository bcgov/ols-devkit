import { Component, OnInit, ElementRef, ViewChild, Input, Injector } from '@angular/core';
import { Map as LMap } from 'leaflet';
import * as L from 'leaflet';
import { HttpClient } from '@angular/common/http';
import { BaseComponent } from 'src/app/base/base-component';

@Component({
  selector: 'app-geomark-map',
  templateUrl: './geomark-map.component.html',
  styleUrls: ['./geomark-map.component.css']
})
export class GeomarkMapComponent extends BaseComponent implements OnInit {
  @ViewChild('map', { static: true })
  mapElement: ElementRef;

  get geomark(): any {
    return this.route.snapshot.parent.data.geomark;
  }

  constructor(injector: Injector) {
    super(injector);
  }

  ngOnInit(): void {
    L.Icon.Default.imagePath = '';
    L.Icon.Default.mergeOptions({
      iconRetinaUrl: 'assets/images/marker-icon-2x.png',
      iconUrl: 'assets/images/marker-icon.png',
      shadowUrl: 'assets/images/marker-shadow.png',
    });
    const map = new LMap(this.mapElement.nativeElement, {
      maxZoom: 17
    });


    new L.tileLayer.wms('http://maps.gov.bc.ca/arcgis/services/province/roads_wm/MapServer/WMSServer', {
      layers: '0',
      format: 'image/png',
      transparent: true,
      attribution: '© 2013-2015 Data BC, The Province of British Columbia'
    }).addTo(map);
    const url = `${this.geomark.url}/feature.geojson`;
    this.http.get(url).subscribe((data) => {
      const layer = L.geoJson(data);
      map.fitBounds(layer.getBounds(), {
        maxZoom: 17
      });
      layer.addTo(map);
    });
  }

}
