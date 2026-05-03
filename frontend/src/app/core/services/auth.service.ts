import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import {
  CredentialsDto,
  AuthTokenDto,
  PatientRegistrationDto,
  UserDto,
  DecodedToken,
} from '../models/auth.models';
import { Observable, BehaviorSubject, throwError } from 'rxjs';
import { tap, catchError } from 'rxjs/operators';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = environment.apiUrl;
  private readonly tokenKey = 'auth_token';
  private readonly userKey = 'current_user';

  isAuthenticated$ = new BehaviorSubject<boolean>(this.isBrowser() ? this.hasToken() : false);
  currentUser$ = new BehaviorSubject<UserDto | null>(this.isBrowser() ? this.loadCurrentUser() : null);
  currentRole$ = new BehaviorSubject<string | null>(this.isBrowser() ? this.loadCurrentRole() : null);

  constructor(
    private http: HttpClient,
    private router: Router
  ) {}

  /**
   * Login with email and password
   */
  login(credentials: CredentialsDto): Observable<AuthTokenDto> {
    return this.http
      .post<AuthTokenDto>(`${this.apiUrl}/auth/login`, credentials)
      .pipe(
        tap((response) => {
          this.storeToken(response.token);
          this.decodeAndStoreUser(response.token);
          this.isAuthenticated$.next(true);
          this.currentRole$.next(this.loadCurrentRole());
        }),
        catchError((error) => this.handleError(error))
      );
  }

  /**
   * Register a new patient
   */
  registerPatient(
    registration: PatientRegistrationDto
  ): Observable<string> {
    return this.http
      .post(`${this.apiUrl}/patients`, registration, {
        responseType: 'text'
      })
      .pipe(
        catchError((error) => this.handleError(error))
      );
  }

  /**
   * Logout the current user
   */
  logout(): void {
    this.clearToken();
    this.currentUser$.next(null);
    this.currentRole$.next(null);
    this.isAuthenticated$.next(false);
    this.router.navigate(['/']);
  }

  /**
   * Get the stored JWT token
   */
  getToken(): string | null {
    return this.isBrowser() ? localStorage.getItem(this.tokenKey) : null;
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return this.hasToken();
  }

  /**
   * Get the current user's role
   */
  getRole(): string | null {
    return this.currentRole$.value;
  }

  /**
   * Check if user has a specific role
   */
  hasRole(role: string): boolean {
    return this.currentRole$.value === role;
  }

  /**
   * Get the current user ID
   */
  getUserId(): number | null {
    const user = this.currentUser$.value;
    return user ? user.id : null;
  }

  getPatientId(): number | null {
    const user = this.currentUser$.value;
    return user?.patientId ?? null;
  }

  // ============ Private helpers ============

  private isBrowser(): boolean {
    return typeof window !== 'undefined' && typeof localStorage !== 'undefined';
  }

  private hasToken(): boolean {
    return this.isBrowser() && !!localStorage.getItem(this.tokenKey);
  }

  private storeToken(token: string): void {
    if (this.isBrowser()) {
      localStorage.setItem(this.tokenKey, token);
    }
  }

  private clearToken(): void {
    if (this.isBrowser()) {
      localStorage.removeItem(this.tokenKey);
      localStorage.removeItem(this.userKey);
    }
  }

  private loadCurrentUser(): UserDto | null {
    if (!this.isBrowser()) return null;
    const storedUser = localStorage.getItem(this.userKey);
    if (storedUser) {
      try {
        return JSON.parse(storedUser);
      } catch (error) {
        console.warn('Clearing invalid stored user payload:', error);
        localStorage.removeItem(this.userKey);
      }
    }

    const token = this.getToken();
    if (!token) return null;

    try {
      const decoded = this.decodeToken(token);
      const user: UserDto = {
        id: decoded.userId,
        email: decoded.sub,
        role: decoded.role as any,
        active: true,
        patientId: decoded.patientId ?? undefined,
      };
      localStorage.setItem(this.userKey, JSON.stringify(user));
      return user;
    } catch (error) {
      console.warn('Unable to recover user from token:', error);
      localStorage.removeItem(this.tokenKey);
      return null;
    }
  }

  private loadCurrentRole(): string | null {
    const token = this.getToken();
    if (!token) return null;
    try {
      const decoded = this.decodeToken(token);
      return decoded.role || null;
    } catch {
      return null;
    }
  }

  private decodeAndStoreUser(token: string): void {
    try {
      const decoded = this.decodeToken(token);
      const user: UserDto = {
        id: decoded.userId,
        email: decoded.sub,
        role: decoded.role as any,
        active: true,
        patientId: decoded.patientId ?? undefined,
      };
      localStorage.setItem(this.userKey, JSON.stringify(user));
      this.currentUser$.next(user);
    } catch (error) {
      console.error('Failed to decode token:', error);
      this.clearToken();
    }
  }

  private decodeToken(token: string): DecodedToken {
    const parts = token.split('.');
    if (parts.length !== 3) {
      throw new Error('Invalid token');
    }

    const payload = parts[1].replace(/-/g, '+').replace(/_/g, '/');
    const decodedPayload =
      typeof atob === 'function'
        ? atob(payload)
        : Buffer.from(payload, 'base64').toString('utf-8');

    try {
      return JSON.parse(decodedPayload);
    } catch (error) {
      throw new Error('Invalid token payload');
    }
  }

  private handleError(error: HttpErrorResponse) {
    let errorMessage = 'An unknown error occurred';

    if (error.error instanceof ErrorEvent) {
      // Client-side error
      errorMessage = error.error.message;
    } else {
      // Server-side error
      errorMessage =
        error.error?.message ||
        error.error?.error ||
        `HTTP Error: ${error.status}`;
    }

    console.error(errorMessage);
    return throwError(() => new Error(errorMessage));
  }
}
