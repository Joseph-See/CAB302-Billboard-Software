package CreateBillboards;

import java.io.IOException;

public class TempXMLCreator {
    public static void makeTempXML(String xmlData, String fileName) throws IOException {
        java.io.FileWriter fw = new java.io.FileWriter(fileName);
        fw.write(xmlData);
        fw.close();
    }
}