package hn.infatlan.msvc_transactions.util;

import java.security.SecureRandom;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CustomUtils {

    private static final int ACCOUNT_NUMBER_LENGTH = 12;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Genera un número de cuenta de exactamente 12 dígitos numéricos.
     */
    public static String generateAccountNumber() {
        StringBuilder accountNumber = new StringBuilder(ACCOUNT_NUMBER_LENGTH);
        accountNumber.append(SECURE_RANDOM.nextInt(9) + 1);
        for (int i = 1; i < ACCOUNT_NUMBER_LENGTH; i++) {
            accountNumber.append(SECURE_RANDOM.nextInt(10));
        }
        return accountNumber.toString();
    }
}
