import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideRouter } from "@angular/router";
import { signal } from "@angular/core";
import { of } from "rxjs";
import { EventCard } from "./event-card";
import { FavoriteService } from "@/core/services/favorite.service";
import { Event } from "@/core/models/event.model";

describe("EventCard", () => {
  let fixture: ComponentFixture<EventCard>;
  let element: HTMLElement;
  let mockFavoriteService: {
    favoriteIds: ReturnType<typeof signal<Set<string>>>;
    addFavorite: ReturnType<typeof vi.fn>;
    removeFavorite: ReturnType<typeof vi.fn>;
  };

  const mockEvent: Event = {
    id: "ev1",
    name: "Rock Festival 2026",
    imageUrl: "https://example.com/rock.jpg",
    startDate: "2026-06-15T20:00:00",
    categoryName: "Konzert",
    locationName: "Hallenstadion",
    city: "Zürich",
  };

  beforeEach(async () => {
    mockFavoriteService = {
      favoriteIds: signal<Set<string>>(new Set()),
      addFavorite: vi.fn(() => of({})),
      removeFavorite: vi.fn(() => of(undefined)),
    };

    await TestBed.configureTestingModule({
      imports: [EventCard],
      providers: [
        provideRouter([]),
        {
          provide: FavoriteService,
          useValue: mockFavoriteService,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(EventCard);
    fixture.componentRef.setInput("event", mockEvent);
    fixture.detectChanges();
    element = fixture.nativeElement;
  });

  it("should create the component", () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it("should render event name", () => {
    const h3 = element.querySelector("h3");
    expect(h3?.textContent?.trim()).toBe("Rock Festival 2026");
  });

  it("should render category badge when categoryName is present", () => {
    const badge = element.querySelector(".badge");
    expect(badge).toBeTruthy();
    expect(badge!.textContent?.trim()).toBe("Konzert");
  });

  it("should not render category badge when categoryName is empty", () => {
    fixture.componentRef.setInput("event", {
      ...mockEvent,
      categoryName: "",
    });
    fixture.detectChanges();

    const badge = element.querySelector(".badge");
    expect(badge).toBeNull();
  });

  it("should render city", () => {
    const metaItems = element.querySelectorAll(".meta-item");
    const cityItem = Array.from(metaItems).find((el) =>
      el.textContent?.includes("Zürich"),
    );
    expect(cityItem).toBeTruthy();
  });

  it("should render date", () => {
    const metaItems = element.querySelectorAll(".meta-item");
    expect(metaItems.length).toBeGreaterThan(0);
  });

  it("should use fallback image on error", () => {
    const img = element.querySelector("img") as HTMLImageElement;
    expect(img).toBeTruthy();

    img.dispatchEvent(new window.Event("error"));
    fixture.detectChanges();

    expect(img.src).toContain("placehold.co");
  });

  it("should call addFavorite when not favorited and toggle is clicked", () => {
    mockFavoriteService.favoriteIds.set(new Set());

    const btn = element.querySelector(".fav-btn") as HTMLButtonElement;
    btn.click();
    fixture.detectChanges();

    expect(mockFavoriteService.addFavorite).toHaveBeenCalledWith("ev1");
  });

  it("should call removeFavorite when already favorited and toggle is clicked", () => {
    mockFavoriteService.favoriteIds.set(new Set(["ev1"]));
    fixture.detectChanges();

    const btn = element.querySelector(".fav-btn") as HTMLButtonElement;
    btn.click();
    fixture.detectChanges();

    expect(mockFavoriteService.removeFavorite).toHaveBeenCalledWith("ev1");
  });

  it("should show active state when event is favorited", () => {
    mockFavoriteService.favoriteIds.set(new Set(["ev1"]));
    fixture.detectChanges();

    const btn = element.querySelector(".fav-btn");
    expect(btn?.classList.contains("active")).toBe(true);
  });
});
