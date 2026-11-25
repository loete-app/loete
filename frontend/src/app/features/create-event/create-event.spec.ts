import { ComponentFixture, TestBed } from "@angular/core/testing";
import { Router, provideRouter } from "@angular/router";
import { of, throwError } from "rxjs";
import { CreateEvent } from "./create-event";
import { EventService } from "@/core/services/event.service";
import { CategoryService } from "@/core/services/category.service";
import { LocationService } from "@/core/services/location.service";
import { Category } from "@/core/models/category.model";

describe("CreateEvent", () => {
  let fixture: ComponentFixture<CreateEvent>;
  let component: CreateEvent;
  let element: HTMLElement;
  let router: Router;

  const categories: Category[] = [
    { id: 1, name: "Konzert", slug: "konzert" },
    { id: 4, name: "Festival", slug: "festival" },
  ];

  const mockEventService = { createEvent: vi.fn() };
  const mockCategoryService = { getCategories: vi.fn() };
  const mockLocationService = { getCities: vi.fn() };

  /** Liefert einen datetime-local-Wert relativ zu jetzt. */
  const inDays = (days: number, hour = 18): string => {
    const d = new Date();
    d.setDate(d.getDate() + days);
    d.setHours(hour, 0, 0, 0);
    const pad = (n: number) => n.toString().padStart(2, "0");
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:00`;
  };

  /** Füllt alle Pflichtfelder mit gültigen Werten. */
  const fillValid = () => {
    component.name = "  Sommerfest  ";
    component.categoryId = 4;
    component.startDate = inDays(7);
    component.locationName = "Landiwiese";
    component.city = "Zürich";
  };

  beforeEach(async () => {
    vi.clearAllMocks();
    mockCategoryService.getCategories.mockReturnValue(of(categories));
    mockLocationService.getCities.mockReturnValue(of(["Bern", "Zürich"]));

    await TestBed.configureTestingModule({
      imports: [CreateEvent],
      providers: [
        provideRouter([]),
        { provide: EventService, useValue: mockEventService },
        { provide: CategoryService, useValue: mockCategoryService },
        { provide: LocationService, useValue: mockLocationService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(CreateEvent);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, "navigate").mockResolvedValue(true);
    fixture.detectChanges();
    element = fixture.nativeElement;
  });

  it("should render the heading and load categories and cities", () => {
    expect(element.querySelector("h1")?.textContent).toContain(
      "Event erstellen",
    );
    expect(component.categories()).toEqual(categories);
    expect(component.cities()).toEqual(["Bern", "Zürich"]);
    const options = element.querySelectorAll("#category option");
    expect(options.length).toBe(3);
  });

  it("should show field errors and not submit when required fields are empty", () => {
    component.onSubmit();
    fixture.detectChanges();

    const errors = component.fieldErrors();
    expect(errors.name).toBeTruthy();
    expect(errors.categoryId).toBeTruthy();
    expect(errors.startDate).toBeTruthy();
    expect(errors.locationName).toBeTruthy();
    expect(errors.city).toBeTruthy();
    expect(element.querySelectorAll(".field-error").length).toBe(5);
    expect(mockEventService.createEvent).not.toHaveBeenCalled();
  });

  it("should reject a start date in the past", () => {
    fillValid();
    component.startDate = inDays(-1);
    component.onSubmit();

    expect(component.fieldErrors().startDate).toContain("Zukunft");
    expect(mockEventService.createEvent).not.toHaveBeenCalled();
  });

  it("should reject an end date before the start date", () => {
    fillValid();
    component.endDate = inDays(6);
    component.onSubmit();

    expect(component.fieldErrors().endDate).toContain("nach dem Beginn");
    expect(mockEventService.createEvent).not.toHaveBeenCalled();
  });

  it("should reject non-http URLs", () => {
    fillValid();
    component.ticketUrl = "javascript:alert(1)";
    component.imageUrl = "kein link";
    component.onSubmit();

    expect(component.fieldErrors().ticketUrl).toBeTruthy();
    expect(component.fieldErrors().imageUrl).toBeTruthy();
    expect(mockEventService.createEvent).not.toHaveBeenCalled();
  });

  it("should submit a trimmed request and navigate to the new event", () => {
    mockEventService.createEvent.mockReturnValue(of({ id: "new123" }));
    fillValid();
    component.ticketUrl = " https://example.ch/tickets ";

    component.onSubmit();

    expect(mockEventService.createEvent).toHaveBeenCalledWith({
      name: "Sommerfest",
      description: null,
      imageUrl: null,
      ticketUrl: "https://example.ch/tickets",
      startDate: `${inDays(7)}:00`,
      endDate: null,
      categoryId: 4,
      locationName: "Landiwiese",
      city: "Zürich",
    });
    expect(router.navigate).toHaveBeenCalledWith(["/events", "new123"]);
    expect(component.loading()).toBe(false);
  });

  it("should show an error message when the server rejects the request", () => {
    mockEventService.createEvent.mockReturnValue(
      throwError(() => ({ status: 500 })),
    );
    fillValid();

    component.onSubmit();
    fixture.detectChanges();

    expect(component.error()).toContain("nicht erstellt");
    expect(element.querySelector(".error-msg")).toBeTruthy();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
