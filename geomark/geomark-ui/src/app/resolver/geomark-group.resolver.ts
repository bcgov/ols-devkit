import { Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import {
  Resolve,
  ActivatedRouteSnapshot
} from '@angular/router';
import { HttpService } from '../service/http.service';

@Injectable()
export class GeomarkGroupResolver implements Resolve<any> {
  constructor(
    private http: HttpService,
  ) {
  }

  resolve(route: ActivatedRouteSnapshot): Observable<any> {
    const geomarkGroupId = route.params.geomarkGroupId;
    return this.http.get(`/secure/admin/api/geomarkGroups/${geomarkGroupId}`);
  }
}
