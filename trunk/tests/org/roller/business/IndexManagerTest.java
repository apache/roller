package org.roller.business;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.roller.RollerTestBase;
import org.roller.business.search.operations.AddEntryOperation;
import org.roller.business.search.operations.SearchOperation;
import org.roller.model.IndexManager;
import org.roller.pojos.WeblogEntryData;

/**
 * @author Dave Johnson
 */
public class IndexManagerTest extends RollerTestBase
{
    public void testSearch() throws Exception
    {
        try
        {
            IndexManager imgr = getRoller().getIndexManager();
            
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public static Test suite() 
    {
        return new TestSuite(IndexManagerTest.class);
    }
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(IndexManagerTest.class);
    }
}
