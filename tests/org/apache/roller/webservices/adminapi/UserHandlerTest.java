package org.apache.roller.webservices.adminapi;

import java.io.IOException;
import org.apache.roller.webservices.adminapi.sdk.MissingElementException;
import org.apache.roller.webservices.adminapi.sdk.UnexpectedRootElementException;
import org.apache.roller.webservices.adminapi.sdk.UserEntrySet;
import org.jdom.JDOMException;

/**
 *
 * @author jtb
 */
public class UserHandlerTest extends AappTest {
    public void testHandler() {
        try {
            //create
            UserEntrySet uesCreate = createSampleUser();
            assertNotNull(uesCreate);
            assertNotNull(uesCreate.getEntries());
            assertEquals(uesCreate.getEntries().length, 1);
            assertEquals(uesCreate, getSampleUserEntrySet());
            
            //get
            UserEntrySet uesFetch = fetchSampleUser();
            assertNotNull(uesFetch);
            assertNotNull(uesFetch.getEntries());
            assertEquals(uesFetch.getEntries().length, 1);
            assertEquals(uesFetch, uesCreate);
            
            //update
            UserEntrySet uesUpdate = updateSampleUser();
            assertNotNull(uesUpdate);
            assertNotNull(uesUpdate.getEntries());
            assertEquals(uesUpdate.getEntries().length, 1);
            assertEquals(uesUpdate, updateSampleUserEntrySet(getSampleUserEntrySet()));
            
            //delete
            UserEntrySet uesDelete = deleteSampleUser();
            assertNotNull(uesDelete);
            assertNotNull(uesCreate.getEntries());
            assertEquals(uesCreate.getEntries().length, 1);
            assertEquals(uesDelete, uesUpdate);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            fail(ioe.getMessage());
        } catch (JDOMException je) {
            je.printStackTrace();            
            fail(je.getMessage());
        } catch (MissingElementException mee) {
            mee.printStackTrace();            
            fail(mee.getMessage());
        } catch (UnexpectedRootElementException uree) {
            uree.printStackTrace();            
            fail(uree.getMessage());
        } finally {
            try {
                delete(getSampleUserEntry().getHref(), getUser(), getPassword());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
