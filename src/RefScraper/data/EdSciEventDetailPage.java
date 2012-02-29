package RefScraper.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
            String theVenueName = getVenueNameFromSummary(theSummary);
            
            if (theVenueName != null) {
                PositionMap thePosMap = PositionMap.getInstance();
                thePosition = thePosMap.getPosition(theVenueName);
                
                if (thePosition == null) {
                    thePosition = getVenuePositionFromSummary(theSummary);
                    if (thePosition != null) {
                        thePosMap.addPosition(theVenueName, thePosition);
                    }
                }
            }
            
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
        
        if (theSummary != null) {
            int theDuration = getDurationMinutesFromSummary(theSummary);
            String theDate = getDate();
            String theEventId = getEventId();
            String theTime = getTime(theEventId, theDate);
            
            SimpleDateFormat theDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            theDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            
            try {
                Date startDate = theDateFormat.parse(theDate + " " + theTime);                
                Date endDate = theDateFormat.parse(theDate + " " + theTime);                
                endDate.setTime(endDate.getTime() + theDuration * 60 * 1000);
                
                Period thePeriod = new Period(startDate, endDate);
                thePeriods.add(thePeriod);
            } catch (ParseException ex) {
                Logger.getLogger(EdSciEventDetailPage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
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
            
            if (theData != null) {
                int theLength = theData.getLength();
                
                if (theLength > 0) {
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
    
    private int getDurationMinutesFromSummary(NodeList summaryData) {
        int durationInMinutes = 0;
        String durationString = getDurationFromSummary(summaryData);
        String[] durationBits = durationString.split(" ");
        
        if (durationBits.length > 1) {
            if (durationBits[1].contains("mins")) {
                durationInMinutes = Integer.parseInt(durationBits[0]);                
            } else {
                if (durationBits[1].contains("hour")) {
                    durationInMinutes = Integer.parseInt(durationBits[0]) * 60;                    
                }                
            }            
        }
        
        return durationInMinutes;
    }

    /**
     * Try and get the name of the venue from the summary of the page
     * @param summaryData 
     * @return - valid venue name or null if not obtainable
     */
    private String getDurationFromSummary(NodeList summaryData) {
        Node theValueNode = getValueNodeFromSummary(theSummary, "Duration:");
        String retVal = null;
        
        if (theValueNode != null) {
            retVal = theValueNode.getTextContent();
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
        
        try {
            EdSciEventDetailPage thePage = new EdSciEventDetailPage(locationRef, theLogger);
            refPosition = thePage.getPageCoords();
        } catch (Exception e) {
            theLogger.log(Level.SEVERE, "Cannot get location page", e);
        }
        
        return refPosition;
    }

    /**
     * get the event id from the page
     * @return - the result or "?" if not found
     */
    private String getEventId() {
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
    private String getDate() {
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
    
    private Position getPageCoords() {
        Position thePosition = null;
        
        try {
            XPath mapIdXpath = XPathFactory.newInstance().newXPath();
            Node theNode = (Node) mapIdXpath.evaluate("html//div[@id='google-map']", theDocument, XPathConstants.NODE);
            
            if (theNode != null) {
                NamedNodeMap theAttributes = theNode.getAttributes();
                Node theValueNode = theAttributes.getNamedItem("class");
                String theMapString = theValueNode.getNodeValue();
                int indexOfFirstOpenSqBracket = theMapString.indexOf("[");
                int indexOfFirstCloseSqBracket = theMapString.indexOf("]");
                
                if (indexOfFirstOpenSqBracket > 0 && indexOfFirstCloseSqBracket > 0) {
                    String theCoordString = theMapString.substring(indexOfFirstOpenSqBracket + 1, indexOfFirstCloseSqBracket);
                    String[] splitCoords = theCoordString.split(",");
                    
                    if (splitCoords.length > 1) {
                        String theLat = splitCoords[0].substring(1, splitCoords[0].length() - 1);
                        String theLon = splitCoords[1].substring(1, splitCoords[1].length() - 1);
                        
                        thePosition = new Position(theLat, theLon);                        
                    }
                }
            }
        } catch (XPathExpressionException ex) {
            theLogger.log(Level.SEVERE, null, ex);
        }
        
        return thePosition;
    }
    
    private String getTime(String eventId,
            String theDate) {
        String retVal = null;
        StringBuilder theBuilder = new StringBuilder("http://www.sciencefestival.co.uk/json_event_performances?booking[event_id]=");
        theBuilder.append(eventId);
        theBuilder.append("&booking[date]=");
        String theDateAsURLString = theDate.replace("/", "%2F");
        theBuilder.append(theDateAsURLString);
        String theTimeURLStr = theBuilder.toString();        
        HttpClient client = new DefaultHttpClient();
        
        try {
            URL theTimeURL = new URL(theTimeURLStr);
            HttpGet theGet = new HttpGet(theTimeURL.toString());
            HttpResponse response = client.execute(theGet);
            
            HttpEntity theEntity = response.getEntity();            
            InputStream in = null;
            
            try {
                in = theEntity.getContent();
                StringBuffer buffer = new StringBuffer();
                
                InputStreamReader isr = new InputStreamReader(in, "UTF8");
                Reader theReader = new BufferedReader(isr);
                int ch;
                while ((ch = theReader.read()) > -1) {
                    buffer.append((char) ch);
                }
                in.close();
                String theJSONData = buffer.toString();
                try {
                    JSONObject theObject = new JSONObject(theJSONData);
                    JSONObject theTimes = (JSONObject) theObject.get("times");
                    JSONArray names = theTimes.names();
                    
                    int arrayLength = names.length();
                    
                    if (arrayLength > 0) {
                        retVal = names.getString(0);
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(EdSciEventDetailPage.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(EdSciEventDetailPage.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                    }
                }
            }            
        } catch (IOException ex) {
            Logger.getLogger(EdSciEventDetailPage.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            client.getConnectionManager().shutdown();
        }
        
        return retVal;
    }
}
