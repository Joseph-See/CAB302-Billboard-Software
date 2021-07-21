package ControlPanel;

import ClientSideHelpers.ClientSignal;
import Database.DatabaseNavigator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * This creates a pop-out that allows a user with the scheduling permission to add a billboard the the schedule
 *
 */
public class AddToSchedule extends JFrame implements ActionListener, Runnable, ItemListener {
    private JPanel panel = new JPanel();
    private JTextArea startToFinish = new JTextArea();
    private static LocalDateTime dateTime = LocalDateTime.now();
    private int hourS, hourF, minuteS, minuteF;
    private Boolean[] selectedDays = {false,false,false,false,false,false,false};
    private JComboBox hourStartSelector, hourFinishSelector, minuteStartSelector, minuteFinishSelector,billboardSelector;
    private String selectedBillboard;
    private List<String> bbIds;
    private JButton addToSchedule = new JButton("Add this to the schedule");
    /** This requires {user,token} to be set in order for it to successfully schedule a billboard*/
    public String[] authentication;
    // Create all of the checkbox buttons - in order of M,T,W,T,F,S,S
    private JCheckBox[] checkDays = {null,null,null,null,null,null,null};
    private static String[] days = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};

    /**
     * This function is used to format the label for the currently selected schedule information
     */
    private void s2FFormatter() {
        List<String> daysSelected = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            if (selectedDays[i]) {
                daysSelected.add(DayOfWeek.of(i+1).toString());
            }
        }
        if (daysSelected.isEmpty()) {
            startToFinish.setText("Please Select a Day. \nTrying to schedule without a day will do nothing.");
        }
        else {
            startToFinish.setText("Add billboard " + selectedBillboard + " on days: \n" +
                    String.join(", ", daysSelected) + "\n" +
                    "From " + String.format("%02d", hourS) + String.format("%02d", minuteS) +
                    " to " + String.format("%02d", hourF) + String.format("%02d", minuteF));
        }
    }

    /**
     * This function creates the GUI of the scheduler
     */
    private void createGUI(){
        // Tries to set look and feel to system defaults
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        // Creates a list of days of the week and adds them to a comboBox
        JLabel selectDay = new JLabel("Select days to schedule a billboard:    ");
        constraints.gridx = 1;
        constraints.gridy = 1;
        panel.add(selectDay,constraints);
        JPanel dayCheckBox = new JPanel(new GridBagLayout());
        for (int i = 0; i < 7; i++) {
            checkDays[i] = new JCheckBox(days[i]);
            constraints.gridx = 1;
            constraints.gridy = i;
            dayCheckBox.add(checkDays[i],constraints);
            checkDays[i].addItemListener( this);
        }
        constraints.gridx = 2;
        constraints.gridy = 1;
        panel.add(dayCheckBox,constraints);

        List<String> hours = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hours.add(String.valueOf(i));
        }
        // the hour the billboard starts
        JLabel selectHourS = new JLabel("Select the hour it should start:   ");
        constraints.gridx = 1;
        constraints.gridy = 2;
        panel.add(selectHourS,constraints);
        hourStartSelector = new JComboBox(hours.toArray());
        hourStartSelector.setSelectedIndex(0);
        hourStartSelector.addActionListener(this);
        hourStartSelector.setPreferredSize(new Dimension(100,20));
        constraints.gridx = 2;
        constraints.gridy = 2;
        panel.add(hourStartSelector,constraints);

        //The hour it ends
        JLabel selectHourF = new JLabel("Select the hour it should finish:  ");
        constraints.gridx = 1;
        constraints.gridy = 4;
        panel.add(selectHourF,constraints);
        hourFinishSelector = new JComboBox(hours.toArray());
        hourFinishSelector.setSelectedIndex(0);
        hourFinishSelector.addActionListener(this);
        hourFinishSelector.setPreferredSize(new Dimension(100,20));
        constraints.gridx = 2;
        constraints.gridy = 4;
        panel.add(hourFinishSelector,constraints);

        List<String> minutes = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minutes.add(String.valueOf(i));
        }

        //The minute it starts
        JLabel selectMinuteS = new JLabel("Select the minute it should start:   ");
        constraints.gridx = 1;
        constraints.gridy = 3;
        panel.add(selectMinuteS,constraints);
        minuteStartSelector = new JComboBox(minutes.toArray());
        minuteStartSelector.setSelectedIndex(0);
        minuteStartSelector.addActionListener(this);
        minuteStartSelector.setPreferredSize(new Dimension(100,20));
        constraints.gridx = 2;
        constraints.gridy = 3;
        panel.add(minuteStartSelector,constraints);

        //the minute it ends
        JLabel selectMinuteF = new JLabel("Select the minute it should finish:  ");
        constraints.gridx = 1;
        constraints.gridy = 5;
        panel.add(selectMinuteF,constraints);
        minuteFinishSelector = new JComboBox(minutes.toArray());
        minuteFinishSelector.setSelectedIndex(0);
        minuteFinishSelector.addActionListener(this);
        minuteFinishSelector.setPreferredSize(new Dimension(100,20));
        constraints.gridx = 2;
        constraints.gridy = 5;
        panel.add(minuteFinishSelector,constraints);

        // Loads all billboards into list to look at
        JLabel selectBill = new JLabel("Select the Billboard to schedule:  ");
        constraints.gridx = 1;
        constraints.gridy = 6;
        panel.add(selectBill,constraints);
        String[] data = {};
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
        constraints.gridx = 2;
        constraints.gridy = 6;
        panel.add(billboardSelector,constraints);

        // Write the box that tells them the values they are trying to add to the schedule
        s2FFormatter();
        startToFinish.setEditable(false);
        constraints.gridx = 1;
        constraints.gridy = 7;
        constraints.gridwidth = 2;
        panel.add(startToFinish,constraints);

        // Adds button that adds values to schedule
        constraints.gridx = 1;
        constraints.gridy = 8;
        constraints.gridwidth = 2;
        addToSchedule.addActionListener(this);
        panel.add(addToSchedule,constraints);


        getContentPane().add(panel);
        setPreferredSize(new Dimension(900, 500));
        setLocation(new Point(100, 100));
        pack();
        setVisible(true);
    }

    /**
     * This refreshes the label that displays the selected schedule information
     */
    private void updateS2F () {
        GridBagConstraints constraints = new GridBagConstraints();
        s2FFormatter();
        constraints.gridx = 1;
        constraints.gridy = 7;
        constraints.gridwidth = 2;
        panel.remove(startToFinish);
        panel.add(startToFinish,constraints);
        revalidate();
        repaint();
    }

    /**
     * This returns a date-time in the format expected by the database using values selected by the database
     * @param day The day selected by the user
     * @param hour The hour selected by the user
     * @param minute The minute selected by the user
     * @return A string with the format of the time expected by the database
     */
    private static String timeString(int day, int hour, int minute) {
        int date = dateTime.getDayOfWeek().getValue();
        int dateDiff = day-date;
        LocalDateTime time = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        //System.out.println(time.plusDays(dateDiff).getDayOfWeek());
        return time.plusDays(dateDiff).withHour(hour).withMinute(minute).withSecond(0).format(formatter);
    }

    /**
     * Runs the Scheduler program
     */
    @Override
    public void run() {
        createGUI();
    }

    /**
     * Iterates through the checkboxes searching for which one was clicked, and sets the state to match
     * @param e The event that was caused by interacting with a checkbox
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        Object src = e.getSource();
        for (int i = 0; i < 7; i++) {
            if (src == checkDays[i]) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    selectedDays[i] = true;
                }
                else if (e.getStateChange() == ItemEvent.DESELECTED) {
                    selectedDays[i] = false;
                }
            }
        }
        updateS2F();
    }

    /**
     * This detects when the user changes a dropdown or presses the button within the program, and runs an appropriate function
     * @param e This is the action detected by the user
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == hourStartSelector) {
            hourS = hourStartSelector.getSelectedIndex();
            updateS2F();
        }
        if (src == hourFinishSelector) {
            hourF = hourFinishSelector.getSelectedIndex();
            updateS2F();
        }
        if (src == minuteStartSelector) {
            minuteS = minuteStartSelector.getSelectedIndex();
            updateS2F();
        }
        if (src == minuteFinishSelector) {
            minuteF = minuteFinishSelector.getSelectedIndex();
            updateS2F();
        }
        if (src == billboardSelector) {
            selectedBillboard = bbIds.get(billboardSelector.getSelectedIndex());
            //System.out.println(bbIds.get(billboardSelector.getSelectedIndex()));
            updateS2F();
        }
        if (src == addToSchedule) {
            for (int i = 0; i <7; i++) {
                if (selectedDays[i]) {
                    String startTime = timeString(i + 1, hourS, minuteS);
                    String endTime = timeString(i + 1, hourF, minuteF);
                    String[] data = new String[]{startTime, endTime, selectedBillboard};
                    int response = DatabaseNavigator.Response.INSERT_TIME.getValue();
                    new ClientSignal(response, authentication, data);
                }
            }
            //System.out.println(startTime);
            //System.out.println(endTime);
            this.dispose();
        }
    }
}
