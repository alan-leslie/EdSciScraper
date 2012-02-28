package RefScraper.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Model of Edinburgh science festival detail page (contains details of period position).
 * @author al
 */
public class EdSciEventDetailPage {

    private final URL theURL;
    private final Document theDocument;
    private final Logger theLogger;
    private NodeList theSummary = null;
    private static String theBaseURL = "http://www.sciencefestival.co.uk";

    /**
     * Constructs model of Edinburgh science festival detail page.
     * @param newURL 
     * @param logger
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException  
     */
    public EdSciEventDetailPage(URL newURL,
            Logger logger) throws IOException, ParserConfigurationException, SAXException {
        theURL = newURL;
        theLogger = logger;
        HTMLPageParser theParser = new HTMLPageParser(theLogger);
        theDocument = theParser.getParsedPage(theURL);
    }

    /**
     * Finds the period from the page.
     * @return -valid period or null if unobtainable
     */
    public static String getBaseURL() {
        return theBaseURL;
    }

    /**
     * 
     * @return -valid URL of this page.
     */
    public URL getURL() {
        return theURL;
    }

    /**
     * Finds the position from the page.
     * @return -valid position or null if unobtainable
     */
    public Position getPosition() {
        Position thePosition = null;

        if (theSummary == null) {
            theSummary = getSummary();
        }

        if (theSummary != null) {
            thePosition = getVenuePositionFromSummary(theSummary);
        }

        return thePosition;
    }

    /**
     * Finds the venue name from the page.
     * @return -valid position or null if unobtainable
     */
    public String getVenueName() {
        String theVenueName = null;

        if (theSummary == null) {
            theSummary = getSummary();
        }

        if (theSummary != null) {
            theVenueName = getVenueNameFromSummary(theSummary);
        }

        return theVenueName;
    }

    /**
     * Finds the period from the page.
     * @return -valid period or null if unobtainable
     */
    public List<Period> getPeriods() {
        List<Period> thePeriods = new ArrayList<Period>();

        if (theSummary == null) {
            theSummary = getSummary();
        }

//        if (theSummary != null) {
//            thePeriods = getDateFromSummary(theSummary);
//        }

        return thePeriods;
    }

    /*
     * Get the summary data from the page
     * @return - node list representing the summary section
     *
     */
    private NodeList getSummary() {
        NodeList retVal = null;

        try {
            XPath summaryTableXpath = XPathFactory.newInstance().newXPath();
            NodeList theData = (NodeList) summaryTableXpath.evaluate("html//div[@class='summary']/table/tbody/tr", theDocument, XPathConstants.NODESET);

            if(theData != null){
                int theLength = theData.getLength();
                
                if(theLength > 0){
                    return theData;
                }
            }
        } catch (XPathExpressionException ex) {
            theLogger.log(Level.SEVERE, null, ex);
        }

        return retVal;
    }

    /**
     * Try and get the position from the summary of the page
     * @param summaryData 
     * @return - valid position or null if not obtainable
     */
    private Position getVenuePositionFromSummary(NodeList summaryData) {
        Node theValueNode = getValueNodeFromSummary(theSummary, "Venue:");
        Position summaryPosition = null;

        try {
            if (theValueNode != null) {
                XPath anchorXpath = XPathFactory.newInstance().newXPath();
                Node theAnchor = (Node) anchorXpath.evaluate("./a", theValueNode, XPathConstants.NODE);

                if (theAnchor != null) {
                    Element thePlaceElement = (Element) theAnchor;
                    String thePlaceHREF = thePlaceElement.getAttribute("href");
                    if (thePlaceHREF.indexOf("http://") != 0) {
                        thePlaceHREF = EdSciEventDetailPage.getBaseURL() + thePlaceHREF;
                    }

                    try {
                        URL theLocationRef = new URL(thePlaceHREF);
                        summaryPosition = getLocationFromRef(theLocationRef);
                    } catch (MalformedURLException ex) {
                        theLogger.log(Level.SEVERE, "Unable to format place URL", ex);
                    }
                }
            }
        } catch (XPathExpressionException ex) {
            theLogger.log(Level.SEVERE, null, ex);
        }

        return summaryPosition;
    }

    /**
     * Try and get the name of the venue from the summary of the page
     * @param summaryData 
     * @return - valid venue name or null if not obtainable
     */
    private String getVenueNameFromSummary(NodeList summaryData) {
        Node theValueNode = getValueNodeFromSummary(theSummary, "Venue:");
        String retVal = null;

        try {
            if (theValueNode != null) {
                XPath anchorXpath = XPathFactory.newInstance().newXPath();
                Node theAnchor = (Node) anchorXpath.evaluate("./a", theValueNode, XPathConstants.NODE);

                if (theAnchor != null) {
                    retVal = theAnchor.getTextContent();
                }
            }
        } catch (XPathExpressionException ex) {
            theLogger.log(Level.SEVERE, null, ex);
        }

        return retVal;
    }

    /**
     * Try and get the latitude and longitude from the location reference URL
     * @param locationRef 
     * @return - valid position or null if unobtainable
     */
    private Position getLocationFromRef(URL locationRef) {
        Position refPosition = null;

//        try {
//            EdSciEventDetailPage thePage = new EdSciEventDetailPage(locationRef, theLogger);
//            refPosition = thePage.getPageCoords();
//        } catch (Exception e) {
//            theLogger.log(Level.SEVERE, "Cannot get location page", e);
//        }

        return refPosition;
    }

    /**
     * Try and get the period from page summary
     * @param summaryData 
     * @return - valid period or null if not obtainable
     */
    private Period getDateFromSummary(NodeList summaryData) {
        Period summaryPeriod = null;
        Node theValueNode = getValueNodeFromSummary(theSummary, "Duration:");

        if (theValueNode != null) {
            String detailText = theValueNode.getTextContent();
            summaryPeriod = Period.getRealPeriod(detailText);

            if (summaryPeriod == null) {
                Date theDate = Period.getDate(detailText);

                if (theDate != null) {
                    summaryPeriod = new Period(theDate, theDate);
                } else {
                    theLogger.log(Level.WARNING, "Cannot get date");
                }
            }
        }

        return summaryPeriod;
    }

    /**
     * get the event id from the page
     * @return - the result or "?" if not found
     */
    String getEventId() {
        String retVal = "?";

        try {
            XPath eventIdXpath = XPathFactory.newInstance().newXPath();
            Node theNode = (Node) eventIdXpath.evaluate("html//input[@id='booking_event_id']", theDocument, XPathConstants.NODE);

            if (theNode != null) {
                NamedNodeMap theAttributes = theNode.getAttributes();
                Node theValueNode = theAttributes.getNamedItem("value");
                retVal = theValueNode.getNodeValue();
            }
        } catch (XPathExpressionException ex) {
            theLogger.log(Level.SEVERE, null, ex);
        }

        return retVal;
    }

    /**
     * get the event id from the page
     * @return - the result or "?" if not found
     */
    String getDate() {
        String retVal = "?";

        try {
            XPath eventIdXpath = XPathFactory.newInstance().newXPath();
            Node theNode = (Node) eventIdXpath.evaluate("html//input[@id='booking_date']", theDocument, XPathConstants.NODE);

            if (theNode != null) {
                NamedNodeMap theAttributes = theNode.getAttributes();
                Node theValueNode = theAttributes.getNamedItem("value");
                retVal = theValueNode.getNodeValue();
            }
        } catch (XPathExpressionException ex) {
            theLogger.log(Level.SEVERE, null, ex);
        }

        return retVal;
    }

    /**
     * Try and get the node that contains the value for the summary data item
     * with title dataName.
     * The summary is a table so looking for a tr node that has a th of dataName
     * @param summaryData 
     * @param dataName 
     * @return - valid Node or null if not found
     */
    private Node getValueNodeFromSummary(NodeList summaryData,
            String dataName) {
        Node dataValueNode = null;

        try {
            int theLength = summaryData.getLength();
            boolean dataFound = false;

            for (int i = 0; i < theLength && !dataFound; ++i) {
                XPath headerXpath = XPathFactory.newInstance().newXPath();
                Node theHeaderNode = (Node) headerXpath.evaluate("./th", summaryData.item(i), XPathConstants.NODE);

                if (theHeaderNode != null) {
                    String theHeaderStr = theHeaderNode.getTextContent();

                    if (theHeaderStr.equalsIgnoreCase(dataName)) {
                        XPath detailXpath = XPathFactory.newInstance().newXPath();
                        Node theDetail = (Node) detailXpath.evaluate("./td", summaryData.item(i), XPathConstants.NODE);

                        if (theDetail != null) {
                            dataValueNode = theDetail;
                        }

                        dataFound = true;
                    }
                }

            }
        } catch (XPathExpressionException ex) {
            theLogger.log(Level.SEVERE, null, ex);
        }

        return dataValueNode;
    }
}
