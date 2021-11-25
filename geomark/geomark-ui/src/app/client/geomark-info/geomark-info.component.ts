import { Component, Injector, Input } from '@angular/core';
import { BaseComponent } from '../../base/base-component';

@Component({
  selector: 'app-geomark-info',
  templateUrl: './geomark-info.component.html',
  styleUrls: ['./geomark-info.component.css']
})
export class GeomarkInfoComponent extends BaseComponent {

  get geomark(): any {
    return this.route.snapshot.parent.data.geomark;
  }

  constructor(
    injector: Injector,
  ) {
    super(injector);
  }

  get expiryDate(): string {
    return this.geomark.expiryDate || 'None';
  }

  get boundingBox(): string {
    // tslint:disable-next-line:max-line-length
    return `${this.format6(this.geomark.minX)},${this.format6(this.geomark.minY)},${this.format6(this.geomark.maxX)},${this.format6(this.geomark.maxY)}`;
  }

  private format6(value: number) {
    return new Intl.NumberFormat('en-IN', { useGrouping: false, maximumFractionDigits: 6 }).format(value);
  }
  private format3(value: number) {
    return new Intl.NumberFormat('en-IN', { maximumFractionDigits: 3 }).format(value).replace(',', ' ');
  }
  get centroid(): string {
    return `${this.format6(this.geomark.centroidY)},${this.format6(this.geomark.centroidX)}`;
  }

  get minimumClearance(): string {
    let minimumClearance = this.geomark.minimumClearance;
    let units = 'm';
    if (minimumClearance > 1000) {
      minimumClearance /= 1000;
      units = 'km';
    }
    return `${this.format3(minimumClearance)} ${units}`;
  }
  get length(): string {
    let length = this.geomark.length;
    let units = 'm';
    if (length > 1000) {
      length /= 1000;
      units = 'km';
    }
    return `${this.format3(length)} ${units}`;
  }

  get area(): string {
    let area = this.geomark.length;
    let units = 'm²';
    if (area > 100000) {
      area /= 10000;
      units = 'ha';
    }
    return `${this.format3(area)} ${units}`;
  }
}
