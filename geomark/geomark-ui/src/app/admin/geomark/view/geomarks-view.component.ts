import { Component, OnInit, Injector } from '@angular/core';
import { BaseComponent } from 'src/app/base/base-component';
import { BaseTableComponent } from 'src/app/base/base-table.component';
import { FormControl } from '@angular/forms';
import { MatAutocompleteSelectedEvent } from '@angular/material/autocomplete';

@Component({
  selector: 'app-geomarks-view',
  templateUrl: './geomarks-view.component.html',
  styleUrls: ['./geomarks-view.component.css']
})
export class GeomarksViewComponent extends BaseTableComponent {

  searchGeomarkGroups: any[] = [];
  searchGeomarkGroupsControl = new FormControl('');

  get geomark(): any {
    return this.route.snapshot.data.geomark;
  }

  constructor(injector: Injector) {
    super(injector);
    this.displayedColumns = ['GEOMARK_GROUP_ID', 'DESCRIPTION', 'WHEN_CREATED', 'actions'];
    this.servicePath = `/secure/admin/api/geomarks/${this.geomark.id}/groups`;
    this.searchGeomarkGroupsControl.valueChanges.subscribe((searchText) => {
      if (typeof searchText === 'string') {
        if (searchText !== '') {
          this.httpService.get<any>('/secure/admin/api/geomarkGroups', {
            params: {
              searchText,
              sort: 'DESCRIPTION'
            },
          }).subscribe((groupResult) => {
            if (groupResult?.items) {
              this.searchGeomarkGroups = groupResult.items;
            }
          });
        } else {
          this.searchGeomarkGroups = [];
        }
      }
    });
  }

  get expiryDate(): string {
    return this.geomark.expiryDate || 'None';
  }

  kmlUrl(geomarkId: string): string {
    return this.getUrl({ protocol: 'http:', path: `/api/geomarks/${geomarkId}/feature.kml` });
  }

  geomarkGroupSelected(event: MatAutocompleteSelectedEvent) {
    const geomarkGroup = event.option.value;
    const url = `/secure/admin/api/geomarkGroups/${geomarkGroup.GEOMARK_GROUP_ID}/geomarks/${this.geomark.id}`;
    this.searchGeomarkGroupsControl.setValue('');
    this.httpService.put(url, {}).subscribe(() => {
      this.refresh();
    });
  }

  displayGeomarkGroup(group: any): string {
    if (group == null) {
      return null;
    } else {
      return group.DESCRIPTION;
    }
  }

  removeFromGroup(group: any) {
    this.confirmDelete('Geomark from Group', group.GEOMARK_GROUP_ID, (action) => {
      this.deleteRecord(group.GEOMARK_GROUP_ID, action);
    });
  }
}
