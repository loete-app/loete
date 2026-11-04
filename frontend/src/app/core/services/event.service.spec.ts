import { TestBed } from "@angular/core/testing";
import { provideHttpClient } from "@angular/common/http";
import {
  HttpTestingController,
  provideHttpClientTesting,
} from "@angular/common/http/testing";
import { EventService } from "./event.service";
import { environment } from "../../../environments/environment";
import { EventFilter, PagedResponse, Event } from "../models/event.model";

describe("EventService", () => {
  let service: EventService;
  let httpTesting: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/events`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(EventService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  describe("getEvents", () => {
    const mockResponse: PagedResponse<Event> = {
      content: [
        {
          id: "abc123",
          name: "Test Event",
          imageUrl: "https://example.com/img.jpg",
          startDate: "2026-06-01T20:00:00",
          categoryName: "Konzert",
          locationName: "Hallenstadion",
          city: "Zürich",
        },
      ],
      page: 0,
      size: 20,
      totalElements: 1,
      totalPages: 1,
      last: true,
    };

    it("should make GET request with correct params when filter is provided", () => {
      const filter: EventFilter = {
        page: 1,
        size: 10,
        categoryId: 5,
        city: "Zürich",
        dateFrom: "2026-06-01T00:00:00",
        dateTo: "2026-06-30T23:59:59",
        search: "Konzert",
      };

      service.getEvents(filter).subscribe((res) => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpTesting.expectOne(
        (r) => r.url === apiUrl && r.method === "GET",
      );
      expect(req.request.params.get("page")).toBe("1");
      expect(req.request.params.get("size")).toBe("10");
      expect(req.request.params.get("categoryId")).toBe("5");
      expect(req.request.params.get("city")).toBe("Zürich");
      expect(req.request.params.get("dateFrom")).toBe("2026-06-01T00:00:00");
      expect(req.request.params.get("dateTo")).toBe("2026-06-30T23:59:59");
      expect(req.request.params.get("search")).toBe("Konzert");

      req.flush(mockResponse);
    });

    it("should make GET request with no params when filter is empty", () => {
      service.getEvents().subscribe((res) => {
        expect(res).toEqual(mockResponse);
      });

      const req = httpTesting.expectOne(apiUrl);
      expect(req.request.method).toBe("GET");
      expect(req.request.params.keys().length).toBe(0);

      req.flush(mockResponse);
    });

    it("should skip null and undefined filter fields", () => {
      const filter: EventFilter = {
        page: 0,
        city: null,
        search: null,
      };

      service.getEvents(filter).subscribe();

      const req = httpTesting.expectOne(
        (r) => r.url === apiUrl && r.method === "GET",
      );
      expect(req.request.params.get("page")).toBe("0");
      expect(req.request.params.has("city")).toBe(false);
      expect(req.request.params.has("search")).toBe(false);

      req.flush(mockResponse);
    });
  });

  describe("getEvent", () => {
    it("should make GET request with correct URL", () => {
      const mockDetail = {
        id: "abc123",
        name: "Test Event",
        description: "A great event",
        imageUrl: null,
        ticketUrl: null,
        startDate: "2026-06-01T20:00:00",
        endDate: null,
        categoryName: "Konzert",
        categorySlug: "konzert",
        locationName: "Hallenstadion",
        city: "Zürich",
        country: "CH",
        latitude: null,
        longitude: null,
        favorited: false,
      };

      service.getEvent("abc123").subscribe((res) => {
        expect(res).toEqual(mockDetail);
      });

      const req = httpTesting.expectOne(`${apiUrl}/abc123`);
      expect(req.request.method).toBe("GET");
      req.flush(mockDetail);
    });
  });
});
