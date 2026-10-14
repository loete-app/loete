package ch.loete.backend.util;

import java.security.SecureRandom;
import java.util.stream.Collectors;

public final class NanoIdGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghkmnopqrstuvwxyz123456789_-";
    private static final int ID_LENGTH = 8;

    private NanoIdGenerator() {}

    public static String generate() {
        return RANDOM.ints(ID_LENGTH, 0, CHARS.length())
                .mapToObj(i -> String.valueOf(CHARS.charAt(i)))
                .collect(Collectors.joining());
    }
}
