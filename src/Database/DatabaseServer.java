package Database;

import Database.Helpers.DatabaseToken;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The Main Server class that constantly loops through awaiting connections from billboard viewers and control panel
 * Server expects several different kinds of information to be passed to it all inside of an Object[]
 * The Object Array must have these variables in it, in this order:
 * A Key (int) which is the grabbed from Database Statements to tell it what action to perform
 * Authentication LinkedList with your username, token, and permissions (this order)
 * Data LinkedList of what to add to the statement in order
 */
public class DatabaseServer {

    private static boolean end = false;
    private static DatabaseConnection connection;
    private static DatabaseToken authentication;

    // ---------------------------- MAIN ---------------------------- //


    /**
     * The Main method of the server that will constantly loop until told otherwise
     * @param args unused
     * @throws SQLException an SQL exception
     * @throws IOException an IOException
     */
    public static void main(String[] args) throws SQLException, IOException {
        // Initialize connection for database
        try {
            connection = new DatabaseConnection("./db.props");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            System.out.println("Failed to connect to database");
            System.out.println("Shutting down server...");
            return;
        }
        connection.createDB();
        authentication = new DatabaseToken();

        ServerSocket serverSocket = new ServerSocket(connection.getPort());
        System.out.println("Starting up server on socket: " + connection.getPort());

        // The Main code to run and take requests
        while( !end ) {

            System.out.println("Waiting for signal...");
            Socket socket = serverSocket.accept();
            System.out.println("Signal Received...");

            // Reads the current input that has been received and returns the statements class
            try {
                // Create a new database statement
                read(socket, connection.getInstance());
            } catch (ClassNotFoundException e) {
                System.out.println("Invalid information has been received: " + e);
            }


            // Close socket and streams
            System.out.println("Signal has been handled!");
            socket.close();
        }

        // Close connection to DB
        connection.close();
    }

    // ---------------------------- METHODS ---------------------------- //


    /**
     * Reads the socket the signal is sent over to then perform an action based on the return results
     * @param socket the current socket
     * @param connection the current instance of the database being created
     * @throws IOException an IOException
     * @throws ClassNotFoundException for the object reading
     */
    public static void read(Socket socket, Connection connection) throws IOException, ClassNotFoundException {

        // Get the first input of the connection
        InputStream inputStream = socket.getInputStream();
        OutputStream outputStream = socket.getOutputStream();

        ObjectInputStream ois = new ObjectInputStream(inputStream);
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);

        // Read them into an object array to then be sorted by the DatabaseStatements class
        Object[] objects = (Object[])ois.readObject();
        DatabaseNavigator databaseNavigator;

        // To stop the server from crashing and returning false authentication
        try {
            databaseNavigator = new DatabaseNavigator(objects, authentication, connection);
            databaseNavigator.navigate();

            // Updates the token if login request otherwise keeps the same token
            authentication = databaseNavigator.updateToken();

            // Send the result signal away to the client side applications
            oos.writeObject(databaseNavigator.returnResult());
            oos.flush();

        } catch (Exception e) {
            oos.writeObject(new String[]{e.getMessage()});
            oos.flush();
        }

        // Close the streams
        oos.close();
        ois.close();
    }

}
