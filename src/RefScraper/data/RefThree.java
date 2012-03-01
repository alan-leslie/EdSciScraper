package RefScraper.data;

import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class to hold information for a HREF, a position and a duration.
 * @author al
 */
public class RefThree implements Comparable {

    String theName;
    private String dateString = "";
    private String timeString = "";
    private Position thePosition;
    private URL theLocationRef;
    private URL thePlace;
    private Period thePeriod;
    private URL theURL;
    private String theHREF;
    private String theAges = "";
    private String thePrice = "";
    private String theEventId = "";
    private String theVenueName = "";
    
    private final Logger theLogger;

    /**
     * 
     * @param theTitle 
     * @param theHREF 
     * @param logger 
     */
    public RefThree(String theTitle,
            String theHREF,
            Logger logger) {
        this.theHREF = theHREF;
        String thePageHREF = theHREF;
        theLogger = logger;

        try {
            theURL = new URL(thePageHREF);
        } catch (MalformedURLException ex) {
            theLogger.log(Level.SEVERE, null, ex);
        }
        theName = theTitle;
    }

    /**
     * a copy constructor to make sure that the object is not shared in threads
     * @param theOther - the original to be copied
     */
    public RefThree(RefThree theOther) {
        theName = theOther.theName;
        if (theOther.thePosition == null) {
            thePosition = null;
        } else {
            thePosition = new Position(theOther.thePosition.getLatitude(), theOther.thePosition.getLongitude());
        }
        theLocationRef = theOther.theLocationRef;
        thePlace = theOther.thePlace;
        if (theOther.thePeriod == null) {
            thePeriod = null;
        } else {
            thePeriod = new Period(theOther.thePeriod.getStartDate(), theOther.thePeriod.getEndDate());
        }
        theURL = theOther.theURL;
        theVenueName = theOther.theVenueName;
        thePrice = theOther.thePrice;
        theAges = theOther.theAges;
        theEventId = theOther.theEventId;
        dateString = theOther.dateString;
        timeString = theOther.timeString;
        theLogger = theOther.theLogger;
    }

    /**
     * Output the placemark data in different xml formats.
     * @param ps - the stream to where the data is written
     * @param asKML - whether the output format is for google maps (KML) or 
     * timeline (XML)
     */
    public void outputAsXML(PrintStream ps,
            boolean asKML) {     
        DateFormat theDateTimeFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
        theDateTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        if (asKML) {
            ps.print("<Placemark>");
            ps.println();
            ps.print("<name>");
            ps.println();
            ps.print(theName);
            ps.println();
            ps.print("</name>");
            ps.println();
            ps.print("<description>");
            ps.println();

            if (theURL != null) {
                ps.print("&lt;p&gt;");
                ps.println();
                ps.print("&lt;a href=\"");
                ps.print(theURL);
                ps.print("\"&gt; more info&gt;&gt;&gt;");
                ps.print("&lt;/a&gt;");
                ps.println();
                ps.print("&lt;/p&gt;");
                ps.println();
            }

            ps.print("</description>");
            ps.println();
            ps.print("<styleUrl>#exampleStyleMap</styleUrl>");
            ps.println();

            if (thePeriod.hasDuration()) {
                ps.print("<TimeSpan>");
                ps.print("<begin>");
                ps.print(theDateTimeFormat.format(thePeriod.getStartDate()));
                ps.print("</begin>");
                ps.print("<end>");
                ps.print(theDateTimeFormat.format(thePeriod.getEndDate()));
                ps.print("</end>");
                ps.print("</TimeSpan>");
                ps.println();
            } else {
                ps.print("<TimeStamp>");
                ps.print("<when>");
                ps.print(theDateTimeFormat.format(thePeriod.getStartDate()));
                ps.print("</when>");
                ps.print("</TimeStamp>");
                ps.println();
            }

            ps.print("<ExtendedData>");
            ps.println();
            ps.println("<Data name=\"Ages\">");
            ps.print("<value>");
            ps.print(theAges);
            ps.print("</value>");
            ps.println();
            ps.println("</Data>");
            ps.println("<Data name=\"Price\">");
            ps.print("<value>");
            ps.print(thePrice);
            ps.print("</value>");
            ps.println();
            ps.println("</Data>");
            ps.println("<Data name=\"EventId\">");
            ps.print("<value>");
            ps.print(theEventId);
            ps.print("</value>");
            ps.println();
            ps.println("</Data>");
            ps.println("<Data name=\"LocationName\">");
            ps.print("<value>");
            ps.print(theVenueName);
            ps.print(", Edinburgh");
            ps.print("</value>");
            ps.println();   
            ps.println("</Data>");
            ps.println("<Data name=\"DateString\">");
            ps.print("<value>");
            ps.print(dateString);
            ps.print("</value>");
            ps.println();
            ps.println("</Data>");
            ps.println("<Data name=\"TimeString\">");
            ps.print("<value>");
            ps.print(timeString);
            ps.print("</value>");
            ps.println();
            ps.println("</Data>");
            ps.println("<Data name=\"LatLonStr\">");
            ps.print("<value>");
            ps.print(thePosition.getLatitude());
            ps.print(",");
            ps.print(thePosition.getLongitude());
            ps.print("</value>");
            ps.println();
            ps.println("</Data>");
            ps.println("<Data name=\"Url\">");
            ps.print("<value>");
            ps.print(theURL.toString());
            ps.print("</value>");
            ps.println();
            ps.println("</Data>");
            ps.print("</ExtendedData>");
            ps.println();

            ps.print("<Point>");
            ps.println();
            ps.print("<coordinates>");
            ps.print(thePosition.getLongitude());
            ps.print(",");
            ps.print(thePosition.getLatitude());
            ps.print("</coordinates>");
            ps.println();
            ps.print("</Point>");
            ps.println();
            ps.print("</Placemark>");
            ps.println();
        } else {
            ps.print("<event ");
            ps.print("start=\"");
            ps.print(theDateTimeFormat.format(thePeriod.getStartDate()));
            ps.print("\" ");
            if (thePeriod.hasDuration()) {
                ps.print("end=\"");
                ps.print(theDateTimeFormat.format(thePeriod.getEndDate()));
                ps.print("\" ");
            }

            ps.print("title=\"");
            ps.print(theName);
            ps.print("\">");
            ps.println();
            ps.print("</event>");
            ps.println();
        }
    }

    /**
     * 
     * @return - a unique id for the placemark
     */
    public String getId() {
        String strId = theName.replaceAll(" ", "");
        return strId;
    }

    /**
     * @return - the href of this placemark
     */
    public String getHREF() {
        return theHREF;
    }

    /**
     * 
     * @return - the period
     */
    public Period getPeriod() {
        return thePeriod;
    }

    /**
     * 
     * @return - the date as a string
     */
    public String getDateString() {
        return dateString;
    }

    /**
     * 
     * @return - the position 
     */
    public Position getPosition() {
        return thePosition;
    }

    /**
     * attempt to fill in all of the placemark data
     * @return - whether all of the required data has been completed
     */
    public boolean complete() {
        try {
            EdSciEventDetailPage thePage = new EdSciEventDetailPage(theURL, theLogger);

            thePosition = thePage.getPosition();
            List<Period> thePeriods = thePage.getPeriods();
            if(thePeriods != null && thePeriods.size() > 0){
                thePeriod = thePeriods.get(0);
            }

            // try to recover if data is only partially set
            if ((isPeriodSet() || isPositionSet()) && !(isPeriodSet() && isPositionSet())) {
                if (!isPeriodSet()) {
                    thePeriod = PeriodMap.getInstance().getPeriod(getId());
                }

                if (!isPositionSet()) {
                    thePosition = PositionMap.getInstance().getPosition(getId());
                }
            }
            
            theEventId = thePage.getEventId();
            theVenueName = thePage.getVenueName();
            theAges = thePage.getAges();
            thePrice = thePage.getPrice();

            if (isPeriodSet()) {
                DateFormat theDateFormat = new SimpleDateFormat("EEE MMM dd");
                theDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                dateString = theDateFormat.format(thePeriod.getStartDate());
                DateFormat theTimeFormat = new SimpleDateFormat("HH:mm");
                theTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                timeString = theTimeFormat.format(thePeriod.getStartDate());
            }
        } catch (Exception exc) {
            theLogger.log(Level.SEVERE, "Unable to parse: " + getId(), exc);
        }

        if (isComplete()) {
            return true;
        } else {
            theLogger.log(Level.WARNING, "Unable to complete {0}", getId());
            return false;
        }
    }

    /**
     * @return - whether all of the position data has been set
     */
    private boolean isPositionSet() {
        return (thePosition != null && thePosition.isComplete());
    }

    /**
     * @return - whether all of the period data has been set
     */
    private boolean isPeriodSet() {
        return (thePeriod != null && thePeriod.isComplete());
    }

    /**
     * @return - whether all of the required data has been set
     */
    private boolean isComplete() {
        boolean retVal = isPositionSet() && isPeriodSet();
        return retVal;
    }

    public int compareTo(Object anotherPlacemark) throws ClassCastException {
        if (!(anotherPlacemark instanceof RefThree)) {
            throw new ClassCastException("A RefThree object expected.");
        }
        String anotherPlacemarkName = ((RefThree) anotherPlacemark).getId();
        return this.getId().compareTo(anotherPlacemarkName);
    }
}
