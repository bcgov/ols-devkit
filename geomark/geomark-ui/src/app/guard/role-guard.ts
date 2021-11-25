import { Observable } from 'rxjs';
import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
} from '@angular/router';
import { AuthService } from '../service/auth.service';

@Injectable()
export class RoleGuard implements CanActivate {
  constructor(
    private authService: AuthService,
  ) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
  ): Observable<boolean> | boolean {
    if (route.data) {
      const roles: string[] = route.data.roles;
      if (roles) {
        return this.authService.hasAnyRoleAsync(roles);
      }
    }
    return true;
  }
}
