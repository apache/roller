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

package org.apache.roller.util;

import org.apache.roller.ui.authoring.struts.actions.BookmarksActionTest;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author davidj
 */
public class LinkbackExtractorTest extends TestCase
{
    /**
     * Constructor for LinkbackExtractorTest.
     * @param arg0
     */
    public LinkbackExtractorTest(String arg0)
    {
        super(arg0);
    }

    public static void main(String[] args)
    {
    }

    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /**
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testLinkbackExtractor() throws Exception
    {
		String[][] testrefs = new String[][] 
		{
            { 
                "http://www.rollerweblogger.org/page/roller", 
                "http://staff.develop.com/halloway/weblog/2003/01/23.html" 
            },
            { 
                "http://postneo.com/", 
                "http://www.rollerweblogger.org/page/roller/20030125" 
            }
		};
		
		for ( int i=0; i<testrefs.length; i++ )
		{
			String refurl = testrefs[i][0];
			String requrl = testrefs[i][1];
			LinkbackExtractor le = new LinkbackExtractor(refurl,requrl);
			System.out.println(le.getTitle());
			System.out.println(le.getPermalink());
			System.out.println(le.getExcerpt());
		}		
    }

    public static Test suite() 
    {
        return new TestSuite(LinkbackExtractorTest.class);
    }
}
