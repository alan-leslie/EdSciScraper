package RefScraper.data;

import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Model of wikipedia page that contains lists of items to be processed
 * @author al
 */
public class EdSciEventListPage {

    private final URL theURL;
    private final Document theDocument;
    private final Logger theLogger;
    private static String theBaseURL = "http://http://www.sciencefestival.co.uk";

    /**
     * Constructs model of edinburgh science festival list page.
     * @param newURL 
     * @param logger
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException  
     */
    public EdSciEventListPage(URL newURL,
            Logger logger) throws IOException, ParserConfigurationException, SAXException {
        theURL = newURL;
        theLogger = logger;
        HTMLPageParser theParser = new HTMLPageParser(theLogger);
        theDocument = theParser.getParsedPage(theURL);
    }

    /**
     * @return -the base url for this page
     */
    public static String getBaseURL() {
        return theBaseURL;
    }

    /**
     * @return -the page url
     */
    public URL getURL() {
        return theURL;
    }

    /**
     * Finds candidate links by looking various sections of the page.
     * @return - the candidate links
     */
    public List<HTMLLink> getCandidates() {
        List<HTMLLink> theCandidates = new ArrayList<HTMLLink>();
        getMainListCandidates(theCandidates);

        return theCandidates;
    }

    /**
     * Finds candidate links by looking up top level links in the page
     * @param theCandidates - the links to populate
     */
    private void getMainListCandidates(List<HTMLLink> theCandidates) {
        NodeList linkNodeList = null;

        try {
            String searchString = "/html//div[@id='event-listing']/div[@class='element']/a";
            XPath linkXpath = XPathFactory.newInstance().newXPath();
            linkNodeList = (NodeList) linkXpath.evaluate(searchString, theDocument, XPathConstants.NODESET);

            int listLength = linkNodeList.getLength();

            for (int i = 0; i < listLength; ++i) {
                Node childNode = (Node) linkNodeList.item(i);
                short nodeType = childNode.getNodeType();

                Element theElement = (Element) childNode;
                String theTitle = theElement.getAttribute("title");
                String theHREF = theElement.getAttribute("href");
                String theText = theElement.getTextContent();

                if (!theText.isEmpty()){
                    theLogger.log(Level.INFO, "Found candidate :{0}", theTitle);
                    HTMLLink theCandidate = new HTMLLink(theTitle, theHREF);
                    theCandidates.add(theCandidate);
                }
                
//                String detailsSearchString = "../div[@class='details']/table/tr";  
                String detailsSearchString = "../div[@class='details']/table";  
                XPath detailsXpath = XPathFactory.newInstance().newXPath();
                NodeList detailsNodeList = (NodeList) detailsXpath.evaluate(detailsSearchString, childNode, XPathConstants.NODESET);
                
                if(detailsNodeList != null){
                    boolean dateFound = false;
                    int detailsLength = detailsNodeList.getLength();
                    
                    if(detailsLength > 0){
                        Node theTable = detailsNodeList.item(0);
                        String theContent = theTable.getTextContent().trim();
                        String[] theContentBits = theContent.split("\n");
                        String dateString = "";
                        
                        if(theContentBits.length > 1){
                            String theHeaderBit = theContentBits[0].trim();
                            String theDetailBit = theContentBits[1].trim();                            
                            
                            if(theHeaderBit.equalsIgnoreCase("Date:")){
                                dateString = theDetailBit;
                            }
                        }
                        
                        DateFormat theDateFormat = new SimpleDateFormat("EEE dd MMM");
                        theDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

                        try{
                            Date startDate = theDateFormat.parse(dateString);  
                        } catch (ParseException ex) {
                            theLogger.log(Level.SEVERE, null, ex);
                        }  
                    }
                }
            }
        } catch (Exception e) {
            theLogger.log(Level.SEVERE, "Exception on XPath: ", e);
        }
    }
}
