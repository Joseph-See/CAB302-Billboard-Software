package Database;

import Database.Helpers.DatabaseEncryption;
import Database.Helpers.DatabaseStatements;
import Database.Helpers.DatabaseToken;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A class that is used by all programs to send the correct response and to receive the response turning it into a string
 * To send data across to the server you must have, in this order: response integer, authentication details (can be empty), and data for the sql query
 */
public class DatabaseNavigator {

    // The string to be used for the query of the database
    private Connection connection;
    private DatabaseStatements databaseStatements;
    private PreparedStatement statement;
    private DatabaseToken databaseToken;
    private List<String> data;
    private List<String> authentication;
    private List<String> results;
    private int key;


    /**
     * The list of integers for responses to queries from the control panel or viewer
     * NOTE: This is only used by the viewer and the control panel
     * EXAMPLE OF HOW TO USE: "DatabaseStatements.Response.STORE_BILLBOARD_SCHEDULE.getValue();"
     */
    public enum Response {
        //<editor-fold desc="Enum List">
        /**
         * Params: billboard name, billboard XML string, Owner of billboard, billboard XML string
         * Returns: nothing
         */
        INSERT_BILLBOARD(0),
        /**
         * Params: start time, finish time, billboard name
         * Returns: nothing
         */
        INSERT_TIME(1),
        /**
         * Params: username, password, salt, permission
         * Returns: nothing
         */
        INSERT_NEW_USER(2),
        /**
         * Params: DateTime, DateTime
         * Returns: billboard XML data
         */
        SELECT_BILLBOARD_VIEWER(3),
        /**
         * Params: Nothing
         * Returns: select the names of the billboard
         */
        SELECT_LIST_BILLBOARD(4),
        /**
         * Params: Nothing
         * Returns: all the billboards and details
         */
        SELECT_LIST_DETAILS_BILLBOARD(5),
        /**
         * Params: Nothing
         * Returns: select the names of all the users
         */
        SELECT_LIST_USERS(6),
        /**
         * Params: Nothing
         * Returns: all the users and details
         */
        SELECT_LIST_DETAILS_USERS(7),
        /**
         * Params: username, password
         * Returns: username, permission, token
         */
        SELECT_LOGIN_REQUEST(8),
        /**
         * Params: password, salt, username
         * Returns: nothing
         */
        EDIT_USER_PASSWORD(9),
        /**
         * Params: permission, username
         * Returns: nothing
         */
        EDIT_USER_PERMISSION(10),
        /**
         * Params: billboard name
         * Returns: nothing
         */
        DELETE_BILLBOARD(11),
        /**
         * Params: nothing
         * Returns: all the schedules
         */
        SELECT_SCHEDULES(12),
        /**
         * Params: billboard name
         * Returns: nothing
         */
        DELETE_BILLBOARD_ADMIN(13),
        /**
         * Params: billboard name, starting time
         * Returns: nothing
         */
        DELETE_BILLBOARD_SCHEDULE(14),
        /**
         * Params: username
         * Returns: returns the list of permissions for the user
         */
        SELECT_PERMISSIONS(15),
        /**
         * Params: username
         * Returns: nothing
         */
        DELETE_USER(16),
        /**
         * Params: nothing
         * Returns: nothing
         */
        LOG_OUT(17),
        /**
         * Params: username
         * Returns: the salt of the user
         */
        SELECT_SALT(18),
        /**
         * Params: billboard name
         * Returns: the corresponding XML from the billboards table
         */
        SELECT_ROW_BILLBOARD(19),
        /**
         * Params: new billboard XML String, username of billboard
         * Returns: nothing
         */
        EDIT_BILLBOARD(20),
        /**
         * Params: billboard name
         * Returns: The row of a billboard that is not scheduled
         */
        SELECT_ROW_BILLBOARD_NOT_SCHEDULED(21),
        /**
         * Params: nothing
         * Returns: the whole schedule table
         */
        SELECT_LIST_SCHEDULES(22),
        /**
         * Params: start of day (xxxx-xx-xx 00:00:00) and end of day (xxxx-xx-xx 23:59:59)
         * Returns: the list of billboards that are scheduled between a time frame
         */
        SELECT_SCHEDULE_DAY(23);

        //</editor-fold>

        private int value;
        private static Map map = new HashMap<>();

        /**
         * The constructor for the enum
         * @param value the integers representing the response
         */
        Response(int value) {
            this.value = value;
        }

        // Placement of the response in the HashMap
        static {
            for (Response response : Response.values()) {
                map.put(response.value, response);
            }
        }

        /**
         * Getting the value of the response
         * @param response the integer value being used
         * @return the value the stored value for the enum map
         */
        public static Response valueOf(int response) {
            return (Response) map.get(response);
        }

        /**
         * The function to get the value to be sent over to the server
         * @return the value
         */
        public int getValue() {
            return value;
        }


    }


    /**
     * The constructor for database statements
     * NOTE: This is only used by the server
     * @param objects the data received
     * @param auth the current Hashmap that is storing all the currently active tokens
     * @param connection the current instance of the database connection
     * @throws Exception throws an exception when the token authentication fails meaning the token has expired
     */
    public DatabaseNavigator(Object[] objects, DatabaseToken auth, Connection connection) throws Exception {
        // Put the values in the private variables
        results = new ArrayList<>();
        databaseToken = auth;
        key = (int)objects[0];
        authentication = (LinkedList<String>) objects[1];
        data = (LinkedList<String>) objects[2];
        databaseStatements = new DatabaseStatements(key);
        this.connection = connection;

        // Check that the authentication key sent is correct or logging in
        if (key == 8|| key == 17 || key == 18 || key == 3 ||  key == 0 || databaseToken.verifyToken(authentication.get(0), authentication.get(1))) {
            // Leave empty as it is just to check that what is returned is correct
        } else {
            throw new Exception("Authentication declined");
        }
    }


    // ---------------------------- PUBLIC METHODS ---------------------------- //


    /**
     * Preparation of the statement for it to be further handled
     */
    private void prepareQuery() {
        // Setup the PreparedStatement
        try {
            statement = connection.prepareStatement(databaseStatements.getStatement());
        } catch (SQLException e) {
            System.out.println("An error has occurred: " + e.getMessage());
        }

        // Add the data to the statement
        for (int i = 1; i <= data.size(); i++) {
            try {
                statement.setString(i, data.get(i - 1));
            } catch (SQLException e) {
                System.out.println("An error has occurred: " + e.getMessage());
            }
        }

        // Server documentation
        System.out.println(statement);
    }


    /**
     * Gets the current key
     * @return key
     */
    public int getKey() { return key; }


    /**
     * Returns the updated token list to the server
     * @return the newly updated token list
     */
    public DatabaseToken updateToken() {
        return databaseToken;
    }


    /**
     * Goes through a switch statement to find the correct outcome for the Query
     */
    public void navigate() {
        switch(key) {

            // Insert/Update/Delete Statements Cases
            case 0: case 1: case 10: case 11: case 13: case 14: case 16: case 20:
                prepareQuery();
                generic_statements();
                break;

            // Select billboard viewer information
            case 3: case 4: case 5: case 6: case 7: case 12: case 15: case 18: case 19: case 21: case 22: case 23:
                prepareQuery();
                select_statement();
                break;

            // Insert user with hashing
            case 2:
                insert_new_user();
                break;

            // Login request returning token
            case 8:
                login_request();
                break;

            case 9:
                edit_user_password();
                break;

            // Logout request destroying token
            case 17:
                logout_request();
                break;


            // Default to stop errors
            default:
                break;
        }
    }


    /**
     * The result to be shown to the client side applications
     * @return results TreeSet
     */
    public List<String> returnResult() { return results; }


    // ---------------------------- PRIVATE METHODS ---------------------------- //


    /**
     * For insertion of information into the database
     */
    private void generic_statements() {
        try {
            statement.execute();
            results.add("Success");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            returnError();
        }
    }


    /**
     * The universal select method to return any amount of data required by the statement
     */
    private void select_statement() {
        ResultSet rs;

        try {
            rs = statement.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // Check to see if result is empty first then loop
            if (!rs.next()) {
                returnError();
            } else {
                results.add(0, "Success");
                do {
                    for (int i = 1; i <= columnCount; i++) {
                        results.add(rs.getString(i));
                    }
                } while (rs.next());
            }
        } catch (SQLException ex) {
            returnError();
        }
    }


    /**
     * Creates salt and hashes the password to be stored in the database
     */
    private void insert_new_user() {
        String salt = data.get(2);
        String password = DatabaseEncryption.generateSecurePassword(data.get(1), salt);

        data.remove(1);
        data.add(1, password);

        prepareQuery();
        generic_statements();
    }


    /**
     * Edits an existing users password and salt
     */
    private void edit_user_password() {
        // Get the current hashed password and salt to be hashed again
        String providedPassword = data.get(0);
        String providedSalt = data.get(1);
        String newPassword = DatabaseEncryption.generateSecurePassword(providedPassword, providedSalt);

        // Remove and add new hashed password for query
        data.remove(0);
        data.add(0, newPassword);

        // Run the query
        prepareQuery();
        generic_statements();
    }


    /**
     * Checks whether to see the users login details matches the database
     */
    private void login_request() {
        // Assort the necessary information and remove the password for prepareQuery
        String username = data.get(0);
        String providedPassword = data.get(1);
        data.remove(1);
        prepareQuery();

        // Stored results
        select_statement();
        String storedPassword = results.get(2);
        String storedSalt = results.get(3);

        // Remove from results so it does not get transferred to client
        results.remove(2);
        results.remove(2);

        // See if the newly added results are correct or not
        if (DatabaseEncryption.verifyUserPassword(providedPassword, storedPassword, storedSalt)) {
            // Create Authentication
            databaseToken.createToken(username);

            // Return successful
            results.add(1, databaseToken.getToken(username));
            System.out.println("Login request successful");
        } else {
            results.clear();
            returnError();
        }
    }


    /**
     * Destroys the token stored under name when the user logs out
     */
    private void logout_request() {
        try {
            String username = authentication.get(0);
            databaseToken.removeAuthentication(username);
            results.add("Success");
        } catch (Exception e) {
            returnError();
        }

    }


    /**
     * Handles all basic exceptions when they occur, and updates the result to be sent back to the client
     */
    private void returnError() {
        results.add(0, "Failed");
        results.add(databaseStatements.getError());
    }

}
