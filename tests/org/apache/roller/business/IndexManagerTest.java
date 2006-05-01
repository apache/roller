package org.apache.roller.business;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.roller.business.search.operations.AddEntryOperation;
import org.apache.roller.business.search.operations.SearchOperation;
import org.apache.roller.model.IndexManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.WeblogEntryData;


/**
 * Test Search Manager business layer operations.
 */
public class IndexManagerTest extends TestCase {
    
    
    public IndexManagerTest(String name) {
        super(name);
    }
    
    
    public static Test suite() {
        return new TestSuite(IndexManagerTest.class);
    }
    
    
    public void testSearch() throws Exception {
        try {
            IndexManager imgr = RollerFactory.getRoller().getIndexManager();
            
            WeblogEntryData wd1 = new WeblogEntryData();
            wd1.setId("dummy");
            wd1.setTitle("The Tholian Web");
            wd1.setText(
                    "When the Enterprise attempts to ascertain the fate of the U.S.S. "
                    +"Defiant which vanished 3 weeks ago, the warp engines begin to lose "
                    +"power, and Spock reports strange sensor readings.");
            imgr.executeIndexOperationNow(
                    new AddEntryOperation((IndexManagerImpl)imgr, wd1));
            
            WeblogEntryData wd2 = new WeblogEntryData();
            wd2.setId("dummy");
            wd2.setTitle("A Piece of the Action");
            wd2.setText(
                    "The crew of the Enterprise attempts to make contact with "
                    +"the inhabitants of planet Sigma Iotia II, and Uhura puts Kirk "
                    +"in communication with Boss Oxmyx.");
            imgr.executeIndexOperationNow(
                    new AddEntryOperation((IndexManagerImpl)imgr, wd2));
            
            SearchOperation search = new SearchOperation(imgr);
            search.setTerm("Enterprise");
            imgr.executeIndexOperationNow(search);
            assertTrue(search.getResultsCount() == 2);
            
            SearchOperation search2 = new SearchOperation(imgr);
            search2.setTerm("Tholian");
            imgr.executeIndexOperationNow(search2);
            assertTrue(search2.getResultsCount() == 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
