import { TestBed } from "@angular/core/testing";
import { provideHttpClient } from "@angular/common/http";
import {
  HttpTestingController,
  provideHttpClientTesting,
} from "@angular/common/http/testing";
import { LocationService } from "./location.service";
import { environment } from "../../../environments/environment";

describe("LocationService", () => {
  let service: LocationService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(LocationService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  describe("getCities", () => {
    it("should make GET request to locations/cities endpoint", () => {
      const mockCities = ["Zürich", "Bern", "Basel", "Luzern"];

      service.getCities().subscribe((res) => {
        expect(res).toEqual(mockCities);
        expect(res.length).toBe(4);
      });

      const req = httpTesting.expectOne(
        `${environment.apiUrl}/locations/cities`,
      );
      expect(req.request.method).toBe("GET");
      req.flush(mockCities);
    });
  });
});
