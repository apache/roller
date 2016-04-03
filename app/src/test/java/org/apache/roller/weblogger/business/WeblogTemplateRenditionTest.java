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
*
* Source file modified from the original ASF source; all changes made
* are also under Apache License.
*
* Source file modified from the original ASF source; all changes made
* are also under Apache License.
*/
package org.apache.roller.weblogger.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.pojos.WeblogTemplateRendition;
import org.apache.roller.weblogger.pojos.Template.ComponentType;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class WeblogTemplateRenditionTest extends WebloggerTest {
    public static Log log = LogFactory.getLog(WeblogPageTest.class);

    User testUser = null;
    Weblog testWeblog = null;
    WeblogTemplate testPage = null;
    WeblogTemplateRendition standardCode = null;
    WeblogTemplateRendition mobileCode = null;

    /**
     * All tests in this suite require a user and a weblog.
     */
    @Before
    public void setUp() throws Exception {
        super.setUp();

        try {
            testUser = setupUser("wtTestUser3");
            testWeblog = setupWeblog("wtTestWeblog", testUser);
            endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }

        testPage = new WeblogTemplate();
        testPage.setId(WebloggerCommon.generateUUID());
        testPage.setRole(ComponentType.WEBLOG);
        testPage.setName("testTemplate");
        testPage.setDescription("Test Weblog Template");
        testPage.setRelativePath("testTemp");
        testPage.setLastModified(new java.util.Date());
        testPage.setWeblog(getManagedWeblog(testWeblog));
    }

    @After
    public void tearDown() throws Exception {
        try {
            teardownWeblog(testWeblog.getId());
            teardownUser(testUser.getUserName());
            endSession(true);
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
        // create template
        weblogManager.saveTemplate(testPage);

        //create standard template rendition
        WeblogTemplateRendition standardTemplateCode = new WeblogTemplateRendition(testPage,
                RenditionType.STANDARD);
        standardTemplateCode.setTemplate("standard.template.code");
        standardTemplateCode.setTemplateLanguage(TemplateLanguage.VELOCITY);

        //create mobile code
        WeblogTemplateRendition mobileTemplateCode = new WeblogTemplateRendition(testPage,
                RenditionType.MOBILE);
        mobileTemplateCode.setTemplate("mobile.template.code");
        mobileTemplateCode.setTemplateLanguage(TemplateLanguage.VELOCITY);

        endSession(true);

        // check that create was successful
        WeblogTemplate testPageCheck = weblogManager.getTemplate(testPage.getId());

        assertNotNull(testPageCheck);

        standardCode = testPageCheck.getTemplateRendition(RenditionType.STANDARD);
        assertNotNull(standardCode);
        assertEquals(standardTemplateCode.getTemplate(), standardCode.getTemplate());

        mobileCode = testPageCheck.getTemplateRendition(RenditionType.MOBILE);
        assertNotNull(mobileCode);
        assertEquals(mobileTemplateCode.getTemplate(), mobileCode.getTemplate());

        // update template Code
        standardCode = null;
        standardCode = testPageCheck.getTemplateRendition(RenditionType.STANDARD);
        standardCode.setTemplate("update.standard.template");
        weblogManager.saveTemplateRendition(standardCode);

        mobileCode = null;
        mobileCode = testPageCheck.getTemplateRendition(RenditionType.MOBILE);
        mobileCode.setTemplate("update.mobile.template");
        weblogManager.saveTemplateRendition(mobileCode);

        endSession(true);

        // check that update was successful
        standardCode = null;
        standardCode = testPageCheck.getTemplateRendition(RenditionType.STANDARD);
        assertEquals("update.standard.template", standardCode.getTemplate());

        mobileCode = null;
        mobileCode = testPageCheck.getTemplateRendition(RenditionType.MOBILE);
        assertEquals("update.mobile.template", mobileCode.getTemplate());

        WeblogTemplate page = weblogManager.getTemplate(testPage.getId());
        weblogManager.removeTemplate(page);
        endSession(true);

        // check that template remove was successful
        testPageCheck = weblogManager.getTemplate(testPage.getId());
        assertNull(testPageCheck);

    }
}
