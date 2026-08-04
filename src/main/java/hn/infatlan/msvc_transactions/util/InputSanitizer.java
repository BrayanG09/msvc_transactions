package hn.infatlan.msvc_transactions.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

public final class InputSanitizer {

    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\p{Cntrl}&&[^\r\n\t]]");
    private static final Pattern MULTI_SPACE = Pattern.compile("\\s{2,}");
    private static final Pattern NON_DIGITS = Pattern.compile("\\D");

    private InputSanitizer() {
    }

    public static String sanitize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC).trim();
        normalized = CONTROL_CHARS.matcher(normalized).replaceAll("");
        normalized = MULTI_SPACE.matcher(normalized).replaceAll(" ");
        return normalized;
    }

    /**
     * Conserva únicamente dígitos (elimina espacios, guiones y cualquier otro carácter).
     */
    public static String sanitizeIdentity(String identityNumber) {
        if (identityNumber == null) {
            return null;
        }
        String digitsOnly = NON_DIGITS.matcher(identityNumber).replaceAll("");
        return digitsOnly.isEmpty() ? "" : digitsOnly;
    }

    public static String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
