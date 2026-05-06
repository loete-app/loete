import { TestBed } from "@angular/core/testing";
import { provideHttpClient } from "@angular/common/http";
import {
  HttpTestingController,
  provideHttpClientTesting,
} from "@angular/common/http/testing";
import { VibeSearchService } from "./vibe-search.service";
import { environment } from "../../../environments/environment";
import {
  VibeSearchRequest,
  VibeSearchResponse,
} from "../models/vibe-search.model";

describe("VibeSearchService", () => {
  let service: VibeSearchService;
  let httpTesting: HttpTestingController;
  const apiUrl = `${environment.apiUrl}/search/vibe`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(VibeSearchService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it("should send POST request with correct body", () => {
    const request: VibeSearchRequest = {
      query: "gemütlicher Jazz-Abend",
      categoryId: 1,
      city: "Zürich",
    };

    const mockResponse: VibeSearchResponse = {
      results: [
        {
          id: "ev1",
          name: "Jazz Night",
          imageUrl: "https://example.com/jazz.jpg",
          startDate: "2026-07-15T20:00:00",
          categoryName: "Konzert",
          locationName: "Tonhalle",
          city: "Zürich",
        },
      ],
      fallback: false,
    };

    service.search(request).subscribe((res) => {
      expect(res).toEqual(mockResponse);
    });

    const req = httpTesting.expectOne(apiUrl);
    expect(req.request.method).toBe("POST");
    expect(req.request.body).toEqual(request);
    req.flush(mockResponse);
  });

  it("should handle fallback response", () => {
    const request: VibeSearchRequest = {
      query: "summer festival",
    };

    const mockResponse: VibeSearchResponse = {
      results: [],
      fallback: true,
    };

    service.search(request).subscribe((res) => {
      expect(res.fallback).toBe(true);
      expect(res.results).toEqual([]);
    });

    const req = httpTesting.expectOne(apiUrl);
    req.flush(mockResponse);
  });

  it("should send request with minimal body", () => {
    const request: VibeSearchRequest = {
      query: "rock concert",
    };

    service.search(request).subscribe();

    const req = httpTesting.expectOne(apiUrl);
    expect(req.request.body).toEqual({ query: "rock concert" });
    req.flush({ results: [], fallback: false });
  });
});
