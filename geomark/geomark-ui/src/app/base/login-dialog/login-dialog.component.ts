import { Component, OnInit, AfterViewInit, Inject } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { GeomarkConfigService } from 'src/app/service/geomark-config.service';

@Component({
  selector: 'app-login-dialog',
  templateUrl: './login-dialog.component.html',
  styleUrls: ['./login-dialog.component.css']
})
export class LoginDialogComponent implements AfterViewInit {
  private loginWindow: Window;

  constructor(
    private config: GeomarkConfigService,
    public dialog: MatDialogRef<LoginDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any,
  ) {
  }

  ngAfterViewInit() {
    this.login();
  }

  login() {
    const window = document.defaultView;
    if (this.loginWindow && !this.loginWindow.closed) {
      this.loginWindow.focus();
    } else {
      let width = window.outerWidth * 0.9;
      if (width > 800) {
        width = 800;
      }
      const x = window.outerWidth / 2 + window.screenX - (width / 2);
      const y = window.outerHeight / 2 + window.screenY - (300);

      this.loginWindow = window.open(
        this.config.getUrl({ path: '/secure/login/window' }),
        'geomarkLogin',
        `menubar=no,location=no,status=no,left=${x},top=${y},width=${width},height=600`
      );
    }
    if (this.loginWindow) {
      const listener = (event: any) => {
        if (event.data === 'close') {
          this.dialog.close('Login');
          if (this.loginWindow) {
            this.loginWindow.close();
            this.loginWindow = null;
          }
          window.removeEventListener('message', listener);
        }
      };
      window.addEventListener('message', listener);
    }
  }
}
