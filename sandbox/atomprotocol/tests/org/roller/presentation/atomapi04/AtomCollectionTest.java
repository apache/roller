/*
 * Copyright 2005 David M Johnson (For RSS and Atom In Action)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.roller.presentation.atomapi04;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.Document;
import org.roller.presentation.atomapi.AtomCollection;
import org.roller.presentation.atomapi.AtomService;

/**
 * @author Dave Johnson
 */
public class AtomCollectionTest extends TestCase {
    
    private static SimpleDateFormat df =
        new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ" );
   
    /** Creates a new instance of AtomCollectionTest */
    public AtomCollectionTest() {
    }

    public void testRangeParsing() throws Exception {
        Date end = new Date(); // now 
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DATE, -1);
        Date start = cal.getTime(); // one day ago
        String startString = df.format(start);
        String endString = df.format(end);
        
        String r1 = "Range: updated="+startString+"/"+endString;
        AtomCollection.Range range = AtomCollection.parseRange(r1);
        assertNotNull(range.start);
        assertNotNull(range.end);
        
        String r2 = "Range: updated="+startString+"/";
        range = AtomCollection.parseRange(r2);
        assertNotNull(range.start);
        assertNull(range.end);
        
        String r3 = "Range: updated="+"/"+endString;
        range = AtomCollection.parseRange(r3);
        assertNull(range.start);
        assertNotNull(range.end);
    }
    
    public void testCollectionBean() throws Exception {
        
        // create a collection with a member
        Date date1 = new Date();
        AtomCollection collection = new AtomCollection();
        AtomCollection.Member member1 = new AtomCollection.Member();
        member1.setTitle("title1");
        member1.setHref("http://example.com/item1");
        member1.setUpdated(date1);
        collection.addMember(member1);
        
        // add another member
        Date date2 = new Date();
        AtomCollection.Member member2 = new AtomCollection.Member();
        member2.setTitle("title2");
        member2.setHref("http://example.com/item2");
        member2.setUpdated(date2);
        collection.addMember(member2);
        
        // serialize to XML
        Document doc = AtomCollection.collectionToDocument(collection);
        assertEquals("collection", doc.getRootElement().getName());
        assertEquals(2, doc.getRootElement().getContent().size());
        
        // deserialize from XML and assert we've got the same stuff
        AtomCollection col2 = AtomCollection.documentToCollection(doc);
        assertEquals(2, col2.getMembers().size());
        
        AtomCollection.Member m1 = (AtomCollection.Member)col2.getMembers().get(0);
        assertEquals("title1", m1.getTitle());
        assertEquals("http://example.com/item1", m1.getHref());
        assertCloseEnough(date1, m1.getUpdated());
        
        AtomCollection.Member m2 = (AtomCollection.Member)col2.getMembers().get(1);
        assertEquals("title2", m2.getTitle());
        assertEquals("http://example.com/item2", m2.getHref());
        assertCloseEnough(date2, m2.getUpdated());
    }
    
    public void testServiceBean() {
        
        AtomService.Collection collection = new AtomService.Collection();
        collection.setTitle("All blog entries");
        collection.setContents("entries");
        collection.setHref("http://example.com/collection1");
        
        AtomService.Workspace workspace = new AtomService.Workspace();
        workspace.setTitle("My blog");
        workspace.addCollection(collection);
        
        AtomService service = new AtomService();
        service.addWorkspace(workspace);

        // serialize to XML
        Document doc = AtomService.serviceToDocument(service);
        assertEquals("service", doc.getRootElement().getName());
        assertEquals(1, doc.getRootElement().getContent().size());
  
        // deserialize from XML and assert we've got the same stuff
        AtomService service2 = AtomService.documentToService(doc);
        
        AtomService.Workspace workspace2 = 
            (AtomService.Workspace)service2.getWorkspaces().get(0);
        assertEquals("My blog", workspace2.getTitle());
        
        AtomService.Collection collection2 = 
            (AtomService.Collection)workspace2.getCollections().get(0);
        assertEquals("All blog entries", collection2.getTitle());
        assertEquals("entries", collection.getContents());
        assertEquals("http://example.com/collection1", collection2.getHref());
    }

    /** Compare times ignoring milliseconds */
    public void assertCloseEnough(Date d1, Date d2) {
        long t1 = d1.getTime() - d1.getTime() % 1000;
        long t2 = d2.getTime() - d2.getTime() % 1000;
        assertEquals(t1, t2);
    }
    
    public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new TestSuite(AtomCollectionTest.class));
		return suite;
	}
}
