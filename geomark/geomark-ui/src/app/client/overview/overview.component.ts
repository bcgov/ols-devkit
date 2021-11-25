import { Component, Injector } from '@angular/core';
import { BaseComponent } from '../../base/base-component';


@Component({
  selector: 'app-overview',
  templateUrl: './overview.component.html',
  styleUrls: ['./overview.component.css']
})
export class OverviewComponent extends BaseComponent {

  constructor(injector: Injector) {
    super(injector);
  }

}
