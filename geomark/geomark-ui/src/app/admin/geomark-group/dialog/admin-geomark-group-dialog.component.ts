import { Component, OnInit, Inject, Injector } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ConfirmDialogComponent } from 'src/app/base/confirm-dialog/confirm-dialog.component';
import { BaseComponent } from 'src/app/base/base-component';

@Component({
  selector: 'app-admin-geomark-group-dialog',
  templateUrl: './admin-geomark-group-dialog.component.html',
  styleUrls: ['./admin-geomark-group-dialog.component.css']
})
export class AdminGeomarkGroupDialogComponent extends BaseComponent {

  get title(): string {
    const id = this.geomarkGroup.GEOMARK_GROUP_ID;
    if (id == null) {
      return 'Add Geomark Group';
    } else {
      return `Edit Geomark Group #${id}`;
    }
  }

  geomarkGroup: any = {
    GEOMARK_GROUP_ID: undefined,
    DESCRIPTION: '',
  };

  constructor(
    injector: Injector,
    public dialogRef: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) {
    super(injector);
    if (data.geomarkGroup != null) {
      this.geomarkGroup = Object.assign({}, data.geomarkGroup);
    }
    dialogRef.afterClosed().subscribe(action => {
      if (action === 'ok') {
        data.callback(this.geomarkGroup);
      } else {
        data.callback(null);
      }
    });
  }

}
