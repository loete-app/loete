package ch.loete.backend.util;

import java.security.SecureRandom;
import java.util.stream.Collectors;

/**
 * Generator für kryptographisch sichere NanoIDs.
 *
 * <p>Erzeugt 8-Zeichen-IDs aus einem URL-sicheren Zeichensatz (Buchstaben, Ziffern, Unterstrich,
 * Bindestrich). Wird als Primärschlüssel für Events, Benutzer und Favoriten verwendet.
 */
public final class NanoIdGenerator {

  /** Kryptographisch sicherer Zufallsgenerator. */
  private static final SecureRandom RANDOM = new SecureRandom();

  /** URL-sicherer Zeichensatz (ohne leicht verwechselbare Zeichen wie I, l, O, 0). */
  private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghkmnopqrstuvwxyz123456789_-";

  /** Länge der generierten IDs. */
  private static final int ID_LENGTH = 8;

  /** Privater Konstruktor verhindert Instanziierung. */
  private NanoIdGenerator() {}

  /**
   * Generiert eine neue 8-Zeichen-NanoID.
   *
   * @return die generierte NanoID
   */
  public static String generate() {
    return RANDOM
        .ints(ID_LENGTH, 0, CHARS.length())
        .mapToObj(i -> String.valueOf(CHARS.charAt(i)))
        .collect(Collectors.joining());
  }
}
