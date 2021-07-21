package ClientSideHelpers;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *  The Object Array must have these specific variables and when sending them to this class order must be maintained:
 *  A Key (int) which is the grabbed from Database Statements to tell it what action to perform
 *  Authentication LinkedList with your username, token, and permissions (this order)
 *  Data LinkedList of what to add to the statement (check the DatabaseNavigator.Response.getValue() for the order and params)
 */
public class ClientSignal {

    // The Variables which store the message to be sent to the server
    private Object[] object;
    private List<String> returnedList;


    /**
     * This constructor creates an object to be sent over the server
     * Use when authentication and parameters are not required
     * @param query the response int for the server to interpret
     */
    public ClientSignal(int query) {
        // Create the object to be sent over the server
        List<String> authentication = new LinkedList<>();
        List<String> queryData = new LinkedList<>();

        object = new Object[3];
        object[0] = query;
        object[1] = authentication;
        object[2] = queryData;

        // Connect to the socket server
        try {
            SocketConnect socketConnect = new SocketConnect();
            socketConnect.writeObject(object);
            returnedList = (List<String>) socketConnect.readObject();
            socketConnect.close();
        } catch (Exception e) {
        }
    }


    /**
     * This constructor creates an object to be sent over the server
     * Use when authentication is not required
     * @param query the response signal to be sent to the server
     * @param data the parameters required for the response signal
     */
    public ClientSignal(int query, String[] data) {
        List<String> authentication = new LinkedList<>();
        List<String> queryData = new LinkedList<>(Arrays.asList(data));

        object = new Object[3];
        object[0] = query;
        object[1] = authentication;
        object[2] = queryData;

        // Connect to the socket server
        try {
            SocketConnect socketConnect = new SocketConnect();
            socketConnect.writeObject(object);
            returnedList = (List<String>) socketConnect.readObject();
            socketConnect.close();
        } catch (Exception e) {
        }
    }


    /**
     * This constructor creates an object to be sent over the server
     * @param query the response signal to be sent to the server
     * @param data the parameters required for the response signal
     * @param auth the username, token and permission stored on the client side after login
     */
    public ClientSignal(int query, String[] auth, String[] data) {
        List<String> authentication = new LinkedList<>(Arrays.asList(auth));
        List<String> queryData = new LinkedList<>(Arrays.asList(data));

        object = new Object[3];
        object[0] = query;
        object[1] = authentication;
        object[2] = queryData;

        // Connect to the socket server
        try {
            SocketConnect socketConnect = new SocketConnect();
            socketConnect.writeObject(object);
            returnedList = (List<String>) socketConnect.readObject();
            socketConnect.close();
        } catch (Exception e) {
        }
    }


    /**
     * Returns the result of the socket connection from the server
     * @return the result of the signal sent
     */
    public List<String> returnList() {
        return returnedList;
    }

}
