package RefScraper.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.lobobrowser.html.UserAgentContext;
import org.lobobrowser.html.parser.HtmlParser;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.SAXException;

/**
 * Parses HTML pages using CORBA parser
 * @author al
 */
public class HTMLPageParser {

    private final Logger theLogger;

    HTMLPageParser(Logger logger) {
        theLogger = logger;
    }
    
    /*
     * @param - theURL the page to be parsed
     * @return - parsed html of the page
     */
    Document getParsedPage(URL theURL) throws IOException, ParserConfigurationException, SAXException {
        Document theResult = null;

        if (theURL == null) {
            return theResult;
        } else {
            HttpClient client = new DefaultHttpClient();

            try {
                HttpGet theGet = new HttpGet(theURL.toString());
                HttpResponse response = client.execute(theGet);
                HttpEntity theEntity = response.getEntity();

                UserAgentContext uacontext = new SimpleUserAgentContext();

                // In this case we will use a standard XML document
                // as opposed to Cobra's HTML DOM implementation.
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                InputStream in = null;

                try {
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    in = theEntity.getContent();
                    Reader reader = new InputStreamReader(in, "UTF8");

                    theResult = builder.newDocument();
                    HtmlParser parser = new HtmlParser(uacontext, theResult);
                    parser.parse(reader);
//                } catch (Exception theException) {
//                    theResult = null;
//                    theLogger.log(Level.SEVERE, "Exception on parsing ", theException);
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e) {
                        }
                    }
                }          
//            }  catch (Exception e) {
//                theLogger.log(Level.SEVERE, "Parsing general exception", e);
//                throw e;
            } finally {
                // When HttpClient instance is no longer needed,
                // shut down the connection manager to ensure
                // immediate deallocation of all system resources
                client.getConnectionManager().shutdown();
            }
        }

        return theResult;
    }
}
