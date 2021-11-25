import { Component, OnInit, Injector, Input } from '@angular/core';
import { BaseComponent } from '../../base/base-component';

import { FormGroup, FormControl } from '@angular/forms';

@Component({
  selector: 'app-geomark-download',
  templateUrl: './geomark-download.component.html',
  styleUrls: ['./geomark-download.component.css']
})
export class GeomarkDownloadComponent extends BaseComponent implements OnInit {

  form = new FormGroup({ type: new FormControl('parts') });

  get geomark(): any {
    return this.route.snapshot.parent.data.geomark;
  }

  constructor(injector: Injector) {
    super(injector);
  }

  ngOnInit() {
    if (this.geomark.numParts === 1) {
      this.form.patchValue({ type: 'feature' });
    }
  }

  get downloadUrl() {
    const format = this.form.value.format;
    const coordinateSystemId = this.form.value.srid;
    const type = this.form.value.type;
    return `${this.geomark.url}/${type}.${format.fileExtension}?srid=${coordinateSystemId}`;
  }

}
