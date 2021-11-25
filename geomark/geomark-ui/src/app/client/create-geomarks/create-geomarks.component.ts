import { Component, Injector } from '@angular/core';
import { FormControl, Validators, } from '@angular/forms';
import { MatChipInputEvent } from '@angular/material/chips';
import { BaseCreateComponent } from '../base-create.component';

@Component({
  selector: 'app-create-geomarks',
  templateUrl: './create-geomarks.component.html',
  styleUrls: ['./create-geomarks.component.css']
})
export class CreateGeomarksComponent extends BaseCreateComponent {

  geomarkUrls: string[] = [];

  geomarkUrlsControl = new FormControl(this.geomarkUrls, [(c) => {
    if (c.value.length === 0) {
      return { required: true };
    } else {
      return null;
    }
  }]);

  constructor(injector: Injector) {
    super(injector);
    this.createUrl = '/api/geomarks/copy';
    this.form.addControl('body', new FormControl(false));
    this.form.addControl('geomarkUrls', this.geomarkUrlsControl);
    this.form.addControl('allowOverlap', new FormControl(false, [Validators.required]));
  }

  removeUrl(url: string) {
    this.geomarkUrls = this.geomarkUrls.filter((geomarkUrl) => geomarkUrl !== url);
    this.geomarkUrlsControl.setValue(this.geomarkUrls);
  }

  addUrl(event: MatChipInputEvent) {
    const input = event.input;
    const rexexp = RegExp(input.pattern);
    const url = event.value;
    if (rexexp.test(url)) {
      this.geomarkUrlsControl.setErrors(null);

      if (this.geomarkUrls.indexOf(url) === -1) {
        this.geomarkUrls.push(url);
      }
      this.geomarkUrlsControl.setValue(this.geomarkUrls);

      input.value = '';
    } else {
      this.geomarkUrlsControl.updateValueAndValidity();
    }
  }

}
