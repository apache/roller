package org.apache.roller.webservices.adminapi.sdk;

import org.jdom.Document;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.roller.webservices.adminapi.AappTest;
import org.jdom.JDOMException;

public class WeblogEntryTest extends AappTest {
    public void testEquals() {
        WeblogEntrySet wes1 = getSampleWeblogEntrySet();
        WeblogEntrySet wes2 = getSampleWeblogEntrySet();
        
        assertEquals(wes1, wes2);
    }
    
    public void testDocumentMarshal() {
        try {
            WeblogEntrySet wes1 = getSampleWeblogEntrySet();
            Document d = wes1.toDocument();
            
            WeblogEntrySet wes2 = new WeblogEntrySet(d, getEndpointUrl());
            
            assertEquals(wes1, wes2);
        } catch (MissingElementException mee) {
            fail(mee.getMessage());
        } catch (UnexpectedRootElementException uree) {
            fail(uree.getMessage());
        }
    }
    
    public void testStreamMarshal() {
        try {
            WeblogEntrySet wes1 = getSampleWeblogEntrySet();
            String s = wes1.toString();
            InputStream stream = new ByteArrayInputStream(s.getBytes("UTF-8")); 
            
            WeblogEntrySet wes2 = new WeblogEntrySet(stream, getEndpointUrl());
            
            assertEquals(wes1, wes2);
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
