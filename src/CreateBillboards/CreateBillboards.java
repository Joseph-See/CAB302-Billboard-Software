package CreateBillboards;

import BillboardViewer.Billboard;
import ClientSideHelpers.ClientSignal;
import Database.DatabaseNavigator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.List;

/**
 * The CreateBillboards class allows for the creation, import and export of billboards in XML format.
 * The XML format is compatible with the parser used for the billboard viewer application.
 * Images can be imported as either URL links or as files from the computer.
 * Handles import of XML files for editing, either as an XML file locally or through passing an XML string
 * through the constructor.
 *
 * Exports billboards to the server with no local storage.
 *
 * Includes a previewer implemented from the BillboardViewer class.
 *
 */
public class CreateBillboards extends JFrame implements ActionListener, Runnable {

    private static final String title = "Create Billboards";
    // Panels and buttons necessary for the GUI
    private JPanel previewDisplay;
    private JPanel pnlTwo;
    private JPanel pnlThree;
    private JPanel pnlFour;
    private JPanel pnlBtn;

    private JButton btnImport;
    private JButton btnExport;
    private JButton btnName;
    private JButton btnMessage;
    private JButton btnInformation;
    private JButton btnMsgColour;
    private JButton btnInfoColour;
    private JButton btnBkgColour;
    private JButton btnImageURL;
    private JButton btnImageFile;
    private JButton btnClrAllSettings;
    private JButton btnClrImage;

    // Strings used to store information either input by the user or imported from an XML file.
    private String bbName;
    private String bbMsg;
    private String bbInfo;
    private String bbMsgColor;
    private String bbInfoColor;
    private String bbBkgColour;
    private String bbURL;
    private String bbImageBase64;
    private Boolean bbIsData;

    // Preview Billboard Panel
    private Billboard preview = new Billboard();
    private Dimension previewSize = new Dimension();

    /**
     * Constructs frame with no data loaded.
     */
    public CreateBillboards() {
        super(title);
    }

    /**
     * Constructs frame with a preloaded billboard, for use with Edit Billboards functionality.
     *
     * An IOException is thrown by makeTempXML if there it is unable to create a temporary XML file.
     * This can be due to system permissions.
     *
     * A SAXException is thrown by the XML parser if an invalid XML file is passed as input.
     *
     * @param name the name of the billboard
     * @param xmlData the XML data for the billboard
     * @throws IOException when the file cannot be found
     * @throws SAXException thrown by the XML parser if an invalid XML file is passed as input.
     */
    public CreateBillboards(String name, String xmlData) throws IOException, SAXException {
        super(title);
        makeTempXML(xmlData);
        XMLParser parser = new XMLParser("temp.xml");

        bbName = name;
        bbMsg = parser.getMessage();
        preview.messageText = parser.getMessage();
        bbInfo = parser.getInformation();
        preview.informationText = parser.getInformation();
        bbMsgColor = parser.getMessageColour();
        preview.messageColor = parser.getMessageColour();
        bbInfoColor = parser.getInformationColour();
        preview.informationColor = parser.getInformationColour();
        bbBkgColour = parser.getBillboardColour();
        preview.backgroundColor = parser.getBillboardColour();
        bbURL = parser.getPictureURL();
        if (parser.getPictureURL() != null) {
            preview.isData = false;
            preview.imageString = parser.getPictureURL();
        }
        bbImageBase64 = parser.getPictureData();
        if (parser.getPictureData() != null) {
            preview.isData = true;
            preview.imageString = parser.getPictureData();
        }
    }

    /**
     * Creates a temp XML file for when needed.
     *
     * If the program does not have permissions to create the temporary file on the system, or if null
     * values are passed, an IOException will be passed.
     *
     * @author Jack Sheppeard
     * @param xmlData the xml data in string format
     * @throws IOException is thrown if the file cannot be created
     */
    public static void makeTempXML(String xmlData) throws IOException {
        java.io.FileWriter fw = new java.io.FileWriter("temp.xml");
        fw.write(xmlData);
        fw.close();
    }

    /**
     * The method to initialize and setup the billboard. Sets default size, close behavior and layout.
     * The preview is insert into the frame, and buttons are initialized using a helper method.
     * See also {@link #createButton(String)}
     * See also {@link #createPanel(Color)}     *
     */
    private void createGUI() {
        // Window display parameters -- remove or alter when putting into tabbed pane
        setSize(new Dimension(500, 500));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        //Create Billboard Preview
        previewSize.width = (int)Math.round(getWidth()*0.96);
        previewSize.height = (int)Math.round(getHeight()-200);
        previewDisplay = preview.createBillboardPanel(previewSize);

        // Set background colours
        pnlBtn = createPanel(Color.GRAY);
        pnlTwo = createPanel(Color.GRAY);
        pnlThree = createPanel(Color.GRAY);
        pnlFour = createPanel(Color.GRAY);

        // Create buttons
        btnExport = createButton("Export Billboard");
        btnImport = createButton("Import Billboard");
        btnName = createButton("Name Billboard");
        btnMessage = createButton("Enter Billboard Message");
        btnInformation = createButton("Enter Billboard Information");
        btnMsgColour = createButton("Select Message Colour");
        btnInfoColour = createButton("Select Information Colour");
        btnBkgColour = createButton("Select Background Colour");
        btnImageURL = createButton("Add Image From a URL");
        btnImageFile = createButton("Add Image From a File");
        btnClrAllSettings = createButton("Clear all");
        btnClrImage = createButton("Clear Image");

        // Add panels to the frame
        getContentPane().add(previewDisplay, BorderLayout.CENTER);
        getContentPane().add(pnlTwo, BorderLayout.NORTH);
        getContentPane().add(pnlThree, BorderLayout.WEST);
        getContentPane().add(pnlFour, BorderLayout.EAST);
        getContentPane().add(pnlBtn, BorderLayout.SOUTH);

        layoutButtonPanel();

        getContentPane().repaint();
        getContentPane().setVisible(true);
    }

    /**
     * Helper method to create a panel and set the default background colour.
     * @param c color to set the background to
     * @return a JPanel with the given background colour
     */
    private JPanel createPanel(Color c) {
        JPanel panel = new JPanel();
        panel.setBackground(c);
        return panel;
    }

    /**
     * Sets up the panel containing the buttons.
     */
    private void layoutButtonPanel() {
        GridBagLayout layout = new GridBagLayout();
        pnlBtn.setLayout(layout);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weightx = 100;
        gbc.weighty = 100;

        addToPanel(pnlBtn, btnExport, gbc, 0, 0, 2, 1);
        addToPanel(pnlBtn, btnImport, gbc, 3, 0, 2, 1);
        addToPanel(pnlBtn, btnName, gbc, 0, 2, 2, 1);
        addToPanel(pnlBtn, btnMessage, gbc, 3, 2, 2, 1);
        addToPanel(pnlBtn, btnInformation, gbc, 0, 4, 2, 1);
        addToPanel(pnlBtn, btnMsgColour, gbc, 3, 4, 2, 1);
        addToPanel(pnlBtn, btnInfoColour, gbc, 0, 6, 2, 1);
        addToPanel(pnlBtn, btnBkgColour, gbc, 3, 6, 2, 1);
        addToPanel(pnlBtn, btnImageURL, gbc, 0, 8, 2, 1);
        addToPanel(pnlBtn, btnImageFile, gbc, 3, 8, 2, 1);
        addToPanel(pnlBtn, btnClrAllSettings, gbc, 0, 10, 2, 1);
        addToPanel(pnlBtn, btnClrImage, gbc, 3, 10, 2, 1);
    }

    /**
     * Helper method for adding components to a panel to improve code readability
     * @param jp the panel the component is to be added to
     * @param c the component to be added to the panel
     * @param gbc the GridBagConstraints for setting position
     * @param x the x position of the component
     * @param y the y position of the component
     * @param w the width of the component
     * @param h the height of the component
     */
    private void addToPanel(JPanel jp, Component c, GridBagConstraints gbc, int x, int y, int w, int h) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;
        jp.add(c, gbc);
    }

    /**
     * Helper method for creating buttons. Improves code readability and adds an action listener to each button.
     * @param str the text to go on the button.
     * @return a JButton object.
     */
    private JButton createButton(String str) {
        JButton button = new JButton();
        button.setText(str);
        button.addActionListener(this);
        return button;
    }

    /**
     * Method to turn an image URL to a base64 encoded string. Is useful for user created billboards so that images can
     * be displayed even when there is no internet connection.
     *
     * Throws an IOException if the stream from the URL cannot be opened - for example if no
     * internet connectivity exists or the target URL is down.
     *
     * @param url url object linking to the image
     * @return base64 encoded String of the image
     * @throws IOException throws an IOException if the stream from the URL cannot be opened
     */
    public String base64FromImageUrL(URL url) throws IOException {
        InputStream inputStream = url.openStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int byteCounter = 0;

        while ((byteCounter = inputStream.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, byteCounter);
        }
        baos.flush();
        System.out.println("base64 encoding: " + Base64.getEncoder().encodeToString(baos.toByteArray()));
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    /**
     * Checks to see if the passed URL leads to an image.
     *
     * @param url the URL the user wants to check if it leads to an image
     * @return true or false
     * @throws IOException throws an IOException if a connection to the target URL cannot be made.
     */
    public boolean isImage(URL url) throws IOException {
        boolean isImage;
        Object contents = url.getContent();
        if (contents.getClass().getName() == "sun.awt.image.URLImageSource") {
            isImage = true;
        }
        else {
            isImage = false;
        }
        return isImage;

    }

    /**
     * Functionality for billboard creation implemented through action events. Includes the use of the XMLParser class
     * for importing and exporting of billboards.     *
     * @see XMLParser
     * @param e - Reference to the button that the event came from.
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        Object src = e.getSource();
        // Prompts the user to input a name which will be used when exporting the file.
        // Note that on close any previously saved name is kept.
        if(src==btnName) {
            String temp = "";
            temp = bbName;
            bbName = JOptionPane.showInputDialog(null, "Enter your billboard's name", temp);
            if(bbName == null) {
                bbName = temp;
            }
        }

        // Prompts the user to enter a message to be displayed on the billboard.
        // Note that on close any previously saved message is kept.
        if (src==btnMessage) {
            String temp;
            temp = bbMsg;
            bbMsg = JOptionPane.showInputDialog(null, "Enter your billboard's message", temp);
            if (bbMsg == null) {
                bbMsg = temp;
            }
            preview.messageText = bbMsg;
            System.out.println(bbMsg);
            System.out.println(preview.messageText);
        }

        // Prompts the user to enter the information to be displayed on the billboard.
        // Note that on close any previously entered information is kept.
        if (src==btnInformation) {
            String temp = "";
            temp = bbInfo;
            bbInfo = JOptionPane.showInputDialog(null, "Enter your billboard's information", temp);
            if (bbInfo == null) {
                bbInfo = temp;
            }
            preview.informationText = bbInfo;
        }

        // Opens a color selector for the user to choose a colour for the message text.
        if (src==btnMsgColour) {
            String initial = preview.messageColor;
            if (initial == null) {
                initial = "#FFFFFF";
            }
            Color selection = JColorChooser.showDialog(null, "Example", Color.decode(initial));
            if (selection != null) {
                bbMsgColor = String.format("#%02x%02x%02x", selection.getRed(), selection.getGreen(), selection.getBlue());
            }
            else {bbMsgColor = initial;}
            preview.messageColor = bbMsgColor;
        }

        // Opens a color selector for the user to choose a colour for the information text.
        if (src==btnInfoColour) {
            String initial = preview.informationColor;
            if (initial == null) {
                initial = "#FFFFFF";
            }
            Color selection = JColorChooser.showDialog(null, "Example", Color.decode(initial));
            if (selection != null) {
                bbInfoColor = String.format("#%02x%02x%02x", selection.getRed(), selection.getGreen(), selection.getBlue());
            }
            else {bbInfoColor = initial;}
            preview.informationColor = bbInfoColor;
        }

        // Opens a color selector for the user to choose a colour for the billboard background.
        if (src==btnBkgColour) {
            String initial = preview.backgroundColor;
            if (initial == null) {
                initial = "#000000";
            }
            Color selection = JColorChooser.showDialog(null, "Example", Color.decode(initial));
            if (selection != null) {
                bbBkgColour = String.format("#%02x%02x%02x", selection.getRed(), selection.getGreen(), selection.getBlue());
            }
            else {bbBkgColour = initial;}
            preview.backgroundColor = bbBkgColour;
        }

        // Prompts the user to enter a URL for an image to be displayed on the billboard.
        // Checks to see if the URl entered is valid, and if so checks to see if the image is to an image.
        // If either of these checks fail the user is presented with error messages.
        if (src==btnImageURL) {
            String temp = "";
            temp = bbURL;
            bbURL = JOptionPane.showInputDialog(null, "Enter the URL for the image");
            if (bbURL == null) {
                bbURL = temp;
            }
            try {
                // Check to see if valid URL. If not, the thrown exception is handled and an error message displayed.
                URL checkURL = new URL(bbURL);
                try {
                    if (isImage(checkURL)) {
                        //preview.imageString = bbURL;
                        bbImageBase64 =  base64FromImageUrL(checkURL);
                        preview.imageString = bbImageBase64;
                        bbURL = null;
                    }
                    else if (!isImage(checkURL)) {
                        JOptionPane.showMessageDialog(null, "The entered URL was not an image!",
                                "Not an image", JOptionPane.ERROR_MESSAGE);
                    }
                }
                catch (IOException ioE) {

                }
            }
            catch(MalformedURLException malformedURLException) {
                JOptionPane.showMessageDialog(null, "The URL entered was invalid!",
                        "Invalid URL!", JOptionPane.ERROR_MESSAGE);
            }
            preview.isData = true;
        }

        // Opens a file selector for the user to open an image (BMP, JPEG, PNG) to be displayed on the billbaord.
        // The image is encoded to Base64 for storage in an exported XML file.
        if (src==btnImageFile) {
            // Open a file explorer filtered to show only BMP, JPG/JPEG and PNG image files.
            JFileChooser fc = new JFileChooser();
            FileFilter filter = new FileNameExtensionFilter("BMP, JPEG, JPG, PNG AND GIF", new String[] {"BMP",
            "JPEG", "JPG", "PNG", "GIF"});
            fc.setFileFilter(filter);

            // User has selected a file. Encode to base64.
            int returnVal = fc.showOpenDialog(null);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File selection = fc.getSelectedFile();
                System.out.println(selection.getParent());
                System.out.println(selection.getName());

                try {
                    FileInputStream fis = new FileInputStream(selection);
                    byte[] fileBytes = new byte[(int) selection.length()];
                    fis.read(fileBytes);
                    bbImageBase64 = Base64.getEncoder().encodeToString(fileBytes);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            preview.isData = true;
            preview.imageString = bbImageBase64;
        }

        // Allows the user to export the current billboard to the directory of billboards.
        // If no name has been entered, the user will be prompted to return and enter one.
        if (src==btnExport) {

            // Check if name is null; if so, prompt user to go back and enter a name
            if (bbName==null) {
                JOptionPane.showMessageDialog(new JFrame(), "Please give your Billboard a name before " +
                        "exporting!", "Error", JOptionPane.ERROR_MESSAGE);
            }
            // Otherwise, export the billboard as an XML file using the DocumentBuilderFactory class.
            // The code uses conditional statements to see which fields have values and need to be written to the file.
            else {
                try {
                    // Set up the document builder
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.newDocument();

                    // Create elements - for appropriate nodes check to see if attribute string is not null
                    // If attribute string has a value, place into document
                    Element billboard = doc.createElement("billboard");
                    if (bbBkgColour != null) {
                        billboard.setAttribute("background", bbBkgColour);
                    }
                    doc.appendChild(billboard);

                    if (bbMsg != null) {
                        Element message = doc.createElement("message");
                        if (bbMsgColor != null) {
                            message.setAttribute("colour", bbMsgColor);
                        }
                        message.appendChild(doc.createTextNode(bbMsg));
                        billboard.appendChild(message);
                    }

                    Element picture = doc.createElement("picture");
                    if (bbURL != null) {
                        picture.setAttribute("url", bbURL);
                        billboard.appendChild(picture);
                    }
                    else if (bbImageBase64 != null) {
                        picture.setAttribute("data", bbImageBase64);
                        billboard.appendChild(picture);
                    }

                    if (bbInfo != null) {
                        Element information = doc.createElement("information");
                        if (bbInfoColor != null) {
                            information.setAttribute("colour", bbInfoColor);
                        }
                        information.appendChild(doc.createTextNode(bbInfo));
                        billboard.appendChild(information);
                    }

                    // Build the XML string to send to the server
                    DOMSource source = new DOMSource(doc);
                    TransformerFactory tf = TransformerFactory.newInstance();
                    Transformer transformer = tf.newTransformer();
                    StringWriter writer = new StringWriter();
                    transformer.transform(source, new StreamResult(writer));
                    String xmlString = writer.getBuffer().toString();

                    // Sends the XML to the server to store.
                    String[] xmlExport = {bbName, xmlString, "root", xmlString};
                    int response = DatabaseNavigator.Response.INSERT_BILLBOARD.getValue();
                    ClientSignal clientSignal = new ClientSignal(response, xmlExport);
                    List<String> result2 = clientSignal.returnList();

                    if (result2.contains("Success")) {
                        JOptionPane.showMessageDialog(null, "Export complete. Your billboard has " +
                                "been pushed to the Billboard Server.", "Export Completed", JOptionPane.INFORMATION_MESSAGE);
                        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                    }
                    else {
                        JOptionPane.showMessageDialog(null, "Export failed. Billboard was not " +
                                "uploaded to the server.", "Export Failed!", JOptionPane.ERROR_MESSAGE);
                    }
                }
                catch (ParserConfigurationException pce) {
                    pce.printStackTrace();
                }
                catch (TransformerException tfe) {
                    tfe.printStackTrace();
                }
            }
        }

        // Allows the user to import a billboard from an XML file.
        // Utilizes XMLParser.
        // Since importing wipes the current settings in the billboard creator screen, before importing the user is
        // warned that proceeding will clear any current billboard data they have entered.
        if (src==btnImport) {
            // Warn user that any data currently entered will be lost. Check for input before proceeding.
            int confirmValue = JOptionPane.showConfirmDialog(new JFrame(), "Importing a billboard will " +
                            "overwrite any currently entered data. Are you sure?", "Confirm Import",
                    JOptionPane.YES_NO_OPTION);

            // User has chosen to proceed. A file explorer with a filter for XML files is opened.
            if (confirmValue == 0) {
                JFileChooser fc = new JFileChooser();
                // Only show XML files
                FileFilter filter = new FileNameExtensionFilter("XML", new String[] {"XML"});
                fc.setFileFilter(filter);

                int returnVal = fc.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {

                // User has selected a file. Try the file through the XMLParser to get the information contained within.
                    try {
                        this.clearAllSettings();
                        XMLParser parser = new XMLParser(fc.getCurrentDirectory() + "\\" +
                            fc.getSelectedFile().getName());
                        bbName = fc.getSelectedFile().getName();
                        // Get values from the imported file. Check if null, if so default to default values
                        // in Billboard.java
                        if (parser.getBillboardColour() != null) {
                            bbBkgColour = parser.getBillboardColour();
                            preview.backgroundColor = bbBkgColour;
                        }
                        if (parser.getMessageColour() != null) {
                            bbMsgColor = parser.getMessageColour();
                            preview.messageColor = bbMsgColor;
                        }
                        if (parser.getMessage() != null) {
                            bbMsg = parser.getMessage();
                            preview.messageText = bbMsg;
                        }
                        if (parser.getInformationColour() != null) {
                            bbInfoColor = parser.getInformationColour();
                            preview.informationColor = bbInfoColor;
                        }
                        if (parser.getInformation() != null) {
                            bbInfo = parser.getInformation();
                            preview.informationText = bbInfo;
                        }
                        if (parser.getPictureURL() != null) {
                            bbURL = parser.getPictureURL();
                            preview.imageString = bbURL;
                        }
                        if (parser.getPictureData() != null) {
                            bbImageBase64 = parser.getPictureData();
                            preview.imageString = bbImageBase64;
                        }
                        bbIsData = parser.getPictureData() != null ? true : false;

                    } catch (SAXException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        // Button to clear all settings... not working atm
        if (src == btnClrAllSettings) {
            int returnVal = JOptionPane.showConfirmDialog(null, "This will clear all data and " +
                    "create a blank billboard. All previously entered information will be lost. Are you sure?",
                    "Clear All", JOptionPane.OK_CANCEL_OPTION);

            if (returnVal == 0) {
                clearAllSettings();
                getContentPane().remove(previewDisplay);
            }
        }

        // Clear the image on the Control Panel preview
        if (src == btnClrImage) {
            if (JOptionPane.showConfirmDialog(null, "Are you sure you want to " +
                    "clear the image? Image data will be lost.", "Clear image?", JOptionPane.YES_NO_OPTION) ==
            JOptionPane.YES_OPTION) {
                bbImageBase64 = null;
                bbURL = null;
                preview.imageString = null;
                getContentPane().remove(previewDisplay);
            }
            else {

            }
        }
        // Refresh Billboard
        getContentPane().remove(previewDisplay);
        previewSize.width = (int)Math.round(getWidth()*0.96);
        previewSize.height = (int)Math.round(getHeight()-190.0);
        previewDisplay = preview.createBillboardPanel(previewSize);
        add(previewDisplay, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    /**
     * Clears all values stored in billboard fields. Useful for restarting and creating a new billboard from scratch.
     */
    private void clearAllSettings() {
        bbName = null;
        bbBkgColour = null;
        preview.backgroundColor = "#FFFFFF";
        bbMsgColor = null;
        preview.messageColor = "#000000";
        bbMsg = null;
        preview.messageText = null;
        bbInfoColor = null;
        preview.informationColor = "#000000";
        bbInfo = null;
        preview.informationText = null;
        bbURL = null;
        preview.imageString = null;
        bbImageBase64 = null;
        preview.imageString = null;
    }

    /**
     * Sets up the GUI with dimensions 500*500 and location (100,100). Packed and set visible. Called after the constructor.
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
        setPreferredSize(new Dimension(500, 500));
        setLocation(new Point(100, 100));
        pack();
        setVisible(true);
    }
}
