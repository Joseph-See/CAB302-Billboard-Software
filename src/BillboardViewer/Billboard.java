package BillboardViewer;

import ClientSideHelpers.ClientSignal;
import CreateBillboards.TempXMLCreator;
import CreateBillboards.XMLParser;
import Database.DatabaseNavigator;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.net.URL;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Creates a billboard object that contains the values for the desired billboard.
 * Can be run to create a fullscreen borderless view of the billboard.
 * Has a public function that returns a panel - createBillboardPanel, that creates a panel to be used within a JFrame object
 * Has a public function that allows it to display a fullscreen preview - createBillboardDisplay, for a preview
 * If a value is null or an empty string, the billboard assumes those portions do not exist.
 * All colours are hexadecimal strings that are decoded within, for easy of use when receiving values from a string
 *
 */

public class Billboard extends JFrame implements Runnable {
    private static final int MIN_FONT_SIZE=5; // The minimum font size for the billboard
    private static final int MAX_FONT_SIZE=300; // The maximum possible font size of the billboard
    /** The string that determines the message text */
    public String messageText = null;
    /** The string containing image information */
    public String imageString = null;
    /** The string that determines the information text */
    public String informationText = null;
    /** The Hexadecimal code for the background colour */
    public String backgroundColor = "#FFFFFF";
    /** The Hexadecimal code for the message colour */
    public String messageColor = "#000000";
    /** The Hexadecimal code for the information colour */
    public String informationColor = "#000000";
    /** The bool that determines whether the image is a BASE64 data string or a link */
    public boolean isData = false;
    private JPanel billboardPanel; // The Panel that the billboard is stored on
    private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // The size of the screen that the billboard is going to display on
    private Timer timer = new Timer(); // The timer for checking the server for updates
    private String currentBillboard = ""; // The current billboard as a string to determine if the server is sending a different billboard so that it may update

    /**
     * Shortcut for making a panel with the correct background colour.
     * @return Returns a panel with the background colour set.
     */
    private JPanel createPanel () {
        JPanel newPanel = new JPanel();
        newPanel.setBackground(Color.decode(Objects.requireNonNullElse(backgroundColor, "#FFFFFF")));
        return newPanel;
    }

    /**
     * Resizes the text to fit the size of the billboard.
     * @param boxWidth The width of the text box.
     * @param boxHeight The height of the text box.
     * @param text The text to be resized.
     * @param title A bool that determines whther it resizes to one line or multiple.
     * @param maxSize The maximum font size.
     * @return The maximum font size that would fit the text in the text box.
     */
    private Font resizeText (double boxWidth,double boxHeight, String text, boolean title, int maxSize) {
        Font outputFont = new Font(Font.SANS_SERIF,Font.BOLD,MIN_FONT_SIZE);
        int current_font_size = MIN_FONT_SIZE+1;
        while(current_font_size < maxSize) {
            Font biggerFont = new Font(Font.SANS_SERIF,Font.BOLD,current_font_size);
            FontMetrics metrics = getFontMetrics(biggerFont);
            if(title){
                if(metrics.getHeight() < boxHeight && metrics.stringWidth(text) < boxWidth){
                    outputFont = biggerFont;
                } else {
                    break;
                }
            }
            else {
                int lineCount = (int)(Math.ceil(metrics.stringWidth(text)/boxWidth))+1;
                if (lineCount * metrics.getHeight() < boxHeight) {
                    outputFont = biggerFont;
                } else {
                    break;
                }

            }
            ++current_font_size;
        }
        return outputFont;
    }

    /**
     * Rescales the image to the correct size for the particular layout of the billboard.
     * @param originalWidth Original image width.
     * @param originalHeight Original image height.
     * @param boundWidth The maximum width of the resized image.
     * @param boundHeight The maximum height of the resized image.
     * @return The biggest image that will fit in the borders.
     */
    private Dimension imageRescale (int originalWidth, int originalHeight, int boundWidth, int boundHeight) {
        int newWidth = boundWidth;
        int newHeight = (newWidth * originalHeight) / originalWidth;
        if (newHeight > boundHeight) {
            newHeight = boundHeight;
            newWidth = (newHeight * originalWidth) / originalHeight;
        }
        return new Dimension(newWidth, newHeight);
    }

    /**
     * Creates a panel for use in a JFrame that will have the entire billboard inside of it.
     * @param billboardSize A dimension object that has the desired size of the billboard.
     * @return A JPanel object with the billboard sized to fit.
     */
    public JPanel createBillboardPanel (Dimension billboardSize) {
        JPanel billboardPanel = createPanel();
        billboardPanel.setSize(billboardSize);
        double billboardWidth = billboardSize.width;
        double billboardHeight = billboardSize.height;
        billboardPanel.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.gridx = 1;
        int maxInfoSize = MAX_FONT_SIZE;

        /* This section creates a message, and scales it appropriately to the size of the screen,
         * if message contains information
         */
        if (messageText != null && !messageText.isEmpty()) {
            JPanel pnlTop = createPanel();
            JLabel labTop = new JLabel(messageText,JLabel.CENTER);
            labTop.setForeground(Color.decode(Objects.requireNonNullElse(messageColor, "#000000")));
            double boxWidth = billboardWidth*0.9;
            double boxHeight = billboardHeight/4;
            Font labelFont = resizeText(boxWidth,boxHeight,messageText,true,MAX_FONT_SIZE);
            maxInfoSize = (int)Math.ceil(labelFont.getSize()*0.4);
            maxInfoSize = Math.max(maxInfoSize, MIN_FONT_SIZE);
            labTop.setFont(labelFont);
            Dimension messageSize = new Dimension();
            messageSize.setSize(boxWidth, boxHeight);
            labTop.setPreferredSize(messageSize);
            pnlTop.setPreferredSize(messageSize);
            pnlTop.add(labTop);
            constraints.gridy = 1;
            constraints.weighty = 0.33;
            billboardPanel.add(labTop,constraints);
        }

        /* This section displays an image, and scales it appropriately to the size of the screen and elements
         * It expects the isData to be set to either true or false depending on if the image is being read as a link or a Base64 data string
         */
        if (imageString != null && !imageString.isEmpty()) {
            JPanel pnlMid = createPanel();
            double scale = messageText != null && informationText != null ? 1.0/3 : 0.5;
            BufferedImage image = null;
            if (isData) {
                byte[] imageByte = Base64.getDecoder().decode(imageString);
                ByteArrayInputStream bAIS = new ByteArrayInputStream(imageByte);
                try {
                    image = ImageIO.read(bAIS);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                URL url;
                try {
                    url = new URL(imageString);
                    image = ImageIO.read(url);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (image != null) {
                int boxWidth = (int) Math.ceil(billboardWidth * scale);
                int boxHeight = (int) Math.ceil(billboardHeight * scale);
                Dimension imageSize = imageRescale(image.getWidth(), image.getHeight(), boxWidth, boxHeight);
                Image scaledImage = image.getScaledInstance(imageSize.width, imageSize.height, Image.SCALE_SMOOTH);
                JLabel labMid = new JLabel(new ImageIcon(scaledImage));
                pnlMid.setPreferredSize(imageSize);
                pnlMid.add(labMid);
                constraints.gridy = 2;
                constraints.weighty = (messageText == null || informationText == null) ? 0.66 : 0.33;
                billboardPanel.add(labMid, constraints);
            }
        }

        /* This section creates an information box, and scales it appropriately to the size of the screen,
         * it is forced to be 40% the size of the header, unless that value would be less than the minimum font size,
         * in which case it uses the minimum font size
         */
        if (informationText != null && !informationText.isEmpty()) {
            JPanel pnlBot = createPanel();
            JLabel labBot = new JLabel("<html><div style='text-align: center;'>" + informationText + "</div></html>",JLabel.CENTER);
            labBot.setForeground(Color.decode(Objects.requireNonNullElse(informationColor, "#000000")));
            Dimension infoSize = new Dimension();
            double boxWidth = billboardWidth*0.75;
            double boxHeight = billboardHeight/4;
            infoSize.setSize(boxWidth, boxHeight);
            labBot.setPreferredSize(infoSize);
            Font infoFont = resizeText(boxWidth,boxHeight,informationText,false,maxInfoSize);
            labBot.setFont(infoFont);
            pnlBot.setPreferredSize(infoSize);
            pnlBot.add(labBot);
            constraints.gridy = 3;
            constraints.weighty = 0.33;
            billboardPanel.add(labBot,constraints);
        }
        return billboardPanel;
    }

    /**
     * Creates a fullscreen view of the billboard, using currently inputted values, and without updating to the server.
     */
    public void createBillboardDisplay () {
        setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        setExtendedState(MAXIMIZED_BOTH);
        billboardPanel = createBillboardPanel(screenSize);
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 27) {
                    dispose();
                    timer.cancel();
                    timer.purge();
                }
            }
        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dispose();
                timer.cancel();
                timer.purge();
            }
        });
        getContentPane().add(billboardPanel);
        repaint();
        setUndecorated(true);
        setVisible(true);
    }


    /**
     * Gets the current time in
     * @return the current time in yyyy-MM-dd HH:mm:ss
     */
    private static String getCurrentTime() {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(formatter);
    }

    /**
     * Allows the billboard to run as it's own thread
     */
    @Override
    public void run() {
        createBillboardDisplay();
        timer.schedule(new TimerTask() {
            public void run() {
                try {
                    // Make the connection and receive the data from the server
                    int response = DatabaseNavigator.Response.SELECT_BILLBOARD_VIEWER.getValue();
                    String[] time = new String[]{getCurrentTime(), getCurrentTime()};
                    ClientSignal clientSignal = new ClientSignal(response, time);
                    List<String> results = clientSignal.returnList();

                    // Create the GUI Billboard
                    String sentBillboard = results.get(1);
                    System.out.println(sentBillboard);
                    if (!currentBillboard.equals(sentBillboard)) {
                        TempXMLCreator.makeTempXML(sentBillboard,"tempBill.xml");
                        XMLParser billboardXML = new XMLParser("tempBill.xml");
                        isData = billboardXML.getPictureData() != null;
                        messageText = billboardXML.getMessage();
                        imageString = isData ? billboardXML.getPictureData() : billboardXML.getPictureURL();
                        informationText = billboardXML.getInformation();
                        backgroundColor = billboardXML.getBillboardColour() != null ? billboardXML.getBillboardColour() : "#FFFFFF";
                        messageColor = billboardXML.getMessageColour() != null ? billboardXML.getMessageColour() : "#000000";
                        informationColor = billboardXML.getInformationColour() != null ? billboardXML.getInformationColour() : "#000000";
                        getContentPane().remove(billboardPanel);
                        billboardPanel = createBillboardPanel(screenSize);
                        getContentPane().add(billboardPanel);
                        revalidate();
                        repaint();
                        currentBillboard = sentBillboard;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    isData = true;
                    messageText = "There has been an error accessing the database";
                    imageString = null;
                    informationText = null;
                    backgroundColor = "#FFFFFF";
                    messageColor = "#000000";
                    informationColor = "#000000";
                    getContentPane().remove(billboardPanel);
                    billboardPanel = createBillboardPanel(screenSize);
                    getContentPane().add(billboardPanel);
                    revalidate();
                    repaint();
                    currentBillboard = "";
                }
            }
        }, 0, 15000);
    }

    /**
     * Allows the program to run
     * @param args Should be nothing always as it isn't used
     */
    public static void main(String[] args) {
        Thread billboardThread = new Thread(new Billboard());
        billboardThread.start();
    }
}