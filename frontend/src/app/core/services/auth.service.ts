import { Injectable, signal } from "@angular/core";
import { User } from "../models/auth.model";

@Injectable({ providedIn: "root" })
export class AuthService {
  private readonly tokenKey = "sample_token";

  readonly currentUser = signal<User | null>(null);

  getToken(): string | null {
    if (typeof window === "undefined") return null;
    return localStorage.getItem(this.tokenKey);
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.currentUser.set(null);
  }
}
