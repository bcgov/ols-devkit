import { Component, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.css']
})
export class ConfirmDialogComponent {
  color: string = this.data.color || 'primary';

  title: string = this.data.title;

  message: string = this.data.message;

  objectLabel: string = this.data.objectLabel;

  callback: (action) => void;

  constructor(
    public dialog: MatDialogRef<ConfirmDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) {
    dialog.afterClosed().subscribe(action => data.callback(action));
  }

}
