package Database.Helpers;

import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * This is the class that creates a token for authentication when the user that has logged in tries to access the database
 * The token is stored in a HashMap ordered (Username, String[token, time])
 */
public class DatabaseToken {

    // Variables to create random token results
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder();

    // Storage of all tokens currently active
    static Map<String, String[]> tokenStorage;


    /**
     * The Constructor of the token which will be used for authentication
     */
    public DatabaseToken() {
        tokenStorage = new TreeMap<>();
    }


    /**
     * Checks to see if the token is currently active (if not removes the token)
     * @param username the username of the current user
     * @param token the token sent by the client side to be checked
     * @return true if valid token, false if token expired or invalid
     */
    public boolean verifyToken(String username, String token) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
        Date expiryTime;
        Date storedTime;

        // Parse the value into the Date Object for comparison
        try {
            expiryTime = format.parse(returnTime(1));
            storedTime = format.parse(getStoredTime(username));
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            return false;
        }

        // Checks to see if the date has not expired yet, then check if the token is correct
        if (expiryTime.compareTo(storedTime) > 0) {
            removeAuthentication(username);
            return false;
        } else {
            if (token == getToken(username)) {
                System.out.println("Authentication accepted");
                return true;
            }

            System.out.println("Authentication accepted");
            return true;
        }
    }


    /**
     * Creates a token that is stored in the database
     * @param username the user that the token is attached too
     * @return returns false if the user already has a token, and returns true if the user had a token generated
     */
    public boolean createToken(String username) {
        if (tokenStorage.containsKey(username)) {
            return false;
        }

        // Create Random String
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);

        // Add random token and time to TreeMap
        String randomToken = encoder.encodeToString(randomBytes);
        String time = returnTime(0);
        tokenStorage.put(username, new String[]{randomToken, time});

        return true;
    }


    /**
     * Adds the user information and gets the token for the client to store
     * @param username the username of the currently active user on the client
     * @return the randomized token
     */
    public String getToken(String username) {
        return tokenStorage.get(username)[0];
    }


    /**
     * Gets the stored time fot the token
     * @param username the name of the currently active user
     * @return the stored time
     */
    private String getStoredTime(String username) {
        try {
            return tokenStorage.get(username)[1];
        }
        catch (NullPointerException e) {
            return "";
        }
    }


    /**
     * Removes the values from the TreeMap for the user since the token has expired
     * @param username the user to be removed from authentication
     */
    public void removeAuthentication(String username) {
        tokenStorage.remove(username);
    }


    /**
     * Gets the time and can remove days if needed
     * @param removeDayCount the number of days to be removed from the date created (only used for token checking for expiry)
     * @return the current time in yyyy-MM-dd HH:mm:ss
     */
    private String returnTime(int removeDayCount) {
        LocalDateTime dateTime = LocalDateTime.now().minusDays(removeDayCount);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-mm-dd hh:mm:ss");
        return dateTime.format(formatter);
    }
}
