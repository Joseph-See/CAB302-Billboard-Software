package ControlPanel;

import javax.naming.ldap.Control;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import BillboardViewer.Billboard;
import ClientSideHelpers.ClientSignal;
import CreateBillboards.CreateBillboards;
import CreateBillboards.TempXMLCreator;
import CreateBillboards.XMLParser;
import Database.DatabaseNavigator;
import Database.Helpers.DatabaseEncryption;
import org.xml.sax.SAXException;

/**
 *  This class is the implementation of the GUI for managing billboards, schedules and users.
 *  It requires a user to log in, and permissions are checked before actions are allowed to be performed.
 *  Once a user has logged in, they are able to access the different functions which are spread out across tabs.
 *
 *  The Manage Billboards tab allows users to create billboards, edit billboards, delete billboards and preview billboards (depending on permissions).
 *  Create and edit billboards utilize the CreateBillboards class: {@link CreateBillboards}
 *  Billboards are displayed in a list which is pulled from the server.
 *  See {@link #layoutBBManagerTab()}
 *
 *  The View Schedule tab displays all currently scheduled billboards in a weekly view.
 *  Schedules are pulled from the server and the viewer displays the start time, end time, billboard time, billboard name and scheduling user.
 *  The View Schedule tab also lets users (with correct permissions) schedule a billboard with day/hour/minute control over scheduling.
 *  See: {@link AddToSchedule}
 *  See: {@link #layoutSchedulerTab()}
 *
 *  The Manage Users tab allows authorized users to add users (including selecting their permissions) and delete users.
 *  The tab queries the server for a list of users and displays it to the user.
 *  See: {@link AddUserForm}
 *  See: {@link #layoutUserMngTab()}
 *
 *  The Launch Billboard tab allows users to launch the Billboard viewer and display scheduled billboards.
 *  See: {@link Billboard}
 *  See: {@link #layoutLaunchBBTab()}
 */
public class ControlPanel extends JFrame implements ActionListener, ChangeListener, Runnable, ListSelectionListener {

    // -------------------------- Variables -------------------------- //

    private static final int WIDTH = 1200;
    private static final int HEIGHT = 900;

    // Fields
    private int bbIndexSelected, userListIndex;

    // Event objects
    String bbSelected;

    // List objects
    DefaultListModel bbListModel;
    DefaultListModel userModel;
    JList nameList, userList;

    // Frames
    JFrame mainFrame;

    // Panes & Panels
    private JTabbedPane tabbedPane;
    private JPanel loginPanel;
    private JPanel bbManager;
    private JPanel viewSchedule;
    private JPanel mngUsers;
    private JPanel launchBB;
    private JScrollPane bbList;
    private JScrollPane usernameList;

    // Labels
    JLabel userLabel, pwdLabel, message, userListLabel;

    // Text Fields
    private JPasswordField pwdField;
    private JTextField userField;

    // Buttons
    private JButton newBB, logIn, logOut, editBB, deleteBB, previewBB, schedBB, delSchedBB, addUser, editUser, startBB, deleteUser;

    // Global Variables for storage of important information
    private static String username;
    private static String token;
    private static List<String> permissions;
    private static boolean login = false;

    // Combo Box
    private JComboBox billboardSelector;
    private List<String> bbIds;
    private String selectedBillboard;


    // -------------------------- Control Panel GUI -------------------------- //

    /**
     *  Basic constructor that gives the GUI the title 'Control Panel'.
     */
    public ControlPanel(){
        super("Control Panel");
    }

    /**
     * Main method for initializing and setting up the GUI.
     * Adds a listener so that when the user closes the window a logout is performed.
     * Set to default size of 1200*900.
     *
     * The initial tabbed panes are created here but their implementation is broken out into separate methods for
     * code readability and screen refresh functionality. 
     *
     * Makes use of the helper method {@link #createButton(String)}
     */
    private void createGUI() {
        mainFrame = new JFrame();
        setSize(new Dimension(WIDTH, HEIGHT));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        // Remove authentication when logging out
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                try {
                    logoutPerformed();
                }
                catch (Exception ignored) {}
            }
        });

        setLayout(new BorderLayout());

        // Setup tabbed layout
        tabbedPane = new JTabbedPane();
        loginPanel = new JPanel(new GridBagLayout());
        bbManager = new JPanel(new GridBagLayout());
        viewSchedule = new JPanel(new GridBagLayout());
        mngUsers = new JPanel(new GridBagLayout());
        launchBB = new JPanel(new GridBagLayout());
        tabbedPane.addTab("User Login", loginPanel);
        tabbedPane.addTab("Manage Billboards", bbManager);
        tabbedPane.addTab("View Schedule", viewSchedule);
        tabbedPane.addTab("Manage Users", mngUsers);
        tabbedPane.addTab("Launch Billboard", launchBB);
        tabbedPane.addChangeListener(this);


        // Disable panels before login
        changePanelSetting(false);


        // Create buttons
        newBB = createButton("Create a new Billboard");
        logIn = createButton("Login");
        logOut = createButton("Logout");
        editBB = createButton("Edit selected Billboard");
        previewBB = createButton("Preview selected Billboard");
        deleteBB = createButton("Delete selected Billboard");
        schedBB = createButton("Schedule a Billboard");
        delSchedBB = createButton("Delete a scheduled billboard");
        addUser = createButton("Add a User");
        editUser = createButton("Edit a User");
        startBB = createButton("Launch The Billboard");
        deleteUser = createButton("Delete User");

        layoutLogInTab();
        layoutLaunchBBTab();

        getContentPane().add(tabbedPane);


    }

    /**
     * Helper method to reduce duplicate code and improve readability.
     * Initializes a button, adds an action listener and labels it with the String passed as an argument.
     * @param str - String to be used as button label
     * @return JButton object
     */
    private JButton createButton(String str) {
        JButton button = new JButton();
        button.setText(str);
        button.addActionListener(this);
        return button;
    }

    /**
     * Method to setup the layout for the login tab.
     * This is the first tab seen when the program is run, and users must login to move to other tabs.
     * Login is implemented with {@link #loginPerformed()}.
     * Logout is implemented with {@link #logoutPerformed()}
     */
    private void layoutLogInTab() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        userLabel = new JLabel();

        // User label
        userLabel.setText("Username: ");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        loginPanel.add(userLabel, gbc);

        // Password Label
        pwdLabel = new JLabel();
        pwdLabel.setText("Password: ");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        loginPanel.add(pwdLabel, gbc);

        // Username field
        userField = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        loginPanel.add(userField, gbc);

        // Password field
        pwdField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        loginPanel.add(pwdField, gbc);

        // Add login button
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        loginPanel.add(logIn, gbc);

        // Add logout button
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        loginPanel.add(logOut, gbc);
    }

    /**
     * Method to setup the layout for the Billboard Manager tab.
     * When method is first called a connection is made to the server and the list of billboards populated.
     * Contains buttons for creating, editing, deleting and previewing billbaords.
     */
    private void layoutBBManagerTab () {
        JPanel newBBManager = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add 'new BB' button
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        newBBManager.add(newBB, gbc);

        // Add 'modify BB' button
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        newBBManager.add(editBB, gbc);

        // Add 'Preview BB' button
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        newBBManager.add(previewBB, gbc);

        // Add 'Delete BB' button
        gbc.gridx = 2;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        newBBManager.add(deleteBB, gbc);

        // Add scrollable pane for list of BBs
        bbListModel = new DefaultListModel();
        nameList = new JList(bbListModel);
        nameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        nameList.addListSelectionListener(this);
        bbList = new JScrollPane(nameList);
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 2;

        // Get initial list of billboards from server
        String[] data = {};
        String[] authentication = {username, token};
        int response = DatabaseNavigator.Response.SELECT_LIST_BILLBOARD.getValue();
        ClientSignal clientSignal = new ClientSignal(response,authentication,data);
        List<String> bbIDs = clientSignal.returnList();
        for (int i = 1; i < bbIDs.size(); i++) {
            bbListModel.addElement(bbIDs.get(i));
        }
        newBBManager.add(bbList, gbc);
        bbManager.removeAll();
        bbManager.add(newBBManager);
        revalidate();
        repaint();
    }

    /**
     * Method to set up the layout for the scheduler tab.
     *
     * When called, this method queries the server for a list of schedules and displays it on a 7-day
     * calendar using ArrayLists.
     *
     * Includes a button 'Schedule Billboards' for users with the appropriate permissions to be add a
     * billboard to the schedule. This launches a new window where the user can select from billboards
     * available on the server and schedule them by day, hour and minute.
     *
     * See {@link AddToSchedule}
     */
    private void layoutSchedulerTab() {

        JPanel newViewSchedule = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add 'Schedule BB' button
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        newViewSchedule.add(schedBB, gbc);

        // Add 'Delete BB' button
        gbc.gridx = 2;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        newViewSchedule.add(delSchedBB, gbc);

        // Add billboard selector for delete bb
        gbc.gridx = 3;
        gbc.gridy = 1;
        String[] data = {};
        String[] authentication = {username, token};
        int response = DatabaseNavigator.Response.SELECT_LIST_BILLBOARD.getValue();
        ClientSignal clientSignal = new ClientSignal(response,authentication,data);
        List<String> returnedValues = clientSignal.returnList();
        bbIds = new ArrayList<>();
        for (int i = 1; i < returnedValues.size(); i++) {
            bbIds.add(returnedValues.get(i));
        }
        billboardSelector = new JComboBox(bbIds.toArray());
        billboardSelector.setSelectedIndex(0);
        billboardSelector.addActionListener(this);
        billboardSelector.setPreferredSize(new Dimension(100,20));
        selectedBillboard = bbIds.get(0);
        newViewSchedule.add(billboardSelector,gbc);

        //Get billboard schedule list from server
        response = DatabaseNavigator.Response.SELECT_SCHEDULES.getValue();
        clientSignal = new ClientSignal(response,authentication,data);
        List<String> schIDs = clientSignal.returnList();

        schIDs.remove(0);

        // Get current system time
        LocalDate currentDate = LocalDate.now();
        ArrayList<String> mondayList = new ArrayList<>();
        ArrayList<String> tuesdayList = new ArrayList<>();
        ArrayList<String> wednesdayList = new ArrayList<>();
        ArrayList<String> thursdayList = new ArrayList<>();
        ArrayList<String> fridayList = new ArrayList<>();
        ArrayList<String> saturdayList = new ArrayList<>();
        ArrayList<String> sundayList = new ArrayList<>();
        ArrayList<ArrayList<String>> scheduleArray = new ArrayList<>();
        scheduleArray.add(mondayList);
        scheduleArray.add(tuesdayList);
        scheduleArray.add(wednesdayList);
        scheduleArray.add(thursdayList);
        scheduleArray.add(fridayList);
        scheduleArray.add(saturdayList);
        scheduleArray.add(sundayList);


        int currentDay = currentDate.getDayOfWeek().getValue();
        List<LocalDate> daysOfWeek = new ArrayList<>();
        for (int i = 1; i < 8; i++)
        {
            int dateDiff = i-currentDay;
            daysOfWeek.add(currentDate.plusDays(dateDiff));
        }

        // Setup headers
        String[] columns = new String[] {" Monday "," Tuesday "," Wednesday "," Thursday "," Friday "," Saturday ",
                " Sunday "};

        JPanel tablePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gridConst = new GridBagConstraints();
        for (int i = 0; i < 7; i++) {
            gridConst.gridx = i;
            gridConst.gridy = 0;
            tablePanel.add(new JLabel(columns[i]),gridConst);
        }

        if (!schIDs.get(0).equals("There are currently no billboards scheduled")) {
            for (int i = 0; i < schIDs.size(); i += 4) {
                LocalDate scheduledDate = LocalDateTime.parse(schIDs.get(i), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss[.S]")).toLocalDate();
                if (daysOfWeek.contains(scheduledDate)) {
                    //System.out.println("added a day");
                    int index = daysOfWeek.indexOf(scheduledDate);
                    String addToSchedule = "";
                    for (int j = i; j < i + 4; j++) {
                        String toAdd = "";
                        if (j % 4 == 0) {
                            toAdd = "Start Time: " + schIDs.get(j) + "\n";
                        }
                        if (j % 4 == 1) {
                            toAdd = "End Time: " + schIDs.get(j) + "\n";
                        }
                        if (j % 4 == 2) {
                            toAdd = "Billboard Name: " + schIDs.get(j) + "\n";
                        }
                        if (j % 4 == 3) {
                            toAdd = "Scheduler: " + schIDs.get(j);
                        }
                        addToSchedule = addToSchedule + toAdd;
                        //System.out.println(schIDs.get(j));
                    }
                    scheduleArray.get(index).add(addToSchedule);
                }
            }
            //System.out.println(scheduleArray.toString());
            //Setting scheduleArray to be a Object[][]
            String[][] scheduleForTable = new String[scheduleArray.size()][];
            for (int i = 0; i < scheduleArray.size(); i++) {
                ArrayList<String> row = scheduleArray.get(i);
                scheduleForTable[i] = row.toArray(new String[row.size()]);
            }

            //Schedule table setup
            int maxColumnLength = 0;
            for (int i = 0; i < scheduleForTable.length; i++) {
                if (maxColumnLength < scheduleForTable[i].length) {
                    maxColumnLength = scheduleForTable[i].length;
                }
            }

            // Create innards of table
            for (int i = 0; i < 7; i++) {
                int j = 1;
                for (String string : scheduleForTable[i]) {
                    JTextArea textBox = new JTextArea(string);
                    textBox.setEditable(false);
                    textBox.setFont(new Font(Font.SERIF, Font.PLAIN, 10));
                    JPanel cell = new JPanel();
                    cell.setBorder(BorderFactory.createLineBorder(Color.black));
                    cell.add(textBox);
                    gridConst.gridx = i;
                    gridConst.gridy = j;
                    tablePanel.add(cell, gridConst);
                    j++;
                }
            }
        }
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        newViewSchedule.add(tablePanel, gbc);
        viewSchedule.removeAll();
        viewSchedule.add(newViewSchedule);
        revalidate();
        repaint();
}

    /**
     * Method to setup the layout for the user management tab.
     *
     * When first called, the server is queried and a list populated with usernames existing on the server.
     *
     * Includes the buttons 'Add User' and 'Delete User' so that users with the appropriate permissions
     * can make changes to the users on the server.
     *
     * See: {@link AddUserForm}
     */
    private void layoutUserMngTab() {
        JPanel newMngUsers = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add 'User List' label
        userListLabel = new JLabel("Users");
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        newMngUsers.add(userListLabel, gbc);


        // Add list of users from BB
        userModel = new DefaultListModel();
        userList = new JList(userModel);
        usernameList = new JScrollPane(userList);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 3;


        // Get initial list of users from server
        int response = DatabaseNavigator.Response.SELECT_LIST_USERS.getValue();
        String[] data = {};
        String[] authentication = {username, token};
        ClientSignal clientSignal = new ClientSignal(response,authentication,data);
        List<String> userIDs = clientSignal.returnList();
        for (int i = 1; i < userIDs.size(); i++) {
            userModel.addElement(userIDs.get(i));
        }
        newMngUsers.add(usernameList, gbc);

        // Add 'Add User' button
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        newMngUsers.add(addUser, gbc);

        // Add 'Edit User' button
        gbc.gridx = 2;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        newMngUsers.add(editUser, gbc);


        // Add delete user button
        gbc.gridx = 3;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        newMngUsers.add(deleteUser, gbc);


        mngUsers.removeAll();
        mngUsers.add(newMngUsers);
        revalidate();
        repaint();
    }

    /**
     * Method to create the tab that lets users launch the billboard viewer.
     *
     * Simple screen with just one button: Launch Billboard.
     *
     * See: {@link Billboard}
     */
    private void layoutLaunchBBTab() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        launchBB.add(startBB, gbc);
    }

    /**
     * Implementation of the run() method from interface Runnable. Called after the constructor.
     *
     * Gets the system look and feel, runs {@link #createGUI()} and sets preferred size and location before
     * packing and setting to visible.
     *
     */
    @Override
    public void run() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        createGUI();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setLocation(100, 100);
        pack();
        setVisible(true);
    }

    // -------------------------- Action Listeners -------------------------- //

    /**
     * Implementation of method from ActionListener interface.
     *
     * Contains much of the functionality for different buttons in the class.
     * @param e the parameter passed is the action event whenever one of the buttons is used
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();

        // Deletes the selected billboard from the server.
        if (src == deleteBB) {
            // Gets the currently selected billboard in the list.
            String[] bbName = {bbSelected};
            System.out.println(bbSelected);
            if (JOptionPane.showConfirmDialog(null, "Are you sure you want to " +
                    "delete this billboard? It will be permanently removed from the server.", "Delete billoard?", JOptionPane.YES_NO_OPTION) ==
                    JOptionPane.YES_OPTION) {
                int response = DatabaseNavigator.Response.DELETE_BILLBOARD_ADMIN.getValue();
                String[] authentication = {username,token};
                ClientSignal clientSignal = new ClientSignal(response,authentication,bbName);
                layoutBBManagerTab();
            }
        }

        // Run CreateBillboards class. On close, refresh the list of BBs so newly created BBs populate the list.
        if (src == newBB) {
            CreateBillboards createBB = new CreateBillboards();
            createBB.run();
            createBB.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            createBB.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    e.getWindow().dispose();
                    //System.out.println("Window closed");
                    layoutBBManagerTab();
                }
            });
        }

        // If login is pressed
        if (src == logIn && !login) {
            loginPerformed();
        }

        // If logout is pressed
        if (src == logOut && login) {
            logoutPerformed();
        }

        // If edit billboard is pressed
        if (src == editBB) {
            if (bbSelected == null) {
                JOptionPane.showMessageDialog(this, "No selection has been made!", "Error!", JOptionPane.ERROR_MESSAGE);
            }
            if (bbSelected != null) {

                // Send signal with authentication and receive response
                String[] data = {bbSelected};
                String[] authentication = {username, token};
                int response = DatabaseNavigator.Response.SELECT_ROW_BILLBOARD.getValue();
                ClientSignal clientSignal = new ClientSignal(response, authentication, data);
                List<String> bbDetails = clientSignal.returnList();
                try {
                    CreateBillboards createBB = new CreateBillboards(bbSelected, bbDetails.get(2));
                    createBB.run();
                } catch (IOException | SAXException ioException) {
                    ioException.printStackTrace();
                }
            }
        }

        // Preview Billboard button
        if (src == previewBB) {
            if (bbSelected == null) {
                JOptionPane.showMessageDialog(this, "No selection has been made!", "Error!", JOptionPane.ERROR_MESSAGE);
            }
            if (bbSelected != null) {
                // Send signal with authentication and receive response
                String[] data = {bbSelected};
                String[] authentication = {username, token};
                int response = DatabaseNavigator.Response.SELECT_ROW_BILLBOARD.getValue();
                ClientSignal clientSignal = new ClientSignal(response, authentication, data);
                List<String> bbDetails = clientSignal.returnList();
                try {
                    Billboard preview = new Billboard();
                    TempXMLCreator.makeTempXML(bbDetails.get(2),"temp.xml");
                    XMLParser parser = new XMLParser("temp.xml");
                    preview.messageText = parser.getMessage();
                    preview.informationText = parser.getInformation();
                    preview.messageColor = parser.getMessageColour();
                    preview.informationColor = parser.getInformationColour();
                    preview.backgroundColor = parser.getBillboardColour();
                    if (parser.getPictureURL() != null) {
                        preview.isData = false;
                        preview.imageString = parser.getPictureURL();
                    }
                    if (parser.getPictureData() != null) {
                        preview.isData = true;
                        preview.imageString = parser.getPictureData();
                    }
                    preview.createBillboardDisplay();
                } catch (IOException | SAXException ex) {
                    ex.printStackTrace();
                }
            }
        }

        // Schedule Billboard button
        if (src == schedBB) {
            AddToSchedule scheduler = new AddToSchedule();
            scheduler.authentication = new String[]{username, token};
            scheduler.run();
            scheduler.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            scheduler.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    e.getWindow().dispose();
                    //System.out.println("Window closed");
                    layoutSchedulerTab();
                }
            });
        }

        if (src == delSchedBB) {
            String[] data = {selectedBillboard};
            String[] authentication = {username, token};
            int response = DatabaseNavigator.Response.DELETE_BILLBOARD_SCHEDULE.getValue();
            ClientSignal clientSignal = new ClientSignal(response, authentication, data);
            layoutSchedulerTab();
        }

        if (src == billboardSelector) {
            selectedBillboard = bbIds.get(billboardSelector.getSelectedIndex());
        }

        // Start Billboard button
        if (src == startBB) {
            String[] emptyStringArray = {""};
            Billboard.main(emptyStringArray);
        }

        // Add user button
        if (src == addUser) {
            AddUserForm userForm = new AddUserForm(username, token);
        }

        // The deletion of users
        if (src == deleteUser) {
            userListIndex = userList.getSelectedIndex();
            String text = userModel.getElementAt(userListIndex).toString();

            // Check for no selection
            if (!text.isEmpty()) {
                // Check for self deletion
                if (!text.equals(username)) {

                    // Send signal to delete
                    int response = DatabaseNavigator.Response.DELETE_USER.getValue();
                    String[] data = {text};
                    String[] authentication = {username, token};
                    new ClientSignal(response, authentication, data);
                    JOptionPane.showMessageDialog(new JFrame(), "User has been deleted");


                } else {
                    JOptionPane.showMessageDialog(new JFrame(), "You cannot delete yourself", "Self Delete", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(new JFrame(), "You need to select a user to delete", "User Delete", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Checks to see if a tab has been switched. The tab that the user switches to is then refreshed by
     * calling it's corresponding initialization method.
     * @param e ChangeEvent object
     */
    public void stateChanged(ChangeEvent e) {

        int selectedIndex = tabbedPane.getSelectedIndex();
        switch(selectedIndex) {
            case 1:
                // Add a method for each of these so that when they switch to the tab it updates the information
                layoutBBManagerTab();
                break;
            case 2:
                layoutSchedulerTab();
                break;
            case 3:
                layoutUserMngTab();
                break;
            default:
                break;
        }
    }

    /**
     * Detects if the user has selected a billboard in the list on the Billboard Manager tab.
     *
     * Sets a global variable to the selection for passing to functions such as "Edit Billboard" or
     * "Delete Billboard".
     * @param e ListSelectionEvent object
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        bbIndexSelected = nameList.getSelectedIndex();
        bbSelected = bbListModel.getElementAt(bbIndexSelected).toString();
    }

    /**
     * The login method which returns a result whether an error message or the token, username, and permissions of the user.
     * Communication with the server for user authentication is encrypted.
     */
    private void loginPerformed() {
        // Get the text field information
        String userText = userField.getText();
        String password = String.valueOf(pwdField.getPassword());

        // Create variables for first signal method (grabbing salt)
        int response = DatabaseNavigator.Response.SELECT_SALT.getValue();
        String[] data = {userText};
        ClientSignal clientSignal = new ClientSignal(response, data);
        List<String> result = clientSignal.returnList();

        // Reset fields
        userField.setText("");
        pwdField.setText("");

        // Check if the returned salt was successful for the user
        if (result.contains("Success")) {

            // Generate Safe Password
            String salt = result.get(1);
            String hashedPassword = DatabaseEncryption.generateSecurePassword(password, salt);

            // Login attempt
            int response2 = DatabaseNavigator.Response.SELECT_LOGIN_REQUEST.getValue();
            String[] userInfo = {userText, hashedPassword};
            ClientSignal loginSignal = new ClientSignal(response2, userInfo);
            List<String> loginResult = loginSignal.returnList();

            if (loginResult.contains("Success")) {
                // Update global variables
                token = loginResult.get(1);
                username = loginResult.get(2);
                permissions = Arrays.asList(loginResult.get(3).split("\\s+"));

                // Allow access to panels
                changePanelSetting(true);

                // Success Message
                JOptionPane.showMessageDialog(new JFrame(), "Login Successful");

                // Error Checking for the login
            } else {
                JOptionPane.showMessageDialog(new JFrame(), loginResult.get(1), "Login Failure", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(new JFrame(), result.get(1), "Login Failure", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * When the user logs out of the control panel remove authentication and lock the the control panel again
     */
    private void logoutPerformed() {
        // Logout by sending signal to server
        int response2 = DatabaseNavigator.Response.LOG_OUT.getValue();
        String[] data = {};
        String[] authentication = {username, token};
        ClientSignal logoutSignal = new ClientSignal(response2, authentication, data);
        List<String> logoutResult = logoutSignal.returnList();

        if (logoutResult.contains("Success")) {
            userField.setText("");
            pwdField.setText("");
            changePanelSetting(false);
        }
    }


    /**
     * Disables or Enables the tabs on the control panel
     * @param change the current boolean value for the tabs to be enabled or disabled
     */
    private void changePanelSetting(boolean change) {
        tabbedPane.setEnabledAt(1, change);
        tabbedPane.setEnabledAt(2, change);
        tabbedPane.setEnabledAt(3, change);

        login = change;
    }

    /**
     * Runs the Control Panel.
     *
     * Executes the runnable on the AWT event-dispatching thread for thread-safety.
     * @param args command line args unused
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new ControlPanel());
    }
}
