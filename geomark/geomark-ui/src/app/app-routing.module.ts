import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';

import { GeomarkViewComponent } from './client/geomark-view/geomark-view.component';
import { OverviewComponent } from './client/overview/overview.component';
import { CreateGoogleEarthComponent } from './client/create-google-earth/create-google-earth.component';
import { CreateGeomarksComponent } from './client/create-geomarks/create-geomarks.component';
import { CreateClipboardComponent } from './client/create-clipboard/create-clipboard.component';
import { CreateFileComponent } from './client/create-file/create-file.component';
import { GeomarksHomeComponent } from './admin/geomark/home/geomarks-home.component';
import { GeomarksListComponent } from './admin/geomark/list/geomarks-list.component';
import { GeomarksExpiredComponent } from './admin/geomark/expired/geomarks-expired.component';
import { GeomarksTemporaryComponent } from './admin/geomark/temporary/geomarks-temporary.component';
import { RoleGuard } from './guard/role-guard';
import { PageNotFoundComponent } from './base/page-not-found.component';
import { AdminConfigPropertyListComponent } from './admin/config-property/list/admin-config-property-list.component';
import { AdminGeomarkGroupHomeComponent } from './admin/geomark-group/home/admin-geomark-group-home.component';
import { AdminGeomarkGroupAllComponent } from './admin/geomark-group/all/admin-geomark-group-all.component';
import { AdminGeomarkGroupReportComponent } from './admin/geomark-group/report/admin-geomark-group-report.component';
import { AdminGeomarkGroupViewComponent } from './admin/geomark-group/view/admin-geomark-group-view.component';
import { GeomarkInfoComponent } from './client/geomark-info/geomark-info.component';
import { GeomarkDownloadComponent } from './client/geomark-download/geomark-download.component';
import { GeomarkMapComponent } from './client/geomark-map/geomark-map.component';
import { GeomarkResolver } from './resolver/geomark.resolver';
import { AdminHomeComponent } from './admin/home/admin-home.component';
import { GeomarksViewComponent } from './admin/geomark/view/geomarks-view.component';
import { GeomarkGroupResolver } from './resolver/geomark-group.resolver';

const routes: Routes = [
  { path: '', redirectTo: 'overview', pathMatch: 'full' },
  { path: 'geomarks', redirectTo: 'overview', pathMatch: 'full' },
  { path: 'create', redirectTo: 'overview', pathMatch: 'full' },
  { path: 'overview', component: OverviewComponent, data: { title: 'Geomark web service overview' } },
  { path: 'create/file', component: CreateFileComponent, data: { title: 'Create Geomark from file' } },
  { path: 'create/clipboard', component: CreateClipboardComponent, data: { title: 'Create Geomark from clipboard' } },
  { path: 'create/geomarks', component: CreateGeomarksComponent, data: { title: 'Create Geomark from geomarks' } },
  { path: 'create/kml', component: CreateGoogleEarthComponent, data: { title: 'Create Geomark in Google Earth' } },
  {
    path: 'geomarks/:geomarkId', component: GeomarkViewComponent, children: [
      { path: '', redirectTo: 'info', pathMatch: 'full' },
      { path: 'info', component: GeomarkInfoComponent, data: { title: 'Geomark Info' }, },
      { path: 'download', component: GeomarkDownloadComponent, data: { title: 'Geomark Download' }, },
      { path: 'map', component: GeomarkMapComponent, data: { title: 'Geomark Map' }, },
    ], resolve: { geomark: GeomarkResolver },
  },

  // ADMIN
  {
    path: 'secure/admin', component: AdminHomeComponent, children: [
      { path: '', redirectTo: 'geomarks', pathMatch: 'full' },
      {
        path: 'geomarks',
        component: GeomarksHomeComponent,
        children: [
          { path: '', redirectTo: 'all', pathMatch: 'full' },
          { path: 'all', component: GeomarksListComponent, },
          { path: 'expired', component: GeomarksExpiredComponent, },
          { path: 'temporary', component: GeomarksTemporaryComponent, },
        ], canActivate: [RoleGuard], data: { roles: ['admin'] }
      },
      {
        path: 'geomarks/:geomarkId', component: GeomarksViewComponent,
        pathMatch: 'full', canActivate: [RoleGuard], data: { roles: ['admin'] },
        resolve: { geomark: GeomarkResolver },
      },
      {
        path: 'geomarkGroups',
        component: AdminGeomarkGroupHomeComponent,
        children: [
          { path: '', redirectTo: 'all', pathMatch: 'full' },
          { path: 'all', component: AdminGeomarkGroupAllComponent, },
          { path: 'report', component: AdminGeomarkGroupReportComponent, },
        ], canActivate: [RoleGuard], data: { roles: ['admin'] }
      },
      {
        path: 'geomarkGroups/:geomarkGroupId', component: AdminGeomarkGroupViewComponent,
        pathMatch: 'full', canActivate: [RoleGuard], data: {
          roles: ['admin']
        }, resolve: { geomarkGroup: GeomarkGroupResolver }
      },
      {
        path: 'configProperties', component: AdminConfigPropertyListComponent,
        pathMatch: 'full', canActivate: [RoleGuard], data: { roles: ['admin'] }
      },
    ], data: {
      admin: true,
    }
  },
  { path: '**', component: PageNotFoundComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { relativeLinkResolution: 'legacy' })],
  exports: [RouterModule]
})
export class AppRoutingModule {

}
