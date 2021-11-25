import {
  Injectable
} from '@angular/core';
import {
  Observable,
  of, ReplaySubject
} from 'rxjs';
import { map } from 'rxjs/operators';
import { HttpService } from './http.service';

@Injectable()
export class AuthService {
  username: string;

  roles: string[];

  rolesSubject = new ReplaySubject<string[]>(0);

  observable: Observable<any>;

  constructor(
    private httpService: HttpService,
  ) {
    this.httpService.get<any>('/secure/api/authentication').subscribe(((result) => {
      if (result) {
        this.roles = result.roles;
        this.username = result.name;
      } else {
        this.roles = [];
        this.username = null;
      }
      this.rolesSubject.next(this.roles);
      return result;
    }));
  }

  hasRole(role: string): boolean {
    if (this.roles == null) {
      return false;
    } else {
      return this.roles.indexOf(role) !== -1;
    }
  }

  hasAnyRole(roles: string[]): boolean {
    if (this.roles == null) {
      return true;
    } else {
      for (const role of roles) {
        if (this.roles.indexOf(role) !== -1) {
          return true;
        }
      }
      return false;
    }
  }

  hasAnyRoleAsync(roles: string[]): Observable<boolean> {
    if (this.roles == null) {
      return this.rolesSubject.pipe(
        map((_) => this.hasAnyRole(roles)),
      );
    } else {
      return of(this.hasAnyRole(roles));
    }
  }
}
