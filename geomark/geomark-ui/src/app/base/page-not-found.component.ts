import { Component, Input } from '@angular/core';
import { Location } from '@angular/common';

@Component({
  selector: 'app-page-not-found',
  template: `
<mat-spinner *ngIf="loading; else loaded">
</mat-spinner>
<ng-template #loaded>
  <mat-card style="color:red; margin: 6px">
    <mat-card-title>404 - Not Found Error</mat-card-title>
    <p>I'm sorry the page you requested could not be found.</p>
    <mat-card-actions>
      <button  (click)="back()" mat-raised-button color="warn">
        <span><app-fa-icon name="chevron-left"></app-fa-icon> Back</span>
      </button>
    </mat-card-actions>
  </mat-card>
</ng-template>
  `,
})
export class PageNotFoundComponent {
  @Input()
  loading = false;

  constructor(private location: Location) {
  }

  back() {
    this.location.back();
  }
}
