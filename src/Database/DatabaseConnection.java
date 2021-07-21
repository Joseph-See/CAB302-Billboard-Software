package Database;

import Database.Helpers.DatabaseEncryption;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

/**
 * The initial connections for the database checking whether the schema has been setup and the tables exist
 * A root user will be created on startup for the control panel
 */
public class DatabaseConnection {

    // The connection to be made to the server and socket
    private Connection instance = null;
    private int port;

    // Statements to be sent to the database
    private Statement statement_create = null;
    private Statement statement_use = null;
    private Statement statement_users = null;
    private Statement statement_billboard = null;
    private Statement statement_schedule = null;
    private PreparedStatement statement_create_root = null;

    // Database string queries
    private String database;
    private static final String USERS_SQL = "CREATE TABLE IF NOT EXISTS users " +
                                            "(username VARCHAR(255) NOT NULL," +
                                            "password TEXT NOT NULL," +
                                            "salt TEXT NOT NULL," +
                                            "permissions VARCHAR(255) NOT NULL," +
                                            "PRIMARY KEY ( username ))";
    private static final String BILLBOARDS_SQL = "CREATE TABLE IF NOT EXISTS billboards " +
                                            "(billboard_ID VARCHAR(255) NOT NULL," +
                                            "billboard MEDIUMTEXT NOT NULL, " +
                                            "user VARCHAR(255) NOT NULL, "+
                                            "PRIMARY KEY ( billboard_ID ), " +
                                            "FOREIGN KEY  ( user ) REFERENCES users( username ) " +
                                            "ON UPDATE CASCADE ON DELETE CASCADE)";
    private static final String SCHEDULE_SQL = "CREATE TABLE IF NOT EXISTS schedules " +
                                            "(start_at DATETIME NOT NULL," +
                                            "finish_at DATETIME NOT NULL, " +
                                            "fk_billboard_ID VARCHAR(255) NOT NULL, " +
                                            "FOREIGN KEY  ( fk_billboard_ID ) REFERENCES billboards( billboard_ID ) " +
                                            "ON UPDATE CASCADE ON DELETE CASCADE)";
    private static final String ROOT_SQL = "INSERT IGNORE INTO users VALUES ('root', ?, ?, 'EditUsers ScheduleBillboards CreateBillboards EditAllBillboards')";


    // ---------------------------- PUBLIC METHODS ---------------------------- //

    /**
     * Constructor to setup grab the properties and setup the connection
     * @param fileName --  the name of the file with the properties for the server
     * @throws SQLException when the url returned is null or if the connection cannot be found to the database
     */
    public DatabaseConnection(String fileName) throws SQLException {
        System.out.println("Setting up database...");
        Properties props = getProps(fileName);

        // Grab the specific data
        String url = props.getProperty("jdbc.url");
        String username = props.getProperty("jdbc.username");
        String password = props.getProperty("jdbc.password");
        String schema = props.getProperty("jdbc.schema");
        port = Integer.parseInt(props.getProperty("jdbc.port"));

        // Update database statement
        database = schema;

        // Setup Connection
        instance = DriverManager.getConnection(url + "/", username, password);
    }


    /**
     * The function for creating the initial database setup aslong as it exists
     */
    public void createDB() {
        try {

            // Create the database
            statement_create = instance.createStatement();
            statement_create.executeUpdate("CREATE DATABASE " + database);

            // Populate the Database
            populateDB();
            closeStatements();

        } catch (SQLException sqlException) {
            // 1007 == database already exists
            if (sqlException.getErrorCode() == 1007) {
                System.out.println("Database already exists. \nPopulating Tables if they do not exist...");
                populateDB();
                closeStatements();
            } else {
                System.out.println(sqlException.getMessage());
                sqlException.printStackTrace();
            }
        }
    }


    /**
     * Gets the current instance of the connection
     * @return returns the connection
     */
    public Connection getInstance() {
        return instance;
    }


    /**
     * Produces the port for server to run on
     * @return port
     */
    public int getPort() {
        return port;
    }


    /**
     * Closes the instance of the Database
     * @throws SQLException -- If database does not close successfully throws SQLException
     */
    public void close() throws SQLException {
        instance.close();
    }


    /**
     * Used to get the properties from the db.props file
     * @param name the name of the file
     * @return returns the properties
     */
    public static Properties getProps(String name) {
        Properties props = new Properties();
        try {
            // The objects to store the file and it's properties
            FileInputStream input;

            // Place input file into props
            input = new FileInputStream(name);
            props.load(input);
            input.close();


        } catch (Exception ex) {
            System.out.println("An error has occurred: " + ex.getMessage());
        }

        return props;
    }


    // ---------------------------- PRIVATE METHODS ---------------------------- //


    /**
     * Closes all the open statements that have been used currently
     */
    private void closeStatements() {
        try {
            statement_use.close();
            statement_create.close();
            statement_billboard.close();
            statement_users.close();
            statement_schedule.close();

            System.out.println("Database creation completed!");

        } catch(SQLException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * Populates the Database with tables
     */
    private void populateDB() {
        try {
            // Creating the statements
            statement_use = instance.createStatement();
            statement_users = instance.createStatement();
            statement_billboard = instance.createStatement();
            statement_schedule = instance.createStatement();
            
            // Executing the statements
            statement_use.executeUpdate("USE " + database);
            statement_users.executeUpdate(USERS_SQL);
            statement_billboard.executeUpdate(BILLBOARDS_SQL);
            statement_billboard.executeUpdate(SCHEDULE_SQL);

            createAdmin();
        } catch(SQLException ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }


    /**
     * Creates an admin account with a password that has been hashed twice
     * to match the specifications when logging in client side
     */
    private void createAdmin() {
        String salt = DatabaseEncryption.getSalt(24);
        String firstHash = DatabaseEncryption.generateSecurePassword("password", salt);
        String finalHash = DatabaseEncryption.generateSecurePassword(firstHash, salt);

        try {
            statement_create_root = instance.prepareStatement(ROOT_SQL);
            statement_create_root.setString(1, finalHash);
            statement_create_root.setString(2, salt);
            statement_create_root.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
