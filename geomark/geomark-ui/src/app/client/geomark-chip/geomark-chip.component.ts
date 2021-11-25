import { Component, OnInit, Input, Injector } from '@angular/core';
import { BaseComponent } from '../../base/base-component';

@Component({
  selector: 'app-geomark-chip',
  templateUrl: './geomark-chip.component.html',
  styleUrls: ['./geomark-chip.component.css']
})
export class GeomarkChipComponent extends BaseComponent implements OnInit {

  @Input()
  geomarkId: string;

  constructor(injector: Injector) {
    super(injector);
  }

  ngOnInit(): void {
  }

  get kmlUrl(): string {
    return this.getUrl({ protocol: 'http:', path: `/geomarks/${this.geomarkId}/feature.kml` });
  }
}
