import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { MatDialog, MatDialogRef } from '@angular/material/dialog';
import { AsyncSubject, Observable, of, throwError } from 'rxjs';
import { catchError, first, map, mergeMap, retryWhen } from 'rxjs/operators';
import { LoginDialogComponent } from 'src/app/base/login-dialog/login-dialog.component';
import { MessageDialogComponent } from 'src/app/base/message-dialog/message-dialog.component';
import { GeomarkConfigService } from 'src/app/service/geomark-config.service';

@Injectable({
  providedIn: 'root'
})
export class HttpService {

  private static loginDialog: MatDialogRef<LoginDialogComponent> = null;

  constructor(
    private dialog: MatDialog,
    private http: HttpClient,
    private config: GeomarkConfigService,
  ) {
  }

  public getUrl(path: string, protocol?: string) {
    return this.config.getUrl({ path, protocol });
  }

  public get<R>(path: string, options?: any): Observable<R> {
    const url = this.getUrl(path);
    return this.httpRequest(
      http => {
        return http.get(url, options);
      },
      json => json
    );
  }

  public delete(path: string): Observable<any> {
    const url = this.getUrl(path);
    return this.httpRequest(
      http => {
        return http.post(url, null, { headers: { 'X-HTTP-Method-Override': 'DELETE' } });
      },
      json => json
    );
  }

  public post<R>(path: string, body: any): Observable<R> {
    const url = this.getUrl(path);
    return this.httpRequest(
      http => {
        return http.post(url, body);
      },
      json => json
    );
  }


  public put<R>(path: string, body: any): Observable<R> {
    const url = this.getUrl(path);
    return this.httpRequest(
      http => {
        return http.put(url, body);
      },
      json => json
    );
  }

  public httpRequest<R>(request: (http: HttpClient) => Observable<any>, handler: (response: any) => R): Observable<R> {
    const response = request(this.http);
    return response.pipe(
      map(handler), //
      retryWhen(errors => {
        return errors.pipe(
          mergeMap((error: HttpErrorResponse, i) => {
            console.log(error);
            if (i === 0) {
              if (error.status === 403 || error.status === 0) {
                let loginDialog = HttpService.loginDialog;
                if (loginDialog) {
                  loginDialog.componentInstance.login();
                } else {
                  loginDialog = this.dialog.open(LoginDialogComponent, {
                    disableClose: true
                  });
                  HttpService.loginDialog = loginDialog;
                  loginDialog.afterClosed().subscribe(dialogResponse => {
                    HttpService.loginDialog = null;
                  });
                }
                const retrySubject = new AsyncSubject<boolean>();
                loginDialog.afterClosed().subscribe((_) => {
                  retrySubject.next(true);
                  retrySubject.complete();
                });
                return retrySubject;
              } else {
                return throwError(error);
              }
            } else {
              return throwError(error);
            }
          })
        );
      }),
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404) {
          return of(null);
        } else {
          this.dialog.open(MessageDialogComponent, {
            data: {
              title: 'Error',
              message: error.message,
            }
          });
          return throwError(error.message);
        }
      }),
      first(),
      map((result: any) => {
        if (result?.rest_api_error) {
          this.dialog.open(MessageDialogComponent, {
            data: {
              title: 'Error',
              message: result.rest_api_error,
            }
          });
          return null;
        } else {
          return result;
        }
      }),
    );
  }
}
