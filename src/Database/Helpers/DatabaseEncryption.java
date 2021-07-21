package Database.Helpers;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Random;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * The class which will be used for encrypting the password data so it is secure in the database
 * Client Side Methods: String salt = DatabaseEncryption.getSalt(30), DatabaseEncryption.generateSecurePassword(password, salt)
 * Server Side Methods: generateSecurePassword(password, salt), verifyUserPassword(providedpassword, password, salt)
 */
public class DatabaseEncryption {

    // The private variable used to create a randomly encrypted password
    private static final Random RANDOM = new SecureRandom();
    private static final String KEY = "ASHafnafp091824PAJHEUOHNI999alskfpoweutmzxv";
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;


    /**
     * The method to create a random salt value to be added onto the encrypted password
     * @param length the length of the salt string
     * @return the newly created salt
     */
    public static String getSalt(int length) {
        StringBuilder returnValue = new StringBuilder(length);

        // The creation of the salt value to be returned
        for (int i = 0; i < length; i++) {
            returnValue.append(KEY.charAt(RANDOM.nextInt(KEY.length())));
        }

        return new String(returnValue);
    }


    /**
     * The method used to hash the password with the salt creating a secure password for the DB
     * @param password the password sent by the client in a character array
     * @param salt an extended random string to be added to the password in bytes
     * @return the hashed password
     */
    private static byte[] hash(char[] password, byte[] salt) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        Arrays.fill(password, Character.MIN_VALUE);

        // Try to create the hashed form of the password
        try {
            SecretKeyFactory skf =  SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error occured while hashing a password: " + e.getMessage());
        } finally {
            // Clears the password for security measures
            spec.clearPassword();
        }
    }


    /**
     * The generation of the password in a secure form to be sent to the database
     * @param password the password wanting to be hashed
     * @param salt the salt to be added to the password
     * @return the newly hashed password
     */
    public static String generateSecurePassword(String password, String salt) {
        String returnValue = null;
        byte[] securePassword = hash(password.toCharArray(), salt.getBytes());
        returnValue = Base64.getEncoder().encodeToString(securePassword);
        return returnValue;
    }


    /**
     * Method used to verify the password on login
     * @param providedPassword the password login request to be hashed
     * @param securePassword the currently hashed password in the database
     * @param salt the salt to be added
     * @return true if they match, false if they do not
     */
    public static boolean verifyUserPassword(String providedPassword, String securePassword, String salt) {
        // Generate a new hashed password using the same method
        String newSecurePassword = generateSecurePassword(providedPassword, salt);

        // Compare the two passwords equal the same result
        return newSecurePassword.equalsIgnoreCase(securePassword);
    }


}
