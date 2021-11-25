import { Component, Injector } from '@angular/core';
import { FormControl, Validators } from '@angular/forms';
import { BaseCreateComponent } from '../base-create.component';

@Component({
  selector: 'app-create-clipboard',
  templateUrl: './create-clipboard.component.html',
  styleUrls: ['./create-clipboard.component.css']
})
export class CreateClipboardComponent extends BaseCreateComponent {

  constructor(injector: Injector) {
    super(injector);
    this.form.addControl('body', new FormControl('', [Validators.required]));
  }

}
