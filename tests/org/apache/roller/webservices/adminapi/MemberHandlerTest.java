package org.apache.roller.webservices.adminapi;

import java.io.IOException;
import org.apache.roller.webservices.adminapi.sdk.MemberEntrySet;
import org.apache.roller.webservices.adminapi.sdk.MissingElementException;
import org.apache.roller.webservices.adminapi.sdk.UnexpectedRootElementException;
import org.jdom.JDOMException;

public class MemberHandlerTest extends AappTest {
    public void testHandler() {
        try {
            createSampleUser();
            createSampleWeblog();
            
            //create
            MemberEntrySet mesCreate = createSampleMember();
            assertNotNull(mesCreate);
            assertNotNull(mesCreate.getEntries());
            assertEquals(mesCreate.getEntries().length, 1);
            assertEquals(mesCreate, getSampleMemberEntrySet());
            
            //get
            MemberEntrySet mesFetch = fetchSampleMember();
            assertNotNull(mesFetch);
            assertNotNull(mesFetch.getEntries());
            assertEquals(mesFetch.getEntries().length, 1);
            assertEquals(mesFetch, mesCreate);
            
            //update
            MemberEntrySet mesUpdate = updateSampleMember();
            assertNotNull(mesUpdate);
            assertNotNull(mesUpdate.getEntries());
            assertEquals(mesUpdate.getEntries().length, 1);
            assertEquals(mesUpdate, updateSampleMemberEntrySet(getSampleMemberEntrySet()));
            
            //delete
            MemberEntrySet mesDelete = deleteSampleMember();
            assertNotNull(mesDelete);
            assertNotNull(mesCreate.getEntries());
            assertEquals(mesCreate.getEntries().length, 1);
            assertEquals(mesDelete, mesUpdate);
            
            deleteSampleWeblog();
            deleteSampleUser();
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
                delete(getSampleMemberEntry().getHref(), getUser(), getPassword());
                delete(getSampleWeblogEntry().getHref(), getUser(), getPassword());                
                delete(getSampleUserEntry().getHref(), getUser(), getPassword());                                
            } catch (Exception e) {
                // nothing
            }
        }
    }
