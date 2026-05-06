import { ComponentFixture, TestBed } from "@angular/core/testing";
import { provideRouter } from "@angular/router";
import { Navbar } from "./navbar";
import { AuthService } from "@/core/services/auth.service";
import { User } from "@/core/models/auth.model";
import { signal } from "@angular/core";

describe("Navbar", () => {
  let fixture: ComponentFixture<Navbar>;
  let element: HTMLElement;

  const mockAuthService = {
    currentUser: signal<User | null>(null),
    isAuthenticated: vi.fn().mockReturnValue(false),
    logout: vi.fn(),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Navbar],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: mockAuthService },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Navbar);
    fixture.detectChanges();
    element = fixture.nativeElement;
  });

  it("should create the component", () => {
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should render brand text "Löte"', () => {
    const brand = element.querySelector(".brand");
    expect(brand).toBeTruthy();
    expect(brand!.textContent).toBe("Löte");
  });

  it("should render Events navigation link", () => {
    const links = element.querySelectorAll(".links a");
    const eventsLink = links[0];
    expect(eventsLink).toBeTruthy();
    expect(eventsLink.textContent?.trim()).toContain("Events");
  });

  it("should show login/register links when not authenticated", () => {
    const authLink = element.querySelector(".auth-link");
    const registerLink = element.querySelector(".register-link");
    expect(authLink).toBeTruthy();
    expect(registerLink).toBeTruthy();
  });

  it("should show username and logout when authenticated", () => {
    mockAuthService.currentUser.set({
      id: "usr1",
      email: "test@test.com",
      username: "testuser",
    });
    fixture.detectChanges();

    const username = element.querySelector(".username");
    expect(username).toBeTruthy();
    expect(username!.textContent?.trim()).toBe("testuser");

    const logoutBtn = element.querySelector(".logout-btn");
    expect(logoutBtn).toBeTruthy();
  });

  it("should have correct routerLink on brand", () => {
    const brand = element.querySelector(".brand");
    expect(brand?.getAttribute("href")).toBe("/");
  });
});
