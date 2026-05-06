import { TestBed } from "@angular/core/testing";
import { Title, Meta } from "@angular/platform-browser";
import { SeoService } from "./seo.service";

describe("SeoService", () => {
  let service: SeoService;
  let titleService: Title;
  let metaService: Meta;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SeoService);
    titleService = TestBed.inject(Title);
    metaService = TestBed.inject(Meta);
  });

  describe("set", () => {
    it('should set title with brand suffix "Löte"', () => {
      const titleSpy = vi.spyOn(titleService, "setTitle");

      service.set("Events entdecken", "Finde tolle Events.");

      expect(titleSpy).toHaveBeenCalledWith("Events entdecken – Löte");
    });

    it("should update meta description tag", () => {
      const metaSpy = vi.spyOn(metaService, "updateTag");

      service.set("Favoriten", "Deine gespeicherten Events.");

      expect(metaSpy).toHaveBeenCalledWith({
        name: "description",
        content: "Deine gespeicherten Events.",
      });
    });

    it("should set both title and description together", () => {
      const titleSpy = vi.spyOn(titleService, "setTitle");
      const metaSpy = vi.spyOn(metaService, "updateTag");

      service.set("Startseite", "Willkommen bei Löte.");

      expect(titleSpy).toHaveBeenCalledOnce();
      expect(metaSpy).toHaveBeenCalledOnce();
    });
  });
});
