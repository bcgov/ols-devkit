import { CdkTableModule } from '@angular/cdk/table';
import { HttpClientModule } from '@angular/common/http';
import { NgModule } from '@angular/core';
import {
  FormsModule,
  ReactiveFormsModule
} from '@angular/forms';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule } from '@angular/material/dialog';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatPaginatorModule } from '@angular/material/paginator';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSortModule } from '@angular/material/sort';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { FaIconLibrary, FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { AdminConfigPropertyDialogComponent } from './admin/config-property/dialog/admin-config-property-dialog.component';
import { AdminConfigPropertyListComponent } from './admin/config-property/list/admin-config-property-list.component';
import { AdminGeomarkGroupAllComponent } from './admin/geomark-group/all/admin-geomark-group-all.component';
import { AdminGeomarkGroupDialogComponent } from './admin/geomark-group/dialog/admin-geomark-group-dialog.component';
import { AdminGeomarkGroupHomeComponent } from './admin/geomark-group/home/admin-geomark-group-home.component';
import { AdminGeomarkGroupReportComponent } from './admin/geomark-group/report/admin-geomark-group-report.component';
import { AdminGeomarkGroupViewComponent } from './admin/geomark-group/view/admin-geomark-group-view.component';
import { GeomarksExpiredComponent } from './admin/geomark/expired/geomarks-expired.component';
import { GeomarksHomeComponent } from './admin/geomark/home/geomarks-home.component';
import { GeomarksListComponent } from './admin/geomark/list/geomarks-list.component';
import { GeomarksTableComponent } from './admin/geomark/table/geomarks-table.component';
import { GeomarksTemporaryComponent } from './admin/geomark/temporary/geomarks-temporary.component';
import { GeomarksViewComponent } from './admin/geomark/view/geomarks-view.component';
import { AdminHomeComponent } from './admin/home/admin-home.component';
import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { ConfirmDialogComponent } from './base/confirm-dialog/confirm-dialog.component';
import { CopyClipboardComponent } from './base/copy-clipboard/copy-clipboard.component';
import { FaIconComponent } from './base/fa-icon.component';
import { DragDropDirective } from './base/file-upload/drap-drop-directive';
import { FileUploadComponent } from './base/file-upload/file-upload.component';
import { LoginDialogComponent } from './base/login-dialog/login-dialog.component';
import { MessageDialogComponent } from './base/message-dialog/message-dialog.component';
import { PageNotFoundComponent } from './base/page-not-found.component';
import { BufferSettingsComponent } from './client/buffer-settings/buffer-settings.component';
import { CreateClipboardComponent } from './client/create-clipboard/create-clipboard.component';
import { CreateFileComponent } from './client/create-file/create-file.component';
import { CreateGeomarksComponent } from './client/create-geomarks/create-geomarks.component';
import { CreateGoogleEarthComponent } from './client/create-google-earth/create-google-earth.component';
import { FormatCsComponent } from './client/format-cs/format-cs.component';
import { GeomarkChipComponent } from './client/geomark-chip/geomark-chip.component';
import { GeomarkDownloadComponent } from './client/geomark-download/geomark-download.component';
import { GeomarkInfoComponent } from './client/geomark-info/geomark-info.component';
import { GeomarkMapComponent } from './client/geomark-map/geomark-map.component';
import { GeomarkViewComponent } from './client/geomark-view/geomark-view.component';
import { GeometryTypeComponent } from './client/geometry-type/geometry-type.component';
import { OverviewComponent } from './client/overview/overview.component';
import { RoleGuard } from './guard/role-guard';
import { HelpButtonComponent } from './help-button/help-button.component';
import { initIconLibrary } from './IconLibrary';
import { PageTemplateComponent } from './page-template/page-template.component';
import { GeomarkGroupResolver } from './resolver/geomark-group.resolver';
import { GeomarkResolver } from './resolver/geomark.resolver';
import { AuthService } from './service/auth.service';
import { GeomarkConfigService } from './service/geomark-config.service';


@NgModule({
  declarations: [
    AppComponent,

    FaIconComponent,

    OverviewComponent,
    CreateFileComponent,
    CreateClipboardComponent,
    CreateGeomarksComponent,
    CreateGoogleEarthComponent,
    BufferSettingsComponent,
    FormatCsComponent,
    GeometryTypeComponent,
    GeomarkInfoComponent,
    GeomarkViewComponent,
    GeomarkDownloadComponent,
    GeomarkMapComponent,
    CopyClipboardComponent,
    GeomarkChipComponent,
    HelpButtonComponent,
    PageTemplateComponent,

    DragDropDirective,
    FileUploadComponent,
    GeomarksHomeComponent,
    GeomarksListComponent,
    GeomarksExpiredComponent,
    GeomarksTemporaryComponent,
    GeomarksViewComponent,
    AdminConfigPropertyListComponent,
    GeomarksTableComponent,
    LoginDialogComponent,
    MessageDialogComponent,
    PageNotFoundComponent,
    AdminGeomarkGroupHomeComponent,
    AdminGeomarkGroupAllComponent,
    AdminGeomarkGroupReportComponent,
    ConfirmDialogComponent,
    AdminGeomarkGroupViewComponent,
    AdminHomeComponent,
    AdminConfigPropertyDialogComponent,
    AdminGeomarkGroupDialogComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,

    CdkTableModule,

    MatAutocompleteModule,
    MatBadgeModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatChipsModule,
    MatDialogModule,
    MatExpansionModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatListModule,
    MatMenuModule,
    MatPaginatorModule,
    MatSelectModule,
    MatSortModule,
    MatProgressSpinnerModule,
    MatTabsModule,
    MatTableModule,
    MatToolbarModule,
    MatTooltipModule,

    FontAwesomeModule,
  ],
  providers: [
    GeomarkConfigService,
    AuthService,
    RoleGuard,
    GeomarkResolver,
    GeomarkGroupResolver,
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor(library: FaIconLibrary) {
    initIconLibrary(library);
  }
}
