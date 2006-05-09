package org.apache.roller.webservices.adminapi.sdk;

import org.jdom.Document;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.roller.webservices.adminapi.AappTest;
import org.jdom.JDOMException;

public class UserEntryTest extends AappTest {
    public void testEquals() {
        UserEntrySet ues1 = getSampleUserEntrySet();
        UserEntrySet ues2 = getSampleUserEntrySet();
        
        assertEquals(ues1, ues2);
    }
    
    public void testDocumentMarshal() {
        try {
            UserEntrySet ues1 = getSampleUserEntrySet();
            Document d = ues1.toDocument();
            
            UserEntrySet ues2 = new UserEntrySet(d, getEndpointUrl());
            
            assertEquals(ues1, ues2);
        } catch (MissingElementException mee) {
            fail(mee.getMessage());
        } catch (UnexpectedRootElementException uree) {
            fail(uree.getMessage());
        }
    }
    
    public void testStreamMarshal() {
        try {
            UserEntrySet ues1 = getSampleUserEntrySet();
            String s = ues1.toString();
            InputStream stream = new ByteArrayInputStream(s.getBytes("UTF-8")); 
            
            UserEntrySet ues2 = new UserEntrySet(stream, getEndpointUrl());
            
            assertEquals(ues1, ues2);
        } catch (MissingElementException mee) {
            fail(mee.getMessage());
        } catch (UnexpectedRootElementException uree) {
            fail(uree.getMessage());
        } catch (IOException ioe) {
            fail(ioe.getMessage());
        } catch (JDOMException je) {
            fail(je.getMessage());
        }
    }    
