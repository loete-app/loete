import { ComponentFixture, TestBed } from "@angular/core/testing";
import { signal } from "@angular/core";
import { ActivatedRoute, Router, convertToParamMap } from "@angular/router";
import { of, throwError } from "rxjs";
import { Home } from "./home";
import { EventService } from "@/core/services/event.service";
import { FavoriteService } from "@/core/services/favorite.service";
import { SeoService } from "@/core/services/seo.service";
import { CategoryService } from "@/core/services/category.service";
import { LocationService } from "@/core/services/location.service";
import { Event, PagedResponse } from "@/core/models/event.model";

describe("Home", () => {
  let fixture: ComponentFixture<Home>;
  let component: Home;
  let element: HTMLElement;

  let mockEventService: { getEvents: ReturnType<typeof vi.fn> };
  let mockFavoriteService: {
    init: ReturnType<typeof vi.fn>;
    favoriteIds: ReturnType<typeof signal<Set<string>>>;
    addFavorite: ReturnType<typeof vi.fn>;
    removeFavorite: ReturnType<typeof vi.fn>;
  };
  let mockSeoService: { set: ReturnType<typeof vi.fn> };
  let mockCategoryService: {
    getCategories: ReturnType<typeof vi.fn>;
  };
  let mockLocationService: { getCities: ReturnType<typeof vi.fn> };

  const mockEvents: Event[] = [
    {
      id: "ev1",
      name: "Rock Festival",
      imageUrl: "https://example.com/img.jpg",
      startDate: "2026-06-15T20:00:00",
      categoryName: "Konzert",
      locationName: "Hallenstadion",
      city: "Zürich",
    },
    {
      id: "ev2",
      name: "Football Match",
      imageUrl: "https://example.com/img2.jpg",
      startDate: "2026-07-01T18:00:00",
      categoryName: "Sport",
      locationName: "Letzigrund",
      city: "Zürich",
    },
  ];

  const mockPagedResponse: PagedResponse<Event> = {
    content: mockEvents,
    page: 0,
    size: 20,
    totalElements: 2,
    totalPages: 1,
    last: true,
  };

  function setup(
    eventsResponse: PagedResponse<Event> | Error = mockPagedResponse,
  ) {
    mockEventService = {
      getEvents:
        eventsResponse instanceof Error
          ? vi.fn(() => throwError(() => eventsResponse))
          : vi.fn(() => of(eventsResponse)),
    };
    mockFavoriteService = {
      init: vi.fn(),
      favoriteIds: signal<Set<string>>(new Set()),
      addFavorite: vi.fn(() => of({})),
      removeFavorite: vi.fn(() => of(undefined)),
    };
    mockSeoService = { set: vi.fn() };
    mockCategoryService = {
      getCategories: vi.fn(() => of([])),
    };
    mockLocationService = { getCities: vi.fn(() => of([])) };

    TestBed.configureTestingModule({
      imports: [Home],
      providers: [
        { provide: EventService, useValue: mockEventService },
        {
          provide: FavoriteService,
          useValue: mockFavoriteService,
        },
        { provide: SeoService, useValue: mockSeoService },
        {
          provide: CategoryService,
          useValue: mockCategoryService,
        },
        {
          provide: LocationService,
          useValue: mockLocationService,
        },
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: convertToParamMap({}),
            },
          },
        },
        {
          provide: Router,
          useValue: {
            navigate: vi.fn(),
            createUrlTree: vi.fn(),
            serializeUrl: vi.fn(),
          },
        },
      ],
    });

    fixture = TestBed.createComponent(Home);
    component = fixture.componentInstance;
    element = fixture.nativeElement;
  }

  it("should create the component", () => {
    setup();
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it("should set SEO title and description on init", () => {
    setup();
    fixture.detectChanges();
    expect(mockSeoService.set).toHaveBeenCalledWith(
      "Events entdecken",
      "Entdecke Konzerte, Festivals, Sport und mehr in der Schweiz – an einem Ort.",
    );
  });

  it("should init favorites on init", () => {
    setup();
    fixture.detectChanges();
    expect(mockFavoriteService.init).toHaveBeenCalled();
  });

  it("should render events when loaded", () => {
    setup();
    fixture.detectChanges();

    const cards = element.querySelectorAll("app-event-card");
    expect(cards.length).toBe(2);
  });

  it("should show empty state when no events", () => {
    setup({
      content: [],
      page: 0,
      size: 20,
      totalElements: 0,
      totalPages: 0,
      last: true,
    });
    fixture.detectChanges();

    const empty = element.querySelector(".empty");
    expect(empty).toBeTruthy();
    expect(empty!.textContent).toContain("Keine Events gefunden");
  });

  it("should show error state on failure", () => {
    setup(new Error("Network error"));
    fixture.detectChanges();

    const error = element.querySelector(".error");
    expect(error).toBeTruthy();
    expect(error!.textContent).toContain("Events konnten nicht geladen werden");
  });

  it("should call EventService.getEvents on init", () => {
    setup();
    fixture.detectChanges();
    expect(mockEventService.getEvents).toHaveBeenCalled();
  });

  it("should reload events when reload is called after error", () => {
    setup(new Error("fail"));
    fixture.detectChanges();
    expect(element.querySelector(".error")).toBeTruthy();

    mockEventService.getEvents.mockReturnValue(of(mockPagedResponse));
    component.reload();
    fixture.detectChanges();

    expect(element.querySelector(".error")).toBeNull();
    expect(element.querySelectorAll("app-event-card").length).toBe(2);
  });
});
