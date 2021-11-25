import { GeomarkConfigService } from '../service/geomark-config.service';
import { Injector } from '@angular/core';
import { Location } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { HttpService } from '../service/http.service';
import { ActivatedRoute } from '@angular/router';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ComponentType } from '@angular/cdk/portal';
import { ConfirmDialogComponent } from './confirm-dialog/confirm-dialog.component';

export class BaseComponent {

  public loading = false;

  public config = this.injector.get(GeomarkConfigService);

  public location = this.injector.get(Location);

  public http = this.injector.get(HttpClient);

  public httpService = this.injector.get(HttpService);

  public route = this.injector.get(ActivatedRoute);

  public dialog = this.injector.get(MatDialog);

  constructor(
    protected injector: Injector,
  ) {
  }

  openDialog<C>(
    component: ComponentType<C>,
    dialogData?: any,
  ): MatDialogRef<C> {
    return this.dialog.open(component, {
      panelClass: 'mat-dialog-panel-custom',
      data: dialogData,
      disableClose: true,
    });
  }

  public getUrl(options: {
    path?: string
    protocol?: string
  } = {}): string {
    return this.config.getUrl(options);
  }

  public glossaryUrl(field: string) {
    return this.docsUrl(`glossary.html#${field}`);
  }

  public docsUrl(path: string) {
    return `https://pauldaustin.github.io/geomark/${path}`;
  }

  public geomarkUrl(id: string) {
    return this.getUrl({ path: `/geomarks/${id}` });
  }

  public confirmDelete(typeTitle: string, objectLabel: string, callback: (action) => void) {
    this.openDialog(ConfirmDialogComponent, {
      callback,
      color: 'warn',
      title: `Delete ${typeTitle}?`,
      message: `Are you sure you want to delete ${typeTitle}`,
      objectLabel,
    });
  }
}
