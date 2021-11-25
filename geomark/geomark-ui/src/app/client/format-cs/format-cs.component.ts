import { Component, OnInit, Input, Injector } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';
import { GeomarkConfigService } from 'src/app/service/geomark-config.service';
import { BaseComponent } from '../../base/base-component';


@Component({
  selector: 'app-format-cs',
  templateUrl: './format-cs.component.html',
  styleUrls: ['./format-cs.component.css']
})
export class FormatCsComponent extends BaseComponent implements OnInit {

  coordinateSystemIds = [];

  fileFormats = [];

  @Input()
  formGroup: FormGroup;

  @Input()
  textOnly = false;

  @Input()
  mode = 'input';

  @Input()
  flexDirection = 'row';

  constructor(
    injector: Injector,
  ) {
    super(injector);
    this.loading = true;
  }


  ngOnInit() {
    this.formGroup.addControl('srid', new FormControl(null, [Validators.required]));
    this.formGroup.addControl('format', new FormControl(null, [Validators.required]));
    this.config.changed.subscribe((config) => {
      if (config != null) {
        this.coordinateSystemIds = config.coordinateSystemsWgs84Ids;
        this.fileFormats = config.fileFormats.filter((format) => {
          if (!this.textOnly || format.text) {
            if (this.mode === 'input' && format.input) {
              return true;
            } else if (this.mode === 'output' && format.output) {
              return true;
            }
          }
          return false;
        });
        this.formGroup.valueChanges.subscribe((values) => {
          const fileFormat = values.format;
          const srid = values.srid;
          if (fileFormat != null) {
            this.coordinateSystemIds = fileFormat.coordinateSystemIds;
            if (this.coordinateSystemIds.indexOf(srid) === -1) {
              this.formGroup.patchValue({ srid: this.coordinateSystemIds[0] });
            }
          }
        });
        this.formGroup.patchValue({ srid: 4326, format: this.fileFormats[0] });
        this.loading = false;
      }
    });
  }

}
