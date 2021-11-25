import { Component, OnInit, Input, Injector } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { BaseComponent } from '../../base/base-component';

@Component({
  selector: 'app-geometry-type',
  templateUrl: './geometry-type.component.html',
  styleUrls: ['./geometry-type.component.css']
})
export class GeometryTypeComponent extends BaseComponent implements OnInit {

  @Input()
  formGroup: FormGroup;

  constructor(injector: Injector) {
    super(injector);
  }

  ngOnInit(): void {
    this.formGroup.addControl('geometryType', new FormControl('Polygon', [Validators.required]));
    this.formGroup.addControl('multiple', new FormControl('false', [Validators.required]));
    this.formGroup.addControl('allowOverlap', new FormControl(false, [Validators.required]));
  }

}
