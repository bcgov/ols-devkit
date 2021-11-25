import { Observable } from 'rxjs';

export interface SecurityService {
  getUsername(): string;

  hasRole(role: string): boolean;

  hasAnyRole(roles: string[]): boolean;

  hasAnyRoleAsync(roles: string[]): Observable<boolean>;
}
