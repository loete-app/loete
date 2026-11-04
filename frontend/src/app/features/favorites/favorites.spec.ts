import { ComponentFixture, TestBed } from "@angular/core/testing";
import { signal } from "@angular/core";
import { provideRouter } from "@angular/router";
import { of, throwError } from "rxjs";
import { Favorites } from "./favorites";
import { AuthService } from "@/core/services/auth.service";
import { FavoriteService } from "@/core/services/favorite.service";
import { SeoService } from "@/core/services/seo.service";
import { Favorite } from "@/core/models/favorite.model";

describe("Favorites", () => {
  let fixture: ComponentFixture<Favorites>;
  let component: Favorites;
  let element: HTMLElement;

  let mockAuthService: { isAuthenticated: ReturnType<typeof vi.fn> };
  let mockFavoriteService: {
    init: ReturnType<typeof vi.fn>;
    getFavorites: ReturnType<typeof vi.fn>;
    removeFavorite: ReturnType<typeof vi.fn>;
    favoriteIds: ReturnType<typeof signal<Set<string>>>;
  };
  let mockSeoService: { set: ReturnType<typeof vi.fn> };

  const mockFavorites: Favorite[] = [
    {
      id: "fav1",
      eventId: "ev1",
      name: "Rock Festival",
      imageUrl: "https://example.com/img.jpg",
      startDate: "2026-06-15T20:00:00",
      categoryName: "Konzert",
      locationName: "Hallenstadion",
      city: "Zürich",
      createdAt: "2026-05-01T10:00:00",
    },
    {
      id: "fav2",
      eventId: "ev2",
      name: "Football Match",
      imageUrl: null,
      startDate: "2026-07-01T18:00:00",
      categoryName: "Sport",
      locationName: "Letzigrund",
      city: "Zürich",
      createdAt: "2026-05-02T12:00:00",
    },
  ];

  function setup(
    favoritesResponse: Favorite[] | Error = mockFavorites,
    authenticated = true,
  ) {
    mockAuthService = {
      isAuthenticated: vi.fn().mockReturnValue(authenticated),
    };
    mockFavoriteService = {
      init: vi.fn(),
      getFavorites:
        favoritesResponse instanceof Error
          ? vi.fn(() => throwError(() => favoritesResponse))
          : vi.fn(() => of(favoritesResponse)),
      removeFavorite: vi.fn(() => of(undefined)),
      favoriteIds: signal<Set<string>>(new Set()),
    };
    mockSeoService = { set: vi.fn() };

    TestBed.configureTestingModule({
      imports: [Favorites],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: mockAuthService },
        { provide: FavoriteService, useValue: mockFavoriteService },
        { provide: SeoService, useValue: mockSeoService },
      ],
    });

    fixture = TestBed.createComponent(Favorites);
    component = fixture.componentInstance;
    element = fixture.nativeElement;
  }

  it("should create the component", () => {
    setup();
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it("should set SEO on init", () => {
    setup();
    fixture.detectChanges();
    expect(mockSeoService.set).toHaveBeenCalledWith(
      "Meine Favoriten",
      "Deine gespeicherten Events auf Löte.",
    );
  });

  it("should call favoriteService.init on init", () => {
    setup();
    fixture.detectChanges();
    expect(mockFavoriteService.init).toHaveBeenCalled();
  });

  it("should render favorites when loaded", () => {
    setup();
    fixture.detectChanges();

    const rows = element.querySelectorAll(".row");
    expect(rows.length).toBe(2);
  });

  it("should show empty state when no favorites", () => {
    setup([]);
    fixture.detectChanges();

    const empty = element.querySelector(".empty");
    expect(empty).toBeTruthy();
    expect(empty!.textContent).toContain("Noch keine Favoriten");
  });

  it("should show error state on failure", () => {
    setup(new Error("Network error"));
    fixture.detectChanges();

    const error = element.querySelector(".error");
    expect(error).toBeTruthy();
    expect(error!.textContent).toContain(
      "Favoriten konnten nicht geladen werden",
    );
  });

  it("should call removeFavorite when remove button is clicked", () => {
    setup();
    fixture.detectChanges();

    const removeBtn = element.querySelector(".remove") as HTMLButtonElement;
    removeBtn.click();
    fixture.detectChanges();

    expect(mockFavoriteService.removeFavorite).toHaveBeenCalledWith("ev1");
  });

  it("should remove favorite from list after successful remove", () => {
    setup();
    fixture.detectChanges();
    expect(element.querySelectorAll(".row").length).toBe(2);

    const removeBtn = element.querySelector(".remove") as HTMLButtonElement;
    removeBtn.click();
    fixture.detectChanges();

    expect(element.querySelectorAll(".row").length).toBe(1);
  });

  it("should render favorite names", () => {
    setup();
    fixture.detectChanges();

    const names = element.querySelectorAll(".info h3");
    expect(names[0]?.textContent?.trim()).toBe("Rock Festival");
    expect(names[1]?.textContent?.trim()).toBe("Football Match");
  });

  it("should show anonymous banner when not authenticated", () => {
    setup(mockFavorites, false);
    fixture.detectChanges();

    const banner = element.querySelector(".anon-banner");
    expect(banner).toBeTruthy();
    expect(banner!.textContent).toContain("nur in diesem Browser");
  });

  it("should not show anonymous banner when authenticated", () => {
    setup(mockFavorites, true);
    fixture.detectChanges();

    const banner = element.querySelector(".anon-banner");
    expect(banner).toBeNull();
  });
});
