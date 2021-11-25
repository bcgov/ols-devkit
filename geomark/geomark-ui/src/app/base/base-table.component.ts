import { ViewChild, Injector, AfterViewInit, Directive } from '@angular/core';
import { BaseComponent } from 'src/app/base/base-component';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { merge, of } from 'rxjs';
import { map, catchError } from 'rxjs/operators';
import { FormControl } from '@angular/forms';

@Directive()
export class BaseTableComponent extends BaseComponent implements AfterViewInit {

  displayedColumns: string[];

  records: any[] = [];

  pageSize = 10;

  resultCount = 0;

  searchField = new FormControl('');

  @ViewChild(MatSort)
  sort: MatSort;

  @ViewChild(MatPaginator)
  paginator: MatPaginator;

  servicePath: string;

  constructor(injector: Injector) {
    super(injector);
  }

  protected deleteRecord(id: string, action: string) {
    const path = `${this.servicePath}/${id}`;
    this.deleteRecordPath(path, action);
  }

  protected deleteRecordPath(path: string, action: string) {
    if (action === 'ok') {
      this.httpService.delete(path).subscribe(() => {
        this.refresh();
      });
    } else {
      this.refresh();
    }
  }

  protected saveRecord(record: any) {
    if (record == null) {
      this.refresh();
    } else {
      this.httpService.post(this.servicePath, record).subscribe(() => {
        this.refresh();
      });
    }
  }

  ngAfterViewInit() {
    this.refresh();
    if (this.paginator == null) {
      merge(this.sort.sortChange, this.searchField.valueChanges).subscribe(() => this.refresh());
    } else {
      this.sort.sortChange.subscribe(() => this.paginator.pageIndex = 0);
      merge(this.sort.sortChange, this.paginator.page, this.searchField.valueChanges).subscribe(() => this.refresh());
    }
  }



  protected loadRecords() {
    const params: any = {
      sort: this.sort.active,
      order: this.sort.direction,
      searchText: this.searchField.value,
    };
    if (this.paginator != null) {
      params.page = this.paginator.pageIndex.toString();
      params.pageSize = this.pageSize.toString();
    }
    return this.httpService.get<any>(this.servicePath, {
      params
    });
  }

  refresh() {
    this.loading = true;
    return this.loadRecords().pipe(
      map(data => {
        this.loading = false;
        this.resultCount = data.resultCount;
        return data.items;
      }),
      catchError((e) => {
        console.log(e);
        this.loading = false;
        return of(this.records);
      })
    ).subscribe(records => this.records = records);
  }
}
