package org.apache.roller.webservices.adminapi.sdk;

import org.jdom.Document;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.roller.webservices.adminapi.AappTest;
import org.jdom.JDOMException;

public class MemberEntryTest extends AappTest {
    public void testEquals() {
        MemberEntrySet wes1 = getSampleMemberEntrySet();
        MemberEntrySet wes2 = getSampleMemberEntrySet();
        
        assertEquals(wes1, wes2);
    }
    
    public void testDocumentMarshal() {
        try {
            MemberEntrySet wes1 = getSampleMemberEntrySet();
            Document d = wes1.toDocument();
            
            MemberEntrySet wes2 = new MemberEntrySet(d, getEndpointUrl());
            
            assertEquals(wes1, wes2);
        } catch (MissingElementException mee) {
            fail(mee.getMessage());
        } catch (UnexpectedRootElementException uree) {
            fail(uree.getMessage());
        }
    }
    
    public void testStreamMarshal() {
        try {
            MemberEntrySet wes1 = getSampleMemberEntrySet();
            String s = wes1.toString();
            InputStream stream = new ByteArrayInputStream(s.getBytes("UTF-8")); 
            
            MemberEntrySet wes2 = new MemberEntrySet(stream, getEndpointUrl());
            
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
