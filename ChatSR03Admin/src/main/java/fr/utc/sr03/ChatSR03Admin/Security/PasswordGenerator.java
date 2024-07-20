package fr.utc.sr03.ChatSR03Admin.Security;

import java.security.SecureRandom;

/**
 * Utility class for generating random passwords.
 */
public class PasswordGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_+=<>?";
    private static final int PASSWORD_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a random password.
     *
     * @return the generated password
     */
    public static String generateRandomPassword() {
        StringBuilder password = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            password.append(CHARACTERS.charAt(index));
        }
        return password.toString();
    }

    /**
     * Main method for testing the password generator.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("Generated Password: " + generateRandomPassword());
    }
}
