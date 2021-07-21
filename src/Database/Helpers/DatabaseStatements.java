package Database.Helpers;

import java.util.ArrayList;

/**
 * The class that contains all the statements to be used and all the errors that can be returned from the statements
 */
public class DatabaseStatements {

    //<editor-fold desc="Variable Statement List">
    private final String INSERT_BILLBOARD = "INSERT INTO billboards VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE billboard=?";
    private final String INSERT_TIME = "INSERT INTO schedules VALUES (?, ?, ?)";
    private final String INSERT_NEW_USER = "INSERT INTO users VALUES (?, ?, ?, ?)";
    private final String SELECT_BILLBOARD_VIEWER = "SELECT billboard FROM billboards INNER JOIN schedules ON billboards.billboard_ID = schedules.fk_billboard_ID WHERE schedules.start_at <= ? AND schedules.finish_at >= ? ORDER BY start_at DESC LIMIT 1";
    private final String SELECT_LIST_BILLBOARD = "SELECT billboard_ID FROM billboards";
    private final String SELECT_LIST_DETAILS_BILLBOARD = "SELECT * FROM billboards";
    private final String SELECT_LIST_USERS = "SELECT username FROM users";
    private final String SELECT_LIST_DETAILS_USERS = "SELECT username, permissions FROM users";
    private final String SELECT_LOGIN_REQUEST = "SELECT * FROM users WHERE username = ?";
    private final String EDIT_USER_PASSWORD = "UPDATE users SET password = ?, salt = ? WHERE username = ?";
    private final String EDIT_USER_PERMISSION = "UPDATE users SET permissions = ? WHERE username = ?";
    private final String DELETE_BILLBOARD = "DELETE b from billboards b LEFT JOIN schedules s ON s.fk_billboard_ID = b.billboard_ID WHERE s.fk_billboard_ID is NULL AND b.billboard_ID = ?";
    private final String SELECT_SCHEDULES = "SELECT s.start_at, s.finish_at, s.fk_billboard_ID, b.user FROM schedules s LEFT JOIN billboards b ON s.fk_billboard_ID = b.billboard_ID";

    // New Queries
    private final String DELETE_BILLBOARD_ADMIN = "DELETE b FROM billboards b LEFT JOIN schedules s ON s.fk_billboard_ID = b.billboard_ID WHERE b.billboard_ID = ?";
    private final String DELETE_BILLBOARD_SCHEDULE = "DELETE s FROM schedules s WHERE s.fk_billboard_ID = ?";
    private final String SELECT_PERMISSIONS = "SELECT permissions FROM users WHERE username = ?";
    private final String DELETE_USER = "DELETE FROM users WHERE username = ?";
    private final String LOG_OUT = "";
    private final String SELECT_SALT = "SELECT salt FROM users WHERE username = ?";
    private final String SELECT_ROW_BILLBOARD = "SELECT * FROM billboards WHERE billboard_ID = ?";
    private final String EDIT_BILLBOARD =  "UPDATE billboards SET billboard=? WHERE user=?";
    private final String SELECT_ROW_BILLBOARD_NOT_SCHEDULED = "SELECT b.billboard_ID, b.billboard, b.user FROM billboards b LEFT JOIN schedules s ON s.fk_billboard_ID = b.billboard_ID WHERE s.fk_billboard_ID IS NULL AND b.billboard_id = ?";
    private final String SELECT_LIST_SCHEDULES = "SELECT * FROM schedules";
    private final String SELECT_SCHEDULE_DAY = "SELECT b.billboard_ID, s.start_at, s.finish_at FROM billboards b INNER JOIN schedules s ON b.billboard_ID = s.fk_billboard_ID WHERE s.start_at >= ? AND s.start_at <= ?";

    //</editor-fold>

    //<editor-fold desc="Variable Error List">
    // Errors should be in same order as commands above
    private final String INSERT_BILLBOARD_ERROR = "Failed to insert the billboard into the database";
    private final String INSERT_TIME_ERROR = "You cannot schedule for a time that is already taken";
    private final String INSERT_NEW_USER_ERROR = "The username already exists";
    private final String SELECT_BILLBOARD_VIEWER_ERROR = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n <billboard>\n <message>An error has occurred when trying to display a billboard</message>\n </billboard>";
    private final String SELECT_ROW_BILLBOARD_ERROR = "No data corresponding to the given billboard ID was found";
    private final String SELECT_LIST_DETAILS_BILLBOARD_ERROR = "There are no billboards available to be shown";
    private final String SELECT_LIST_USERS_ERROR = "There are no users available to be shown";
    private final String SELECT_LIST_DETAILS_USERS_ERROR = "There are no users available to be shown";
    private final String SELECT_LOGIN_REQUEST_ERROR = "Login information incorrect";
    private final String EDIT_USER_PASSWORD_ERROR = "Could not update user password";
    private final String EDIT_USER_PERMISSION_ERROR = "Could not update user permissions";
    private final String DELETE_BILLBOARD_ERROR = "You cannot delete a billboard that are currently scheduled";
    private final String SELECT_SCHEDULES_ERROR = "There are currently no billboards scheduled";

    // New Errors
    private final String DELETE_BILLBOARD_ADMIN_ERROR = "No such user exists to delete";
    private final String DELETE_BILLBOARD_SCHEDULE_ERROR =  "No such user exists to delete";
    private final String SELECT_PERMISSIONS_ERROR = "No permissions were returned for that user";
    private final String DELETE_USER_ERROR = "That user does not exist to delete";
    private final String LOG_OUT_ERROR = "You were not able to logout";
    private final String SELECT_SALT_ERROR = "That user does not exist";
    private final String SELECT_LIST_BILLBOARD_ERROR = "There are no billboards available to be shown";
    private final String EDIT_BILLBOARD_ERROR = "Could not update the billboard";
    private final String SELECT_ROW_BILLBOARD_NOT_SCHEDULED_ERROR = "That billboard could not be selected";
    private final String SELECT_LIST_SCHEDULES_ERROR = "There are no schedules available";
    private final String SELECT_SCHEDULE_DAY_ERROR = "There are no schedules available during that day";

    //</editor-fold>

    // Variables to store the current statement
    private ArrayList<String> statement_list;
    private ArrayList<String> error_list;
    private String statement;
    private String error;


    /**
     * The constructor used to create a list of statements to then be grabbed
     * @param index the index of the statement to be used
     */
    public DatabaseStatements(int index) {
        statement_list = new ArrayList<>();
        error_list = new ArrayList<>();
        //<editor-fold desc="Adding to Statement_List">
        statement_list.add(INSERT_BILLBOARD);
        statement_list.add(INSERT_TIME);
        statement_list.add(INSERT_NEW_USER);
        statement_list.add(SELECT_BILLBOARD_VIEWER);
        statement_list.add(SELECT_LIST_BILLBOARD);
        statement_list.add(SELECT_LIST_DETAILS_BILLBOARD);
        statement_list.add(SELECT_LIST_USERS);
        statement_list.add(SELECT_LIST_DETAILS_USERS);
        statement_list.add(SELECT_LOGIN_REQUEST);
        statement_list.add(EDIT_USER_PASSWORD);
        statement_list.add(EDIT_USER_PERMISSION);
        statement_list.add(DELETE_BILLBOARD);
        statement_list.add(SELECT_SCHEDULES);

        statement_list.add(DELETE_BILLBOARD_ADMIN);
        statement_list.add(DELETE_BILLBOARD_SCHEDULE);
        statement_list.add(SELECT_PERMISSIONS);
        statement_list.add(DELETE_USER);
        statement_list.add(LOG_OUT);
        statement_list.add(SELECT_SALT);
        statement_list.add(SELECT_ROW_BILLBOARD);
        statement_list.add(EDIT_BILLBOARD);
        statement_list.add(SELECT_ROW_BILLBOARD_NOT_SCHEDULED);
        statement_list.add(SELECT_LIST_SCHEDULES);
        statement_list.add(SELECT_SCHEDULE_DAY);
        //</editor-fold>

        //<editor-fold desc="Adding to Error_list">
        error_list.add(INSERT_BILLBOARD_ERROR);
        error_list.add(INSERT_TIME_ERROR);
        error_list.add(INSERT_NEW_USER_ERROR);
        error_list.add(SELECT_BILLBOARD_VIEWER_ERROR);
        error_list.add(SELECT_LIST_BILLBOARD_ERROR);
        error_list.add(SELECT_LIST_DETAILS_BILLBOARD_ERROR);
        error_list.add(SELECT_LIST_USERS_ERROR);
        error_list.add(SELECT_LIST_DETAILS_USERS_ERROR);
        error_list.add(SELECT_LOGIN_REQUEST_ERROR);
        error_list.add(EDIT_USER_PASSWORD_ERROR);
        error_list.add(EDIT_USER_PERMISSION_ERROR);
        error_list.add(DELETE_BILLBOARD_ERROR);
        error_list.add(SELECT_SCHEDULES_ERROR);

        error_list.add(DELETE_BILLBOARD_ADMIN_ERROR);
        error_list.add(DELETE_BILLBOARD_SCHEDULE_ERROR);
        error_list.add(SELECT_PERMISSIONS_ERROR);
        error_list.add(DELETE_USER_ERROR);
        error_list.add(LOG_OUT_ERROR);
        error_list.add(SELECT_SALT_ERROR);
        error_list.add(SELECT_ROW_BILLBOARD_ERROR);
        error_list.add(EDIT_BILLBOARD_ERROR);
        error_list.add(SELECT_ROW_BILLBOARD_NOT_SCHEDULED_ERROR);
        error_list.add(SELECT_LIST_SCHEDULES_ERROR);
        error_list.add(SELECT_SCHEDULE_DAY_ERROR);

        //</editor-fold>

        statement = statement_list.get(index);
        error = error_list.get(index);
    }


    /**
     * Gets the current statement being used for the query
     * @return returns a statement as a string
     */
    public String getStatement() {
        return statement;
    }


    /**
     * Returns the error for the current possible statement
     * @return the error that can occur if the database query fails
     */
    public String getError() {
        return error;
    }

}
