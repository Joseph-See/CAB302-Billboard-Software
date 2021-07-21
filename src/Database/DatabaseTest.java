package Database;

import Database.Helpers.DatabaseEncryption;
import Database.Helpers.DatabaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The following tests are for the DatabaseStatements class which has different methods for
 * client side applications and server side applications
 *
 * Client Side: Will use the public enum setup to send across an integer on the socket to explain
 * what query needs to be used, and how to handle the query
 *
 * Server Side: This will act as the navigator for the server, grabbing the statement which corresponds to
 * number from the enum and will return a TreeSet with all the information required for the the client
 */
class DatabaseTest {

    // The Constructor to be tested on for the server
    DatabaseNavigator statements;
    DatabaseToken databaseToken;
    String[] string_insert_billboard = {"Billboard", "<Billboard>Test</Billboard>", "Harrison"};
    Object[] object_insert_billboard = { DatabaseNavigator.Response.SELECT_BILLBOARD_VIEWER.getValue(),  string_insert_billboard};


    // ----------------------------------- CLIENT SIDE TESTING ----------------------------------- //

    /**
     * This will test that the enum is returning the correct result
     */
    @Test
    public void testEnum() {
        assertEquals(DatabaseNavigator.Response.INSERT_NEW_USER.getValue(), 2);
    }

    @Test
    public void testEnum2() {
        assertEquals(DatabaseNavigator.Response.INSERT_BILLBOARD.getValue(), 0);
    }

    // ----------------------------------- SERVER SIDE TESTING ----------------------------------- //
    /**
     * The Base constructor for the creation of DatabaseStatements
     * It takes an Object[] which contains a Int and String[] in that order
     */
    /*@BeforeEach @Test
    public void constructStatements() {
        statements = new DatabaseNavigator(object_insert_billboard, databaseToken, connection);
    }*/


    /**
     * This tests that the key is being stored correctly in the database
     */
    @Test
    public void testGetKey() {
        assertEquals(statements.getKey(), 3);
    }


    /**
     * Cannot do the rest of the tests currently since I do not know how to setup a mock database connection
     * will add in later, even though I have done the database now
     */
}

// ------------------------------------------------ Database Encyrption Tests ------------------------------------------------ //

/**
 * The following tests are for DatabaseEncryption methods which are used for hashing of the password
 * for the client side and evaluation of login requests for the server
 */
class DatabaseEncryptionTest {

    String salt = DatabaseEncryption.getSalt(30);
    String securedpassword = DatabaseEncryption.generateSecurePassword("password1", salt);


    @Test
    public void testEncryptionTrue() {
        String providedPassword = "password1";

        assertEquals(DatabaseEncryption.verifyUserPassword(providedPassword, securedpassword, salt), true);
    }

    @Test
    public void testEncryptionFalse() {
        String providedPassword = "password";

        assertEquals(DatabaseEncryption.verifyUserPassword(providedPassword, securedpassword, salt), false);
    }
}


// ------------------------------------------------ Database Token Tests ------------------------------------------------ //

/**
 * The following tests are for the authentication factor for the server to create a random token to be stored by the client
 * and then sent back to the server to be checked that it is still a valid token
 *
 * It will require a few public methods for grabbing the token and remove the values when the token is no longer valid
 * (Such as exiting the client or the 24hr time limit has been reached)
 *
 * The Data will be stored in a HashMap that will be shown as TreeMap
 */
class DatabaseTokenTest {

    // Variables to be used to test the results of the Token
    DatabaseToken auth;
    String username = "Harrison";
    String username2 = "Jack";
    String clientToken;


    /**
     * The Base Constructor to be tested over the methods
     */
    @BeforeEach
    public void constructDatabaseToken() {
        auth = new DatabaseToken();
        if (auth.createToken(username)) {
            clientToken = auth.getToken(username);
        }
    }


    /**
     * Test to see if the same username works or causes an error
     */
    @Test
    public void sameUser() {
        assertEquals(auth.createToken(username), false);
    }


    /**
     * Checks to see if the server token is equal to the client token
     */
    @Test
    public void testToken() {
        assertEquals(clientToken, auth.getToken(username));
    }


    /**
     * Checks to verify that the token is equal to true for the user
     */
    @Test
    public void verifyTokenTrue() {
        assertEquals(auth.verifyToken(username, clientToken), true);
    }


    /**
     * Checks if the wrong information has been entered and returns false
     */
    @Test
    public void verifyTokenFalse() {
        assertEquals(auth.verifyToken(username2, clientToken), false);
    }


    /**
     * Checks to see that the token has been removed correctly
     */
    @Test
    public void testRemoveAuthentication() {
        auth.removeAuthentication(username);
        assertEquals(auth.verifyToken(username, clientToken), false);
    }

}