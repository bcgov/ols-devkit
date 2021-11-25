import { Component, OnInit, Injector, AfterViewInit } from '@angular/core';
import { BaseTableComponent } from 'src/app/base/base-table.component';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';
import { FormControl } from '@angular/forms';

@Component({
  selector: 'app-admin-geomark-group-view',
  templateUrl: './admin-geomark-group-view.component.html',
  styleUrls: ['./admin-geomark-group-view.component.css']
})
export class AdminGeomarkGroupViewComponent extends BaseTableComponent implements AfterViewInit {

  searchGeomarks: any[] = [];

  searchGeomarksControl = new FormControl('');

  get geomarkGroup(): any {
    return this.route.snapshot.data.geomarkGroup;
  }

  constructor(injector: Injector) {
    super(injector);
    this.displayedColumns = ['GEOMARK_ID', 'WHEN_CREATED', 'EXPIRY_DATE', 'actions'];

  }

  ngAfterViewInit() {
    this.servicePath = `/secure/admin/api/geomarkGroups/${this.geomarkGroup.GEOMARK_GROUP_ID}/geomarks`;
    this.searchGeomarksControl.valueChanges.subscribe((searchText) => {
      if (typeof searchText === 'string') {
        if (searchText === '') {
          this.searchGeomarks = [];
        } else {
          this.httpService.get<any>('/secure/admin/api/geomarks', {
            params: {
              searchText,
              sort: 'GEOMARK_ID'
            },
          }).subscribe((groupResult) => {
            if (groupResult?.items) {
              this.searchGeomarks = groupResult.items;
            }
          });
        }
      }
    });
    super.ngAfterViewInit();
  }

  kmlUrl(geomarkId: string): string {
    return this.getUrl({ protocol: 'http:', path: `/api/geomarks/${geomarkId}/feature.kml` });
  }
  expiryDate(geomark: any): string {
    return geomark.expiryDate || 'None';
  }

  isExpired(geomark: any): boolean {
    const date = new Date();
    const expiryDate = new Date(geomark.EXPIRY_DATE);
    return expiryDate.getTime() < date.getTime();
  }

  geomarkGroupSelected(event: MatAutocompleteSelectedEvent) {
    const geomark = event.option.value;
    const url = `/secure/admin/api/geomarkGroups/${this.geomarkGroup.GEOMARK_GROUP_ID}/geomarks/${geomark.GEOMARK_ID}`;
    this.searchGeomarksControl.setValue('');
    this.httpService.put(url, {}).subscribe(() => {
      this.refresh();
    });
  }

  displayGeomark(geomark: any): string {
    if (geomark == null) {
      return null;
    } else {
      return geomark.GEOMARK_ID;
    }
  }

  removeFromGroup(geomark: any) {
    this.confirmDelete('Geomark from Group', geomark.GEOMARK_ID, (action) => {
      this.deleteRecord(geomark.GEOMARK_ID, action);
    });
  }
}
