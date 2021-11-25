import { Component, Injector, AfterViewInit, Input } from '@angular/core';
import { BaseTableComponent } from 'src/app/base/base-table.component';

@Component({
  selector: 'app-geomarks-table',
  templateUrl: './geomarks-table.component.html',
  styleUrls: ['./geomarks-table.component.css']
})
export class GeomarksTableComponent extends BaseTableComponent implements AfterViewInit {
  @Input()
  pagePath: string;

  constructor(injector: Injector) {
    super(injector);
    this.displayedColumns = ['GEOMARK_ID', 'WHEN_CREATED', 'EXPIRY_DATE'];
  }

  ngAfterViewInit() {
    this.servicePath = `/secure/admin/api/geomarks${this.pagePath}`;
    super.ngAfterViewInit();
  }

  kmlUrl(geomarkId: string): string {
    return this.getUrl({ protocol: 'http:', path: `/api/geomarks/${geomarkId}/feature.kml` });
  }

  isExpired(geomark: any): boolean {
    const date = new Date();
    const expiryDate = new Date(geomark.EXPIRY_DATE);
    return expiryDate.getTime() < date.getTime();
  }
}
