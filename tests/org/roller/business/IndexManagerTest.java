package org.roller.business;

import junit.framework.Test;
import junit.framework.TestSuite;
 
import org.roller.business.search.operations.SearchOperation;
import org.roller.model.IndexManager;
import org.roller.RollerTestBase;

/**
 * @author Dave Johnson
 */
public class IndexManagerTest extends RollerTestBase
{
    public void testSearch() 
    {
        try
        {
            IndexManager imgr = getRoller().getIndexManager();
            
            SearchOperation search = new SearchOperation(imgr);
            search.setTerm("test");
            imgr.executeIndexOperationNow(search);
            assertTrue(search.getResultsCount() > 0);
            
            SearchOperation search2 = new SearchOperation(imgr);
            search2.setTerm("test");
            imgr.executeIndexOperationNow(search2);
            assertTrue(search2.getResultsCount() > 0);
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
