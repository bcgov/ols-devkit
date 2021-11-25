import {
  Component, ElementRef, EventEmitter, Injector, Input, OnInit, Output, ViewChild
} from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { BaseComponent } from '../base-component';

@Component({
  selector: 'app-file-upload',
  templateUrl: 'file-upload.component.html',
  styleUrls: ['file-upload.component.css'],
})
export class FileUploadComponent extends BaseComponent implements OnInit {

  @ViewChild('fileInput', { static: true })
  fileInput: ElementRef;

  file: File;

  @Input()
  fileType: string;

  @Input()
  formGroup: FormGroup;

  formControl = new FormControl(null, [Validators.required]);

  @Output()
  fileSelected = new EventEmitter<File>();

  constructor(injector: Injector) {
    super(injector);
  }

  ngOnInit() {
    this.formGroup.addControl('body', this.formControl);
    this.formGroup.controls.format.valueChanges.subscribe((format) => {
      this.fileType = format.fileExtension;
      this.setFile(this.file);
    });
  }

  fileSize(file: File): string {
    let size = file.size;
    const digits = Math.ceil(Math.log10(size));
    let suffix: string;
    if (digits <= 3) {
      suffix = 'B';
    } else if (digits <= 6) {
      suffix = 'KB';
      size = Math.round(size / 1e3);
    } else if (digits <= 9) {
      suffix = 'MB';
      size = Math.round(size / 1e6);
    } else {
      suffix = 'GB';
      size = Math.round(size / 1e9);
    }
    return size + ' ' + suffix;
  }

  addFilesInput(event: Event) {
    const files = (event.target as any).files;
    this.addFiles(files);
  }

  addFiles(files: FileList) {
    if (files.length > 0) {
      this.setFile(files.item(0));
    }
  }

  setFile(file: File) {
    if (file == null || this.fileType != null && !file.name.endsWith('.' + this.fileType)) {
      this.file = null;
    } else {
      this.file = file;
    }
    this.formControl.setValue(this.file);
  }

  removeFile() {
    this.setFile(null);
  }

  selectFile(event) {
    this.fileInput.nativeElement.click();
    event.stopPropagation();
  }
}
