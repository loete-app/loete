import { TruncatePipe } from "./truncate.pipe";

describe("TruncatePipe", () => {
  let pipe: TruncatePipe;

  beforeEach(() => {
    pipe = new TruncatePipe();
  });

  it("should return short string unchanged", () => {
    expect(pipe.transform("Hello world")).toBe("Hello world");
  });

  it("should truncate long string with ellipsis", () => {
    const long = "a".repeat(100);
    const result = pipe.transform(long, 50);
    expect(result).toBe("a".repeat(50) + "...");
    expect(result.length).toBe(53);
  });

  it("should use default limit of 80", () => {
    const exactly80 = "x".repeat(80);
    expect(pipe.transform(exactly80)).toBe(exactly80);

    const str81 = "x".repeat(81);
    expect(pipe.transform(str81)).toBe("x".repeat(80) + "...");
  });

  it("should return empty string as-is", () => {
    expect(pipe.transform("")).toBe("");
  });

  it("should return null/undefined as-is", () => {
    expect(pipe.transform(null as unknown as string)).toBeNull();
    expect(pipe.transform(undefined as unknown as string)).toBeUndefined();
  });

  it("should return string equal to limit unchanged", () => {
    const exact = "a".repeat(40);
    expect(pipe.transform(exact, 40)).toBe(exact);
  });

  it("should handle custom limit", () => {
    expect(pipe.transform("Hello, World!", 5)).toBe("Hello...");
  });
});
