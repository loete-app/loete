import { Injectable } from "@angular/core";

@Injectable({ providedIn: "root" })
export class ClientIdService {
  private readonly storageKey = "loete_client_id";
  private cachedId: string | null = null;

  getClientId(): string | null {
    if (typeof window === "undefined") return null;
    if (this.cachedId) return this.cachedId;

    let id = localStorage.getItem(this.storageKey);
    if (!id) {
      id = this.generateId();
      localStorage.setItem(this.storageKey, id);
    }
    this.cachedId = id;
    return id;
  }

  private generateId(): string {
    if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
      return crypto.randomUUID();
    }
    return (
      Date.now().toString(36) + Math.random().toString(36).slice(2, 12)
    ).padEnd(20, "0");
  }
}
