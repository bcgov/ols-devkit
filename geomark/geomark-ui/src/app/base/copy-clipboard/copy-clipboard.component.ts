import { Component, OnInit, Input, ViewChild, ElementRef } from '@angular/core';

@Component({
  selector: 'app-copy-clipboard',
  templateUrl: './copy-clipboard.component.html',
  styleUrls: ['./copy-clipboard.component.css']
})
export class CopyClipboardComponent implements OnInit {

  @Input()
  text: string;

  @Input()
  width = '40px';

  @Input()
  buttonLabel: string;

  @ViewChild('copyText', { static: true })
  copyText: ElementRef;

  constructor() { }

  ngOnInit(): void {
  }

  copy(event) {
    event.stopPropagation();
    const textArea = this.copyText.nativeElement;
    textArea.value = this.text;
    textArea.select();
    document.execCommand('copy');
    textArea.value = '';
  }
}
