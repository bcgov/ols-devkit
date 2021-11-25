import { Component, Input } from '@angular/core';
import { IconProp } from '@fortawesome/fontawesome-svg-core';

@Component({
    selector: 'app-fa-icon',
    template: `<fa-icon [icon]="name" style="height:auto;width:auto"></fa-icon>`,
})
export class FaIconComponent {

    @Input()
    name: IconProp;
}
