import { TestBed } from "@angular/core/testing";
import { provideHttpClient } from "@angular/common/http";
import {
  HttpTestingController,
  provideHttpClientTesting,
} from "@angular/common/http/testing";
import { CategoryService } from "./category.service";
import { environment } from "../../../environments/environment";
import { Category } from "../models/category.model";

describe("CategoryService", () => {
  let service: CategoryService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(CategoryService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  describe("getCategories", () => {
    it("should make GET request to categories endpoint", () => {
      const mockCategories: Category[] = [
        { id: 1, name: "Konzert", slug: "konzert" },
        { id: 2, name: "Sport", slug: "sport" },
      ];

      service.getCategories().subscribe((res) => {
        expect(res).toEqual(mockCategories);
        expect(res.length).toBe(2);
      });

      const req = httpTesting.expectOne(`${environment.apiUrl}/categories`);
      expect(req.request.method).toBe("GET");
      req.flush(mockCategories);
    });
  });
});
