import { Injectable } from '@angular/core';
import { Location } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { first } from 'rxjs/operators';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class GeomarkConfigService {

  public changed = new BehaviorSubject<GeomarkConfigService>(null);

  public coordinateSystemById = {};

  public coordinateSystemIds = [];

  public coordinateSystemsWgs84Ids = [4326];

  public fileFormats = [];

  public fileFormatByExtension = {};

  public footerLinks = {};

  public properties: any = {};

  constructor(
    private location: Location,
    private http: HttpClient,
  ) {

    this.http.get(this.getUrl({ path: '/api/config' })).pipe(first()).subscribe((config: any) => {
      this.properties = config;
      this.footerLinks = config.footerLinks;
      for (const coordinateSystem of config.coordinateSystems) {
        const id = coordinateSystem.id;
        const name = coordinateSystem.name;
        this.coordinateSystemIds.push(id);
        this.coordinateSystemById[id] = `${id} - ${name}`;
      }

      for (const format of config.fileFormats) {
        this.fileFormats.push(format);
        this.fileFormatByExtension[format.fileExtension] = format;

      }

      this.changed.next(this);
    });
  }


  public getUrl(options: {
    path?: string
    protocol?: string
  }): string {
    const protocol = options.protocol || window.location.protocol;
    const host = window.location.host;
    let path = options.path || '/';
    path = this.location.prepareExternalUrl(path);
    path = path.replace('//', '/');
    const url = `${protocol}//${host}${path}`;
    return url;
  }
}
