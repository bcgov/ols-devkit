import {
  Component, Injector, OnInit
} from '@angular/core';
import { Title } from '@angular/platform-browser';
import {
  NavigationEnd,
  Router
} from '@angular/router';
import {
  filter, map, mergeMap
} from 'rxjs/operators';
import { BaseComponent } from '../base/base-component';
import { MenuItem } from './menu-item';

@Component({
  selector: 'app-page-template',
  templateUrl: './page-template.component.html',
  styleUrls: ['./page-template.component.css']
})
export class PageTemplateComponent extends BaseComponent implements OnInit {

  appTitle = 'Geomark';

  footerMenuVisible = true;

  version = '6.0.0';

  clientHeaderMenuItems: Array<MenuItem> = [
    {
      routerLink: '/overview',
      icon: 'info-circle',
      title: 'Overview',
    },
    {
      routerLink: '/create/file',
      icon: 'upload',
      title: 'Create from File',
    },
    {
      routerLink: '/create/clipboard',
      icon: 'clipboard',
      title: 'Create from Clipboard',
    },
    {
      routerLink: '/create/geomarks',
      icon: 'copy',
      title: 'Create from Geomarks',
    },
    {
      routerLink: '/create/kml',
      icon: 'globe-americas',
      title: 'Create in Google Earth',
    },
  ];

  adminHeaderMenuItems: Array<MenuItem> = [
    {
      routerLink: '/secure/admin/geomarks',
      title: 'Geomarks',
    },
    {
      routerLink: '/secure/admin/geomarkGroups',
      title: 'Geomark Groups',
    },
    {
      routerLink: '/secure/admin/configProperties',
      icon: 'cog',
      title: 'Config Properties',
    },
  ];

  get headerMenuItems(): Array<MenuItem> {
    if (this.location.path().startsWith('/secure/admin')) {
      return this.adminHeaderMenuItems;
    } else {
      return this.clientHeaderMenuItems;
    }
  }

  footerMenuItems: Array<MenuItem> = [];

  constructor(
    injector: Injector,
    private router: Router,
    private titleService: Title,
  ) {
    super(injector);
  }

  ngOnInit() {
    this.config.changed.subscribe((config) => {
      if (config != null) {
        for (const label of Object.keys(config.footerLinks)) {
          const menuItem = new MenuItem();
          menuItem.title = label;
          menuItem.routerLink = this.config.footerLinks[label];
          this.footerMenuItems.push(menuItem);
        }
      }
    });

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      map(() => this.route),
      map(route => {
        while (route.firstChild) {
          route = route.firstChild;
        }
        return route;
      }),
      filter(route => route.outlet === 'primary'),
      mergeMap(route => route.data),
    ).subscribe(event => {
      let title = event.title;
      if (!title) {
        title = this.appTitle;
      }
      this.titleService.setTitle(title);
    });
  }

  get title(): string {
    if (this.appTitle) {
      return this.appTitle;
    } else {
      return this.titleService.getTitle();
    }
  }
}
