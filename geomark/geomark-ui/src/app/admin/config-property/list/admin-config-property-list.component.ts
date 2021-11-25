import { Component, Injector } from '@angular/core';
import { BaseTableComponent } from 'src/app/base/base-table.component';
import { AdminConfigPropertyDialogComponent } from '../dialog/admin-config-property-dialog.component';

@Component({
  selector: 'app-admin-config-list',
  templateUrl: './admin-config-property-list.component.html',
  styleUrls: ['./admin-config-property-list.component.css']
})
export class AdminConfigPropertyListComponent extends BaseTableComponent {
  constructor(injector: Injector) {
    super(injector);
    this.displayedColumns = ['CONFIG_PROPERTY_ID', 'PROPERTY_NAME', 'VALUE', 'actions'];
    this.servicePath = '/secure/admin/api/configProperties';
  }

  deleteConfigProperty(configProperty: any) {
    this.confirmDelete('Config Property', configProperty.PROPERTY_NAME, (action) => {
      this.deleteRecord(configProperty.CONFIG_PROPERTY_ID, action);
    });
  }

  propertyDialog(configProperty: any) {
    this.openDialog(AdminConfigPropertyDialogComponent, {
      configProperty,
      callback: (record) => this.saveRecord(record),
    });
  }
}
