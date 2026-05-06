import { Component } from "@angular/core";
import { TestBed } from "@angular/core/testing";
import { provideHttpClient } from "@angular/common/http";
import {
  HttpTestingController,
  provideHttpClientTesting,
} from "@angular/common/http/testing";
import { provideRouter } from "@angular/router";
import { AuthService } from "./auth.service";
import { FavoriteService } from "./favorite.service";
import { environment } from "../../../environments/environment";
import { of } from "rxjs";

@Component({ template: "" })
class DummyComponent {}

describe("AuthService", () => {
  let service: AuthService;
  let httpTesting: HttpTestingController;
  let mockFavoriteService: {
    migrateLocalFavoritesToServer: ReturnType<typeof vi.fn>;
    init: ReturnType<typeof vi.fn>;
    reset: ReturnType<typeof vi.fn>;
  };
  const apiUrl = `${environment.apiUrl}/auth`;

  beforeEach(() => {
    mockFavoriteService = {
      migrateLocalFavoritesToServer: vi.fn(() => of(null)),
      init: vi.fn(),
      reset: vi.fn(),
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: "login", component: DummyComponent }]),
        { provide: FavoriteService, useValue: mockFavoriteService },
      ],
    });
    service = TestBed.inject(AuthService);
    httpTesting = TestBed.inject(HttpTestingController);
    sessionStorage.clear();
  });

  afterEach(() => {
    httpTesting.verify();
    sessionStorage.clear();
  });

  describe("getToken", () => {
    it("should return token from sessionStorage", () => {
      sessionStorage.setItem("loete_token", "my-jwt-token");
      expect(service.getToken()).toBe("my-jwt-token");
    });

    it("should return null when no token is stored", () => {
      expect(service.getToken()).toBeNull();
    });
  });

  describe("isAuthenticated", () => {
    it("should return true when token exists", () => {
      sessionStorage.setItem("loete_token", "some-token");
      expect(service.isAuthenticated()).toBe(true);
    });

    it("should return false when no token exists", () => {
      expect(service.isAuthenticated()).toBe(false);
    });
  });

  describe("login", () => {
    it("should store tokens and user on success", () => {
      const mockResponse = {
        accessToken: "access-token",
        refreshToken: "refresh-token",
        userId: "usr1",
        username: "testuser",
        email: "test@test.com",
      };

      service
        .login({ email: "test@test.com", password: "password" })
        .subscribe();

      const req = httpTesting.expectOne(`${apiUrl}/login`);
      expect(req.request.method).toBe("POST");
      req.flush(mockResponse);

      expect(sessionStorage.getItem("loete_token")).toBe("access-token");
      expect(sessionStorage.getItem("loete_refresh_token")).toBe(
        "refresh-token",
      );
      expect(service.currentUser()).toEqual({
        id: "usr1",
        email: "test@test.com",
        username: "testuser",
      });
    });

    it("should trigger favorite migration after login", () => {
      const mockResponse = {
        accessToken: "access-token",
        refreshToken: "refresh-token",
        userId: "usr1",
        username: "testuser",
        email: "test@test.com",
      };

      service
        .login({ email: "test@test.com", password: "password" })
        .subscribe();

      const req = httpTesting.expectOne(`${apiUrl}/login`);
      req.flush(mockResponse);

      expect(
        mockFavoriteService.migrateLocalFavoritesToServer,
      ).toHaveBeenCalled();
    });
  });

  describe("register", () => {
    it("should store tokens and user on success", () => {
      const mockResponse = {
        accessToken: "access-token",
        refreshToken: "refresh-token",
        userId: "usr1",
        username: "testuser",
        email: "test@test.com",
      };

      service
        .register({
          email: "test@test.com",
          username: "testuser",
          password: "password",
        })
        .subscribe();

      const req = httpTesting.expectOne(`${apiUrl}/register`);
      expect(req.request.method).toBe("POST");
      req.flush(mockResponse);

      expect(sessionStorage.getItem("loete_token")).toBe("access-token");
      expect(service.currentUser()?.username).toBe("testuser");
    });
  });

  describe("logout", () => {
    it("should clear storage, currentUser, and reset favorites", () => {
      sessionStorage.setItem("loete_token", "some-token");
      sessionStorage.setItem("loete_refresh_token", "refresh-token");
      sessionStorage.setItem(
        "loete_user",
        JSON.stringify({ id: "u1", email: "t@t.com", username: "u" }),
      );
      service.currentUser.set({
        id: "u1",
        email: "t@t.com",
        username: "u",
      });

      service.logout();

      const req = httpTesting.expectOne(`${apiUrl}/logout`);
      req.flush({});

      expect(sessionStorage.getItem("loete_token")).toBeNull();
      expect(sessionStorage.getItem("loete_refresh_token")).toBeNull();
      expect(sessionStorage.getItem("loete_user")).toBeNull();
      expect(service.currentUser()).toBeNull();
      expect(mockFavoriteService.reset).toHaveBeenCalled();
    });
  });

  describe("refreshAccessToken", () => {
    it("should return error when no refresh token exists", () => {
      let error: Error | null = null;
      service.refreshAccessToken().subscribe({
        error: (e) => (error = e),
      });

      expect(error).toBeTruthy();
      expect(error!.message).toBe("No refresh token");
    });
  });
});
