package CreateBillboards;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A class to parse through XML files and retrieve the information contained within.
 * The XML files for this billboard project contain three types of nodes:
 *      message: Primary billboard text. Must fit in one line with no breaks and be large enough to be clearly visible.
 *               May have a color attribute.
 *      information: For larger amounts of text that can be broken across multiple lines. Smaller than message font.
 *                      May have a color attribute.
 *      picture: Can have one of two attributes: an image url or a Base64-encoded image.
 *
 * @author Joseph See
 */

public class XMLParser {

    /*
     * Variables needed for the parser. Based on nodes used in the billboard XML and their attributes
     */
    private String billboardColour = null;
    private String message = null;
    private String messageColour = null;
    private String information = null;
    private String informationColour = null;
    private String pictureData = null;
    private String pictureURL = null;

    /**
     * Constructor for the XMLParser. Parses the input for allowable nodes for Billboards and if they
     * are present checks for appropriate attributes.
     *
     * Will throw a SAXException if the input is NOT a file in XML format or if there are serious errors
     * in the formatting of the XML.
     *
     * @param xmlFile -- A string representing the filepath of the XML file
     * @throws SAXException -- Prints a toString representation if SAXParserFactory fails
     * @see org.xml.sax
     */
    public XMLParser(String xmlFile) throws SAXException{

        // Creates a SAXParser for handling the given XML file
        try{
            SAXParserFactory factory = SAXParserFactory.newDefaultInstance();
            SAXParser parser = factory.newSAXParser();

            // Default base for SAX2 event handler. Overridden here to write XML data to the class fields.
            DefaultHandler handle = new DefaultHandler(){
                // Boolean variables to check what nodes the XML file contains

                boolean bMessage = false;
                boolean bInformation = false;
                boolean bPicture = false;
                boolean bPictureURL = false;
                boolean bPictureData = false;

                @Override
                /**
                 * If a given node is found is found, sets the appropriate boolean value to true. If there are
                 * attributes then these are also assigned to the appropriate variable.
                 */
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if (qName.equals("billboard")){
                        billboardColour = attributes.getValue("background");
                    }

                    if (qName.equals("message")){
                        bMessage = true;
                        messageColour = attributes.getValue("colour");
                    }
                    if (qName.equals("information")){
                        bInformation = true;
                        informationColour = attributes.getValue("colour");
                    }
                    if (qName.equals("picture")){
                        bPicture = true;
                        pictureURL = attributes.getValue("url");
                        pictureData = attributes.getValue("data");
                    }
                }

                /**
                 * If the billboard contains 'message' or 'information' data, this method parses the data and assigns
                 * to the appropriate variable.
                 *
                 * @param ch -- character array containing the information from the XML file
                 * @param start -- offset for the start position of the array
                 * @param length -- number of characters to utilize
                 * @throws SAXException --
                 */
                public void characters(char[] ch, int start, int length) throws SAXException{
                    if (bMessage) {
                        message = new String(ch, start, length);
                        bMessage = false;
                    }

                    if (bInformation) {
                        information = new String(ch, start, length);
                        bInformation = false;
                    }
                }
            };

            // Parses the file passed as argument to the XMLParser (xmlFile), and uses the handler as specified in
            // this class.
            parser.parse(xmlFile, handle);
        }
        catch(Exception e){
            e.toString();
        }
    }

    /**
     * If it exists, returns the data contained in the message element of the XML file.
     * @return -- String representation of the billboard message.
     */
    public String getMessage(){
//        System.out.println(message);
        return message;
    }

    /**
     * If it exists, returns the data contained in the information element of the XML file.
     * @return -- String representation of the billboard information.
     */
    public String getInformation(){
//        System.out.println(information);
        return information;
    }

    /**
     * If it exists, returns the URL for the image specified in the picture URL node of the XML file.
     * @return -- String representation of the billboard image URL.
     */
    public String getPictureURL(){
        return pictureURL;
    }

    /**
     * If it exists, returns the Base64 encoded image in the picture data node of the XML file.
     * @return -- String representation of the Base64 encoded image.
     */
    public String getPictureData(){

        return pictureData;
    }

    /**
     * If it exists, returns the hex encoded colour from the message colour node of the XML file.
     * @return -- String representation of the hex encoded message colour.
     */
    public String getMessageColour() {

        return messageColour;
    }

    /**
     * If it exists, returns the hex encoded colour from the information colour node of the XML file
     * @return -- String representation of the hex encoded information colour.
     */
    public String getInformationColour(){

        return informationColour;
    }

    /**
     * If it exists, returns the hex encoded colour from the billboard colour node of the XML file.
     * @return -- String representation of the hex encoded billboard background colour.
     */
    public String getBillboardColour(){

        return billboardColour;
    }


}
