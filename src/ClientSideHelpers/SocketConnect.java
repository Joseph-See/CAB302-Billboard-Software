package ClientSideHelpers;

import Database.DatabaseConnection;

import java.io.*;
import java.net.Socket;
import java.util.Properties;

/**
 * A class that helps send signals without all the needed repetition of code
 * Uses similar syntax to the normal way to make it easy to understand
 */
public class SocketConnect {

    // The variables that hold the connection information
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    /**
     * The constructor that sets up a connection the server for a signal to be sent
     * @throws IOException throws an exception if the file cannot be found ./db.props
     */
    public SocketConnect() throws IOException {
        // Setting up the ports
        Properties props = DatabaseConnection.getProps("./db.props");
        int port = Integer.parseInt(props.getProperty("jdbc.port"));
        socket = new Socket("localhost", port);

        // Signal Sending
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
        oos = new ObjectOutputStream(outputStream);
        ois = new ObjectInputStream(inputStream);
    }


    /**
     * Send the object from the client to the server
     * @param obj the current object to be sent
     * @throws IOException when the object cannot be sent across the socket connection
     */
    public void writeObject(Object[] obj) throws IOException {
        oos.writeObject(obj);
        oos.flush();
    }


    /**
     * Gets the signal being sent and stores it for the client
     * @return the object being sent by the server
     * @throws IOException when the signal cannot be received by the socket connection
     * @throws ClassNotFoundException used for when converting the object into a List (will never occur)
     */
    public Object readObject() throws IOException, ClassNotFoundException {
        return ois.readObject();
    }


    /**
     * Closes the connection
     * @throws IOException if there is an error closing any connections
     */
    public void close() throws IOException {
        ois.close();
        oos.close();
        socket.close();

    }
}
