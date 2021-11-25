import { Component, Injector } from '@angular/core';
import { BaseCreateComponent } from '../base-create.component';

@Component({
  selector: 'app-create-file',
  templateUrl: './create-file.component.html',
  styleUrls: ['./create-file.component.css']
})
export class CreateFileComponent extends BaseCreateComponent {

  constructor(injector: Injector) {
    super(injector);
  }

}
