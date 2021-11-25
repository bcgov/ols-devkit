import { Component, Injector } from '@angular/core';
import { BaseComponent } from 'src/app/base/base-component';

@Component({
  selector: 'app-create-google-earth',
  templateUrl: './create-google-earth.component.html',
  styleUrls: ['./create-google-earth.component.css']
})
export class CreateGoogleEarthComponent extends BaseComponent {

  constructor(injector: Injector) {
    super(injector);
  }

}
