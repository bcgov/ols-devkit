import { Component, Injector, } from '@angular/core';
import { BaseTableComponent } from 'src/app/base/base-table.component';

@Component({
  selector: 'app-admin-geomark-group-report',
  templateUrl: './admin-geomark-group-report.component.html',
  styleUrls: ['./admin-geomark-group-report.component.css']
})
export class AdminGeomarkGroupReportComponent extends BaseTableComponent {

  constructor(injector: Injector) {
    super(injector);
    this.servicePath = '/secure/admin/api/geomarkGroups/report';
    this.displayedColumns = ['GEOMARK_GROUP_ID', 'DESCRIPTION', 'GEOMARK_COUNT', 'GEOMETRY_COUNT', 'VERTEX_COUNT'];
  }

}
