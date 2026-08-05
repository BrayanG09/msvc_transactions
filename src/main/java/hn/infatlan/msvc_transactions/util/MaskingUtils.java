package hn.infatlan.msvc_transactions.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class MaskingUtils {

    private static final int DEFAULT_VISIBLE_DIGITS = 4;
    private static final char MASK_CHAR = '*';

    /**
     * Enmascara un número de cuenta dejando visibles solo los últimos 4 dígitos.
     * Ejemplo: {@code 591168003682} → {@code ********3682}
     */
    public static String maskAccountNumber(String accountNumber) {
        return maskAccountNumber(accountNumber, DEFAULT_VISIBLE_DIGITS);
    }

    /**
     * Enmascara un número de cuenta dejando visibles solo los últimos {@code visibleDigits} caracteres.
     * Si el valor es nulo, vacío o más corto/igual que los dígitos visibles, se enmascara por completo.
     */
    public static String maskAccountNumber(String accountNumber, int visibleDigits) {
        if (accountNumber == null || accountNumber.isBlank()) {
            return accountNumber;
        }

        String value = accountNumber.trim();
        int length = value.length();
        int safeVisible = Math.max(0, visibleDigits);

        if (safeVisible == 0 || length <= safeVisible) {
            return String.valueOf(MASK_CHAR).repeat(length);
        }

        int maskedLength = length - safeVisible;
        return String.valueOf(MASK_CHAR).repeat(maskedLength) + value.substring(maskedLength);
    }
}
