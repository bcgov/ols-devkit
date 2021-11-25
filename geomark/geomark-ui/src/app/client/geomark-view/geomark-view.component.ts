import { Component, Injector } from '@angular/core';
import { IconProp } from '@fortawesome/fontawesome-svg-core';
import { BaseComponent } from '../../base/base-component';

interface Link {
  path: string;
  label: string;
  iconName: IconProp;
}

@Component({
  selector: 'app-geomark-view',
  templateUrl: './geomark-view.component.html',
  styleUrls: ['./geomark-view.component.css']
})
export class GeomarkViewComponent extends BaseComponent {

  get geomark(): any {
    return this.route.snapshot.data.geomark;
  }

  navLinks: Link[] = [
    {
      path: 'info',
      label: 'Info',
      iconName: 'info',
    },
    {
      path: 'download',
      label: 'Download',
      iconName: 'download',
    },
    {
      path: 'map',
      label: 'Map',
      iconName: 'map',
    },
  ];

  constructor(
    injector: Injector,
  ) {
    super(injector);
  }

}
