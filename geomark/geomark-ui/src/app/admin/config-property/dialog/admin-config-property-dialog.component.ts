import { Component, OnInit, Inject, Injector } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ConfirmDialogComponent } from 'src/app/base/confirm-dialog/confirm-dialog.component';
import { BaseComponent } from 'src/app/base/base-component';

@Component({
  selector: 'app-admin-config-property-dialog',
  templateUrl: './admin-config-property-dialog.component.html',
  styleUrls: ['./admin-config-property-dialog.component.css']
})
export class AdminConfigPropertyDialogComponent extends BaseComponent {

  get title(): string {
    const id = this.configProperty.CONFIG_PROPERTY_ID;
    if (id == null) {
      return 'Add Config Property';
    } else {
      return `Edit Config Property #${id}`;
    }
  }

  configProperty: any = {
    CONFIG_PROPERTY_ID: undefined,
    PROPERTY_NAME: '',
    VALUE: '',
  };

  constructor(
    injector: Injector,
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) {
    super(injector);
    if (data.configProperty != null) {
      this.configProperty = Object.assign({}, data.configProperty);
    }
    dialogRef.afterClosed().subscribe(action => {
      if (action === 'ok') {
        data.callback(this.configProperty);
      } else {
        data.callback(null);
      }
    });
  }

}
