/**
 * Pipe zum Kuerzen von Texten auf eine maximale Länge.
 *
 * Fügt Auslassungszeichen (...) an, wenn der Text das Limit überschreitet.
 */
import { Pipe, PipeTransform } from "@angular/core";

@Pipe({ name: "truncate" })
export class TruncatePipe implements PipeTransform {
  /**
   * Kürzt den Text auf die angegebene Länge.
   *
   * @param value der zu kürzende Text
   * @param limit maximale Zeichenanzahl (Standard: 80)
   * @returns der gekürzte Text mit "..." oder der Originaltext
   */
  transform(value: string, limit = 80): string {
    if (!value || value.length <= limit) return value;
    return value.substring(0, limit) + "...";
  }
}
