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
/*
 * Created on Jun 8, 2004
 */
package org.apache.roller.ui.rendering.velocity.plugins.smileys;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.roller.ui.core.MockRollerRequest;
import org.apache.roller.ui.rendering.velocity.VelocityServletTestBase;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockServletContext;
import org.apache.roller.presentation.velocity.plugins.smileys.*;

/**
 * @author lance.lavandowska
 */
public class SmileysTest extends VelocityServletTestBase
{
    public void testSmileEmoticon() throws Exception
    {
        MockServletContext ctx = getMockFactory().getMockServletContext();
        ctx.setServletContextName("/roller");      
        
        MockHttpServletRequest request = getMockFactory().getMockRequest();
        request.setContextPath("/roller");
       
        doFilters();

        SmileysPlugin plugin = new SmileysPlugin();
        plugin.init(mWebsite, null);
        assertTrue( SmileysPlugin.smileyPatterns.length > 0 );

        String test = "put on a happy :-) face";
        String expected = 
            "put on a happy <img src=\"/roller/images/smileys/smile.gif" +
            "\" class=\"smiley\" alt=\":-)\" title=\":-)\"> face";
        String result = plugin.render(null, test);
        //System.out.println(result);
        assertEquals(expected, result);
    }
        
    public SmileysTest()
    {
        super();
    }

    public static Test suite() 
    {
        return new TestSuite(SmileysTest.class);
    }
}
