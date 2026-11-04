import { TestBed } from "@angular/core/testing";
import { provideHttpClient } from "@angular/common/http";
import {
  HttpTestingController,
  provideHttpClientTesting,
} from "@angular/common/http/testing";
import { FavoriteService } from "./favorite.service";
import { AuthService } from "./auth.service";
import { environment } from "../../../environments/environment";

describe("FavoriteService", () => {
  let service: FavoriteService;
  let httpTesting: HttpTestingController;
  let mockAuthService: { isAuthenticated: ReturnType<typeof vi.fn> };
  const apiUrl = `${environment.apiUrl}/favorites`;

  beforeEach(() => {
    mockAuthService = {
      isAuthenticated: vi.fn().mockReturnValue(false),
    };

    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: mockAuthService },
      ],
    });
    service = TestBed.inject(FavoriteService);
    httpTesting = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  describe("init (anonymous)", () => {
    it("should load from localStorage when not authenticated", () => {
      localStorage.setItem("loete_favorites", JSON.stringify(["ev1", "ev2"]));
      service.init();
      expect(service.favoriteIds()).toEqual(new Set(["ev1", "ev2"]));
    });

    it("should set empty set when localStorage is empty", () => {
      service.init();
      expect(service.favoriteIds()).toEqual(new Set());
    });
  });

  describe("init (authenticated)", () => {
    it("should fetch from server when authenticated", () => {
      mockAuthService.isAuthenticated.mockReturnValue(true);
      service.init();

      const req = httpTesting.expectOne(`${apiUrl}/ids`);
      expect(req.request.method).toBe("GET");
      req.flush(["id1", "id2"]);

      expect(service.favoriteIds()).toEqual(new Set(["id1", "id2"]));
    });

    it("should set empty set on server error", () => {
      mockAuthService.isAuthenticated.mockReturnValue(true);
      service.init();

      const req = httpTesting.expectOne(`${apiUrl}/ids`);
      req.flush("Error", { status: 500, statusText: "Server Error" });

      expect(service.favoriteIds()).toEqual(new Set());
    });
  });

  describe("isFavorite", () => {
    it("should return true when id is in the set", () => {
      service.favoriteIds.set(new Set(["ev1", "ev2"]));
      expect(service.isFavorite("ev1")).toBe(true);
    });

    it("should return false when id is not in the set", () => {
      service.favoriteIds.set(new Set(["ev1", "ev2"]));
      expect(service.isFavorite("ev3")).toBe(false);
    });
  });

  describe("addFavorite (anonymous)", () => {
    it("should add to signal and localStorage", () => {
      service.favoriteIds.set(new Set(["existing"]));
      service.addFavorite("newEvent").subscribe();

      expect(service.favoriteIds().has("newEvent")).toBe(true);
      expect(service.favoriteIds().has("existing")).toBe(true);

      const stored = JSON.parse(
        localStorage.getItem("loete_favorites") || "[]",
      );
      expect(stored).toContain("newEvent");
    });
  });

  describe("addFavorite (authenticated)", () => {
    it("should make POST request and add id to signal", () => {
      mockAuthService.isAuthenticated.mockReturnValue(true);
      service.favoriteIds.set(new Set(["existing"]));

      service.addFavorite("newEvent").subscribe();

      const req = httpTesting.expectOne(`${apiUrl}/newEvent`);
      expect(req.request.method).toBe("POST");
      req.flush({
        id: "fav1",
        eventId: "newEvent",
        name: "New Event",
      });

      expect(service.favoriteIds().has("newEvent")).toBe(true);
      expect(service.favoriteIds().has("existing")).toBe(true);
    });
  });

  describe("removeFavorite (anonymous)", () => {
    it("should remove from signal and localStorage", () => {
      service.favoriteIds.set(new Set(["ev1", "ev2"]));
      service.removeFavorite("ev1").subscribe();

      expect(service.favoriteIds().has("ev1")).toBe(false);
      expect(service.favoriteIds().has("ev2")).toBe(true);
    });
  });

  describe("removeFavorite (authenticated)", () => {
    it("should make DELETE request and remove id from signal", () => {
      mockAuthService.isAuthenticated.mockReturnValue(true);
      service.favoriteIds.set(new Set(["ev1", "ev2"]));

      service.removeFavorite("ev1").subscribe();

      const req = httpTesting.expectOne(`${apiUrl}/ev1`);
      expect(req.request.method).toBe("DELETE");
      req.flush(null);

      expect(service.favoriteIds().has("ev1")).toBe(false);
      expect(service.favoriteIds().has("ev2")).toBe(true);
    });
  });

  describe("migrateLocalFavoritesToServer", () => {
    it("should send local favorites to server and clear localStorage", () => {
      localStorage.setItem("loete_favorites", JSON.stringify(["ev1", "ev2"]));

      service.migrateLocalFavoritesToServer().subscribe();

      const migrateReq = httpTesting.expectOne(`${apiUrl}/migrate`);
      expect(migrateReq.request.method).toBe("POST");
      expect(migrateReq.request.body).toEqual({ eventIds: ["ev1", "ev2"] });
      migrateReq.flush({ migrated: ["ev1", "ev2"], skipped: [] });

      // loadFromServer() fires after migration success
      const idsReq = httpTesting.expectOne(`${apiUrl}/ids`);
      idsReq.flush(["ev1", "ev2"]);

      expect(localStorage.getItem("loete_favorites")).toBeNull();
    });

    it("should no-op when localStorage is empty", () => {
      service.migrateLocalFavoritesToServer().subscribe();
      httpTesting.expectNone(`${apiUrl}/migrate`);
    });
  });

  describe("reset", () => {
    it("should clear the signal", () => {
      service.favoriteIds.set(new Set(["ev1"]));
      service.reset();
      expect(service.favoriteIds()).toEqual(new Set());
    });
  });
});
