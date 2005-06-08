/*
 * Created on Oct 27, 2003
 */
package org.roller.presentation.bookmarks;

import com.mockrunner.mock.web.MockActionMapping;
import com.mockrunner.struts.ActionTestModule;

import org.roller.presentation.ServletTestBase;
import org.roller.presentation.bookmarks.actions.BookmarksAction;
import org.roller.presentation.bookmarks.formbeans.BookmarksForm;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author dmj
 */
public class BookmarksActionTest extends ServletTestBase
{    
    private ActionTestModule module;
    private BookmarksForm form;
    
    //------------------------------------------------------------------------
    public static Test suite() 
    {
        return new TestSuite(BookmarksActionTest.class);
    }

    public void testSelectFolder() 
    {
        MockActionMapping mapping = module.getMockActionMapping();
        mapping.setForward("BookmarksForm");
        mapping.setupForwards( new String[] {"access-denied"} );
        mapping.setParameter("method");
        
        module.addRequestParameter("method","selectFolder");
        
        authenticateUser();

        module.actionPerform(BookmarksAction.class, form);
        
        module.verifyNoActionMessages();
        module.verifyForward("BookmarksForm");
    }
    
    //------------------------------------------------------------------------
    /**
     * @see TestCase#setUp()
     */
    public void setUp() throws Exception
    {
        super.setUp();
        
        module = new ActionTestModule(mockFactory); 
        form = (BookmarksForm)module.createActionForm(BookmarksForm.class);
        //module.setValidate(true);
    }

    //------------------------------------------------------------------------

    /**
     * Child TestCases should take care to tearDown() their own resources
     * (including their own implementation).  RollerTestBase will clean up the
     * getRoller() instance.
     * 
     * @see TestCase#tearDown()
     */
    public void tearDown() throws Exception
    {      
        super.tearDown();
    }
}
