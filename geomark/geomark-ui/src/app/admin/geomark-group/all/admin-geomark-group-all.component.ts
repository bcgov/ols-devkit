import { Component, Injector, } from '@angular/core';
import { BaseTableComponent } from 'src/app/base/base-table.component';
import { AdminGeomarkGroupDialogComponent } from '../dialog/admin-geomark-group-dialog.component';

@Component({
  selector: 'app-admin-geomark-group-all',
  templateUrl: './admin-geomark-group-all.component.html',
  styleUrls: ['./admin-geomark-group-all.component.css']
})
export class AdminGeomarkGroupAllComponent extends BaseTableComponent {
  constructor(injector: Injector) {
    super(injector);
    this.displayedColumns = ['GEOMARK_GROUP_ID', 'DESCRIPTION', 'WHEN_CREATED', 'actions'];
    this.servicePath = '/secure/admin/api/geomarkGroups';
  }

  deleteGeomarkGroup(geomarkGroup: any) {
    this.confirmDelete('Geomark Group', geomarkGroup.DESCRIPTION, (action) => {
      this.deleteRecord(geomarkGroup.GEOMARK_GROUP_ID, action);
    });
  }

  propertyDialog(geomarkGroup: any) {
    this.openDialog(AdminGeomarkGroupDialogComponent, {
      geomarkGroup,
      callback: (record) => this.saveRecord(record),
    });
  }
}
