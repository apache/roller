package org.apache.roller.webservices.adminapi;

import java.io.IOException;
import org.apache.roller.webservices.adminapi.sdk.MissingElementException;
import org.apache.roller.webservices.adminapi.sdk.UnexpectedRootElementException;
import org.apache.roller.webservices.adminapi.sdk.WeblogEntrySet;
import org.jdom.JDOMException;

public class WeblogHandlerTest extends AappTest {
    public void testHandler() {
        try {
            createSampleUser();
            
            //create
            WeblogEntrySet wesCreate = createSampleWeblog();
            assertNotNull(wesCreate);
            assertNotNull(wesCreate.getEntries());
            assertEquals(wesCreate.getEntries().length, 1);
            assertEquals(wesCreate, getSampleWeblogEntrySet());
            
            //get
            WeblogEntrySet wesFetch = fetchSampleWeblog();
            assertNotNull(wesFetch);
            assertNotNull(wesFetch.getEntries());
            assertEquals(wesFetch.getEntries().length, 1);
            assertEquals(wesFetch, wesCreate);
            
            //update
            WeblogEntrySet wesUpdate = updateSampleWeblog();
            assertNotNull(wesUpdate);
            assertNotNull(wesUpdate.getEntries());
            assertEquals(wesUpdate.getEntries().length, 1);
            assertEquals(wesUpdate, updateSampleWeblogEntrySet(getSampleWeblogEntrySet()));
            
            //delete
            WeblogEntrySet wesDelete = deleteSampleWeblog();
            assertNotNull(wesDelete);
            assertNotNull(wesCreate.getEntries());
            assertEquals(wesCreate.getEntries().length, 1);
            assertEquals(wesDelete, wesUpdate);
            
            deleteSampleUser();
        } catch (IOException ioe) {
            fail(ioe.getMessage());
            ioe.printStackTrace();            
        } catch (JDOMException je) {
            fail(je.getMessage());
            je.printStackTrace();
        } catch (MissingElementException mee) {
            fail(mee.getMessage());
            mee.printStackTrace();
        } catch (UnexpectedRootElementException uree) {
            fail(uree.getMessage());
            uree.printStackTrace();
        } finally {
            try {
                delete(getSampleWeblogEntry().getHref(), getUser(), getPassword());
                delete(getSampleUserEntry().getHref(), getUser(), getPassword());                
            } catch (Exception e) {
                // nothing
            }
        }
    }
