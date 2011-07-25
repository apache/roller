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

package org.apache.roller.weblogger.business;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTemplateCode;

public class WeblogTemplateCodeTest extends TestCase{
    public static Log log = LogFactory.getLog(WeblogPageTest.class);

       User testUser = null;
       Weblog testWeblog = null;
       WeblogTemplate testPage = null;
       WeblogTemplateCode standardCode = null;
       WeblogTemplateCode mobileCode = null;


       public WeblogTemplateCodeTest(String name) {
           super(name);
       }


       public static Test suite() {
           return new TestSuite(WeblogTemplateCodeTest.class);
       }


       /**
        * All tests in this suite require a user and a weblog.
        */
       public void setUp() throws Exception {

           // setup weblogger
           TestUtils.setupWeblogger();

           try {
               testUser = TestUtils.setupUser("wtTestUser");
               testWeblog = TestUtils.setupWeblog("wtTestWeblog", testUser);
               TestUtils.endSession(true);
           } catch (Exception ex) {
               log.error(ex);
               throw new Exception("Test setup failed", ex);
           }

           testPage = new WeblogTemplate();
           testPage.setAction(WeblogTemplate.ACTION_WEBLOG);
           testPage.setName("testTemplate");
           testPage.setDescription("Test Weblog Template");
           testPage.setLink("testTemp");
           testPage.setContents("a test weblog template.");
           testPage.setLastModified(new java.util.Date());
           testPage.setWebsite(TestUtils.getManagedWebsite(testWeblog));
           testPage.setTemplateLanguage("velocity");
           testPage.setType("standard");




       }

       public void tearDown() throws Exception {

           try {
               TestUtils.teardownWeblog(testWeblog.getId());
               TestUtils.teardownUser(testUser.getUserName());
               TestUtils.endSession(true);
           } catch (Exception ex) {
               log.error(ex);
               throw new Exception("Test teardown failed", ex);
           }

           testPage = null;
       }


       /**
        * Test basic persistence operations ... Create, Update, Delete
        */
       public void testTemplateCRUD() throws Exception {

           WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();


           // create template
           mgr.savePage(testPage);


           //create standard template coce
           WeblogTemplateCode standardTemplateCode = new WeblogTemplateCode(testPage.getId(),"standard");
           standardTemplateCode.setTemplate("standard.template.code");
           mgr.saveTemplateCode(standardTemplateCode);
            //TestUtils.endSession(true);
           //create mobile code
           WeblogTemplateCode mobileTemplateCode = new WeblogTemplateCode(testPage.getId(),"mobile");
           mobileTemplateCode.setTemplate("mobile.template.code");
           mgr.saveTemplateCode(mobileTemplateCode);
             TestUtils.endSession(true);


           // check that create was successful
            standardCode = mgr.getTemplateCodeByType(testPage.getId(), "standard");
           assertNotNull(standardCode);
           assertEquals(standardTemplateCode.getTemplate() ,standardCode.getTemplate());

            mobileCode = mgr.getTemplateCodeByType(testPage.getId(), "mobile");
           assertNotNull(mobileCode);
           assertEquals(mobileTemplateCode.getTemplate() ,mobileCode.getTemplate());

           // update template Code
           standardCode = null;
           standardCode = mgr.getTemplateCodeByType(testPage.getId(), "standard");
           standardCode.setTemplate("update.standard.template");
           mgr.saveTemplateCode(standardCode);

           mobileCode = null;
           mobileCode = mgr.getTemplateCodeByType(testPage.getId(), "mobile");
           mobileCode.setTemplate("update.mobile.template");
           mgr.saveTemplateCode(mobileCode);

           TestUtils.endSession(true);

           // check that update was successful
           standardCode =null;
            standardCode = mgr.getTemplateCodeByType(testPage.getId(), "standard");
            assertEquals("update.standard.template",standardCode.getTemplate());

           mobileCode =null;
           mobileCode = mgr.getTemplateCodeByType(testPage.getId(), "mobile");
           assertEquals("update.mobile.template",mobileCode.getTemplate());

           WeblogTemplate page = mgr.getPage(testPage.getId());
           mgr.removePage(page);
           TestUtils.endSession(true);

           // check that update was successful
           standardCode =null;
            standardCode = mgr.getTemplateCodeByType(testPage.getId(), "standard");
            assertNull(standardCode);

           mobileCode =null;
           mobileCode = mgr.getTemplateCodeByType(testPage.getId(), "mobile");
           assertNull(mobileCode);

       }



}
