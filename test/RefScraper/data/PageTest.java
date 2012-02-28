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
            String theEventId = thePage.getEventId();
            String theDate = thePage.getDate();
            String theVenueName = thePage.getVenueName();
            List<Period> thePeriods = thePage.getPeriods();     // this could be multiples
            Position thePosition = thePage.getPosition();
            Period thePeriod = thePeriods.get(0);

            assertEquals(true, thePeriod.hasDuration());
            Calendar startDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            startDate.setTime(thePeriod.getStartDate());
            Calendar endDate = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
            endDate.setTime(thePeriod.getEndDate());
            assertEquals(2012, startDate.get(Calendar.YEAR));
            assertEquals(2012, endDate.get(Calendar.YEAR));
            
            // assert that the others are as required
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
