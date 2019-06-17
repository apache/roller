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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.pojos.CustomTemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CustomTemplateRenditionTest  {
    public static Log log = LogFactory.getLog(CustomTemplateRenditionTest.class);

       User testUser = null;
       Weblog testWeblog = null;
       WeblogTemplate testPage = null;
       CustomTemplateRendition standardCode = null;
       CustomTemplateRendition mobileCode = null;


       /**
        * All tests in this suite require a user and a weblog.
        */
       @BeforeEach
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
           testPage.setAction(ComponentType.WEBLOG);
           testPage.setName("testTemplate");
           testPage.setDescription("Test Weblog Template");
           testPage.setLink("testTemp");
           testPage.setLastModified(new java.util.Date());
           testPage.setWeblog(TestUtils.getManagedWebsite(testWeblog));
       }

       @AfterEach
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
       @Test
       public void testTemplateCRUD() throws Exception {
           WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();

           // create template
           mgr.saveTemplate(testPage);

           //create standard template rendition
           CustomTemplateRendition standardTemplateCode = new CustomTemplateRendition(testPage, RenditionType.STANDARD);
           standardTemplateCode.setTemplate("standard.template.code");
           standardTemplateCode.setTemplateLanguage(TemplateLanguage.VELOCITY);

           //create mobile code
           CustomTemplateRendition mobileTemplateCode = new CustomTemplateRendition(testPage, RenditionType.MOBILE);
           mobileTemplateCode.setTemplate("mobile.template.code");
           mobileTemplateCode.setTemplateLanguage(TemplateLanguage.VELOCITY);

           TestUtils.endSession(true);

           // check that create was successful
           WeblogTemplate testPageCheck = mgr.getTemplate(testPage.getId());

           assertNotNull(testPageCheck);

           standardCode = testPageCheck.getTemplateRendition(RenditionType.STANDARD);
           assertNotNull(standardCode);
           assertEquals(standardTemplateCode.getTemplate(), standardCode.getTemplate());

           mobileCode = testPageCheck.getTemplateRendition(RenditionType.MOBILE);
           assertNotNull(mobileCode);
           assertEquals(mobileTemplateCode.getTemplate() ,mobileCode.getTemplate());

           // update template Code
           standardCode = null;
           standardCode = testPageCheck.getTemplateRendition(RenditionType.STANDARD);
           standardCode.setTemplate("update.standard.template");
           mgr.saveTemplateRendition(standardCode);

           mobileCode = null;
           mobileCode = testPageCheck.getTemplateRendition(RenditionType.MOBILE);
           mobileCode.setTemplate("update.mobile.template");
           mgr.saveTemplateRendition(mobileCode);

           TestUtils.endSession(true);

           // check that update was successful
           standardCode = null;
           standardCode = testPageCheck.getTemplateRendition(RenditionType.STANDARD);
           assertEquals("update.standard.template",standardCode.getTemplate());

           mobileCode = null;
           mobileCode = testPageCheck.getTemplateRendition(RenditionType.MOBILE);
           assertEquals("update.mobile.template",mobileCode.getTemplate());

           WeblogTemplate page = mgr.getTemplate(testPage.getId());
           mgr.removeTemplate(page);
           TestUtils.endSession(true);

           // check that template remove was successful
           testPageCheck = mgr.getTemplate(testPage.getId());
           assertNull(testPageCheck);

       }
}
