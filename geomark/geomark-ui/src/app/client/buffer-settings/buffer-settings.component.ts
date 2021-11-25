import { Component, OnInit, Input, Injector } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { BaseComponent } from '../../base/base-component';

@Component({
  selector: 'app-buffer-settings',
  templateUrl: './buffer-settings.component.html',
  styleUrls: ['./buffer-settings.component.css']
})
export class BufferSettingsComponent extends BaseComponent implements OnInit {

  @Input()
  formGroup: FormGroup;

  constructor(injector: Injector) {
    super(injector);
  }

  ngOnInit(): void {
    this.formGroup.addControl('bufferMetres', new FormControl(''));
    this.formGroup.addControl('bufferSegments', new FormControl('8', [Validators.required]));
    this.formGroup.addControl('bufferMitreLimit', new FormControl('5', [Validators.required]));
    this.formGroup.addControl('bufferCap', new FormControl('ROUND', [Validators.required]));
    this.formGroup.addControl('bufferJoin', new FormControl('ROUND', [Validators.required]));
  }

}
