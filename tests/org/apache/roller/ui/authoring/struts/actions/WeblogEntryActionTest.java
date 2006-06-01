/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
package org.apache.roller.ui.authoring.struts.actions;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.roller.RollerException;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.UserData;

import com.mockrunner.mock.web.MockActionMapping;
import com.mockrunner.mock.web.MockHttpServletRequest;
import org.apache.roller.ui.StrutsActionTestBase;
import org.apache.roller.ui.authoring.struts.formbeans.WeblogEntryFormEx;
import org.apache.roller.ui.core.RollerRequest;

/**
 * @author dave
 */
public class WeblogEntryActionTest extends StrutsActionTestBase
{
    public void testCreateWeblogEntry() 
    {
        MockHttpServletRequest mockRequest = getMockFactory().getMockRequest();
        mockRequest.setContextPath("/dummy");        
        doFilters();
        
        UserManager umgr = null;
        UserData user = null; 
        try
        {
            umgr = getRoller().getUserManager();
            user = (UserData)umgr.getUsers(mWebsite, null, 0, Integer.MAX_VALUE).get(0);       
            authenticateUser(user.getUserName(), "editor");
        }
        catch (RollerException e)
        {
            e.printStackTrace();
            fail();
        }
        
        // Setup mapping and request parameters
        MockActionMapping mapping = strutsModule.getMockActionMapping();
        mapping.setupForwards(new String[] {
            "access-denied","weblogEdit.page","weblogEntryRemove.page"});
        mapping.setParameter("method");  
        strutsModule.addRequestParameter("weblog",mWebsite.getHandle()); 
        strutsModule.addRequestParameter("method","create"); 
        
        // Setup form bean
        WeblogEntryFormEx form = (WeblogEntryFormEx)
            strutsModule.createActionForm(WeblogEntryFormEx.class);
        form.setTitle("test_title");
        form.setText("Test blog text");

        try {
            RollerRequest rreq = new RollerRequest(strutsModule.getMockPageContext());
            rreq.setWebsite(mWebsite);
            strutsModule.setRequestAttribute(RollerRequest.ROLLER_REQUEST, rreq);
            strutsModule.actionPerform(WeblogEntryFormAction.class, form);        
        } catch (Throwable t) {
            t.printStackTrace();
            fail();
        }
        // Test for success
        strutsModule.verifyNoActionMessages();
        strutsModule.verifyForward("weblogEdit.page");
        
        // Verify objects we put in context for JSP page
        verifyPageContext();
    }
    
    protected void verifyPageContext() 
    {
        HttpServletRequest req = (HttpServletRequest)
            servletModule.getFilteredRequest();
        assertNotNull(req.getAttribute("model"));
    }

    public static Test suite() 
    {
        return new TestSuite(WeblogEntryActionTest.class);
    }

}
