import { TestBed } from "@angular/core/testing";
import {
  provideHttpClient,
  HttpClient,
  withInterceptors,
} from "@angular/common/http";
import {
  HttpTestingController,
  provideHttpClientTesting,
} from "@angular/common/http/testing";
import { authInterceptor } from "./auth.interceptor";
import { AuthService } from "../services/auth.service";

describe("authInterceptor", () => {
  let httpClient: HttpClient;
  let httpTesting: HttpTestingController;
  let authService: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: AuthService,
          useValue: { getToken: vi.fn() },
        },
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    httpClient = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
    authService = TestBed.inject(AuthService);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it("should add Authorization header when token exists", () => {
    vi.mocked(authService.getToken).mockReturnValue("my-jwt-token");

    httpClient.get("/api/test").subscribe();

    const req = httpTesting.expectOne("/api/test");
    expect(req.request.headers.get("Authorization")).toBe(
      "Bearer my-jwt-token",
    );
    req.flush({});
  });

  it("should not add Authorization header when no token", () => {
    vi.mocked(authService.getToken).mockReturnValue(null);

    httpClient.get("/api/test").subscribe();

    const req = httpTesting.expectOne("/api/test");
    expect(req.request.headers.has("Authorization")).toBe(false);
    req.flush({});
  });
});
