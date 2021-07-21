package ControlPanel;

import ClientSideHelpers.ClientSignal;
import Database.DatabaseNavigator;
import Database.Helpers.DatabaseEncryption;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;


/**
 * The GUI for creating a user form page to create a new user
 * No fields must be empty when sending
 */
public class AddUserForm extends JFrame implements ActionListener {

    private static final int WIDTH = 500;
    private static final int HEIGHT = 250;

    // Components for the interface
    JFrame frame;
    JList permissions;

    private JPanel panel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton create;

    JLabel username, password, permission, showPermission;

    // Other Global Variables
    private List<Integer> listIndex;
    DefaultListModel model;
    private String token;
    private String activeUser;


    /**
     * The Constructor to create the add user GUI
     * @param u the username of the active user
     * @param t the current token
     */
    public AddUserForm(String u, String t) {
        token = t;
        activeUser = u;
        listIndex = new ArrayList<>();
        SwingUtilities.invokeLater(this::createUserGUI);
    }


    /**
     * When the button is pressed to add the user to the database
     * @param e the event being acted upon by the button
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String userText = usernameField.getText();
        String passText = String.valueOf(passwordField.getPassword());
        String permText = "";

        // Edit the query permissions to be formatted as needed
        for (int i = 0; i < listIndex.size(); i++) {
            String text = (String) model.getElementAt(listIndex.get(i));
            text = text.replace(" ", "");
            permText = permText.concat(text + " ");
        }


        if (userText.isEmpty()|| passText.isEmpty() || permText.isEmpty()) {
            JOptionPane.showMessageDialog(new JFrame(), "You need to enter values in all fields", "User Creation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Encryption
        String salt = DatabaseEncryption.getSalt(24);
        String hashedPassword = DatabaseEncryption.generateSecurePassword(passText, salt);

        int response = DatabaseNavigator.Response.INSERT_NEW_USER.getValue();
        String[] data = {userText, hashedPassword, salt, permText};
        String[] authentication = {activeUser, token};
        ClientSignal signal = new ClientSignal(response, authentication, data);
        List<String> signalResult = signal.returnList();

        // Get signal from database
        if (signalResult.contains("Success")) {
            JOptionPane.showMessageDialog(new JFrame(), "User has been created");
        } else {
            JOptionPane.showMessageDialog(new JFrame(), signalResult.get(1), "User Creation failure", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * Creates the GUI for the creation of users
     */
    private void createUserGUI() {
        frame = new JFrame("Create User");
        setSize(new Dimension(WIDTH, HEIGHT));
        setLayout(new BorderLayout());

        panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username Label and Field
        username = new JLabel();
        username.setText("Username: ");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        panel.add(username, gbc);

        usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(usernameField, gbc);

        // Password field and text
        password = new JLabel();
        password.setText("Password: ");
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(password, gbc);

        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(passwordField, gbc);


        // Add permissions list and label
        permission = new JLabel();
        permission.setText("Permissions: ");
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        panel.add(permission, gbc);


        model = new DefaultListModel();
        permissions = new JList(model);
        JScrollPane permissionsList = new JScrollPane(permissions);
        model.addElement("Edit Users");
        model.addElement("Create Billboards");
        model.addElement("Schedule Billboards");
        model.addElement("Edit All Billboards");

        permissions.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        permissions.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {

                    int index = permissions.getSelectedIndex();

                    // Check whether to add or remove result
                    if (listIndex.contains(index)) {
                        Object temp = index;
                        listIndex.remove(temp);
                    } else {
                        listIndex.add(index);
                    }


                    String permText = "";

                    for (int i = 0; i < listIndex.size(); i++) {
                        String text = (String) model.getElementAt(listIndex.get(i));
                        permText = permText.concat("[" + text + "] ");
                    }

                    showPermission.setText(permText);
                } else {
                    // Do Nothing
                }
            }});


        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(permissions, gbc);


        showPermission = new JLabel();
        showPermission.setText(" ");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 3;
        panel.add(showPermission, gbc);


        // Add button to panel
        create = new JButton();
        create.setText("Create User");
        create.addActionListener(this);

        gbc.gridx = 1;
        gbc.gridy = 4;
        gbc.gridwidth = 1;
        panel.add(create, gbc);


        // Make the panel visible
        getContentPane().add(panel);
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLocation(100, 100);
        pack();
        setVisible(true);
    }

}
