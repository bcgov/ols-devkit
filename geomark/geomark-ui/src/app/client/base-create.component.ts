import { Injector } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { catchError, first } from 'rxjs/operators';
import { BaseComponent } from '../base/base-component';

export class BaseCreateComponent extends BaseComponent {

  saving = false;

  protected createUrl = '/api/geomarks/new';

  public form: FormGroup = new FormGroup({
    resultFormat: new FormControl('json'),
  });

  private router: Router = this.injector.get(Router);

  constructor(injector: Injector) {
    super(injector);
  }

  createGeomark() {
    this.saving = true;

    const url = this.getUrl({ path: this.createUrl });
    const formData = new FormData();
    for (const name of Object.keys(this.form.value)) {
      const value = this.form.value[name];
      if (value instanceof Array) {
        for (const item of value) {
          formData.append(name, item);
        }
      } else {
        formData.set(name, value);
      }
    }
    formData.set('format', this.form.value.format.fileExtension);
    this.http.post(
      url,
      formData,
    ).pipe(
      first(),
      catchError((err) => {
        console.log(err);
        return of(null);
      }),
    ).subscribe((result: any) => {
      this.saving = false;
      const id = result.id;
      if (id) {
        this.router.navigate(['/geomarks', id]).then((e) => {
          if (!e) {
            console.log(e);
          }
        });
      }
    });
  }
}
