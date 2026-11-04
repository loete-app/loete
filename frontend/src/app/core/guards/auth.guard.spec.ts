import { TestBed } from "@angular/core/testing";
import {
  ActivatedRouteSnapshot,
  Router,
  RouterStateSnapshot,
  UrlTree,
} from "@angular/router";
import { authGuard } from "./auth.guard";
import { AuthService } from "../services/auth.service";

describe("authGuard", () => {
  let authService: AuthService;
  let router: Router;
  const mockRoute = {} as ActivatedRouteSnapshot;
  const mockState = { url: "/favoriten" } as RouterStateSnapshot;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        {
          provide: AuthService,
          useValue: { isAuthenticated: vi.fn() },
        },
        {
          provide: Router,
          useValue: { createUrlTree: vi.fn() },
        },
      ],
    });
    authService = TestBed.inject(AuthService);
    router = TestBed.inject(Router);
  });

  it("should allow access when authenticated", () => {
    vi.mocked(authService.isAuthenticated).mockReturnValue(true);

    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, mockState),
    );

    expect(result).toBe(true);
  });

  it("should redirect to /login with returnUrl when not authenticated", () => {
    vi.mocked(authService.isAuthenticated).mockReturnValue(false);
    const mockUrlTree = {} as UrlTree;
    vi.mocked(router.createUrlTree).mockReturnValue(mockUrlTree);

    const result = TestBed.runInInjectionContext(() =>
      authGuard(mockRoute, mockState),
    );

    expect(router.createUrlTree).toHaveBeenCalledWith(["/login"], {
      queryParams: { returnUrl: "/favoriten" },
    });
    expect(result).toBe(mockUrlTree);
  });
});
