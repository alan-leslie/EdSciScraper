package RefScraper.data;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.xml.sax.SAXException;

/**
 *
 * @author al
 */
public class PageTest {

    /**
     * 
     */
    @Test
    public void testDetailPage() {
        try {
            URL theURL = new URL("http://localhost/the-story-of-time");
            Logger theLogger = Logger.getLogger(PageTest.class.getName());
        
            EdSciEventDetailPage thePage = new EdSciEventDetailPage(theURL, theLogger);            
            
            URL pageURL = thePage.getURL(); 
            String theVenueName = thePage.getVenueName();
            List<Period> thePeriods = thePage.getPeriods();     // this could be multiples
            Position thePosition = thePage.getPosition();
            Period thePeriod = thePeriods.get(0);
            
            assertEquals(theURL, pageURL);   
            assertEquals(1, thePeriods.size());            
            assertEquals(true, thePeriod.hasDuration());
            Calendar startDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            startDate.setTime(thePeriod.getStartDate());
            Calendar endDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            endDate.setTime(thePeriod.getEndDate());
            assertEquals(2012, startDate.get(Calendar.YEAR));
            assertEquals(3, startDate.get(Calendar.MONTH));
            assertEquals(9, startDate.get(Calendar.DAY_OF_MONTH));
            assertEquals(17, startDate.get(Calendar.HOUR_OF_DAY));
            assertEquals(30, startDate.get(Calendar.MINUTE));
            assertEquals(2012, endDate.get(Calendar.YEAR));
            assertEquals(3, endDate.get(Calendar.MONTH));
            assertEquals(9, endDate.get(Calendar.DAY_OF_MONTH));
            assertEquals(19, endDate.get(Calendar.HOUR_OF_DAY));
            assertEquals(0, endDate.get(Calendar.MINUTE));
            
            assert(thePosition.getLatitude().equalsIgnoreCase("55.954868"));
            assert(thePosition.getLongitude().equalsIgnoreCase("-3.196712"));
            assert(theVenueName.equalsIgnoreCase("The Jam House"));
        } catch (MalformedURLException ex) {
            Logger.getLogger(PageTest.class.getName()).log(Level.SEVERE, null, ex);
        }catch (IOException ex) {
            Logger.getLogger(PageTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(PageTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(PageTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
