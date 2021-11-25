import {
  Directive,
  Output,
  EventEmitter,
  HostListener,
  HostBinding,
  Input,
} from '@angular/core';

@Directive({
  selector: '[appDragDrop]'
})
export class DragDropDirective {

  @Output()
  fileDropped = new EventEmitter<FileList>();

  @HostBinding('class.dragOver')
  draggedOver = false;

  @HostListener('dragover', ['$event'])
  dragOver(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.draggedOver = event.dataTransfer.types.indexOf('Files') !== -1;
  }

  @HostListener('dragleave', ['$event'])
  public dragLeave(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.draggedOver = false;
  }

  @HostListener('drop', ['$event'])
  public drop(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.draggedOver = false;
    const files = event.dataTransfer.files;
    if (files.length > 0) {
      this.fileDropped.emit(files);
    }
  }

  @HostListener('dragend', ['$event'])
  public dragend(event: DragEvent) {
    event.preventDefault();
    event.stopPropagation();
    this.draggedOver = false;
  }
}
