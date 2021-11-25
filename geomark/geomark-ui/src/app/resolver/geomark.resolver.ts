import { Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import {
  Resolve,
  ActivatedRouteSnapshot
} from '@angular/router';
import { HttpService } from '../service/http.service';

@Injectable()
export class GeomarkResolver implements Resolve<any> {
  constructor(
    private http: HttpService,
  ) {
  }

  resolve(route: ActivatedRouteSnapshot): Observable<any> {
    const geomarkId = route.params.geomarkId;
    return this.http.get(`/api/geomarks/${geomarkId}.json`);
  }
}
