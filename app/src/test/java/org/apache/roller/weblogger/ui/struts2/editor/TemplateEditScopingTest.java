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

package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Verifies that the template editor resolves the template it is asked to edit
 * within the weblog the request is acting on, rather than by id alone.
 */
public class TemplateEditScopingTest {

    public static Log log = LogFactory.getLog(TemplateEditScopingTest.class);

    User userOne = null;
    User userTwo = null;
    Weblog weblogOne = null;
    Weblog weblogTwo = null;

    @BeforeEach
    public void setUp() throws Exception {

        TestUtils.setupWeblogger();

        try {
            userOne = TestUtils.setupUser("tmplEditUserOne");
            userTwo = TestUtils.setupUser("tmplEditUserTwo");
            weblogOne = TestUtils.setupWeblog("tmplEditWeblogOne", userOne);
            weblogTwo = TestUtils.setupWeblog("tmplEditWeblogTwo", userTwo);
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }

    @AfterEach
    public void tearDown() throws Exception {

        try {
            TestUtils.teardownWeblog(weblogOne.getId());
            TestUtils.teardownWeblog(weblogTwo.getId());
            TestUtils.teardownUser(userOne.getUserName());
            TestUtils.teardownUser(userTwo.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
    }

    private String createTemplate(Weblog weblog, String name) throws Exception {
        WeblogTemplate template = new WeblogTemplate();
        template.setAction(ComponentType.WEBLOG);
        template.setName(name);
        template.setDescription("Test Weblog Template");
        template.setLink(name);
        template.setLastModified(new java.util.Date());
        template.setWeblog(TestUtils.getManagedWebsite(weblog));

        WebloggerFactory.getWeblogger().getWeblogManager().saveTemplate(template);
        TestUtils.endSession(true);

        return template.getId();
    }

    /**
     * Builds the action as the interceptor stack would: the action weblog is
     * already resolved and authorized before myPrepare() runs.
     */
    private TemplateEdit actionFor(Weblog actionWeblog, String requestedTemplateId)
            throws Exception {
        TemplateEdit action = new TemplateEdit();
        action.setActionWeblog(TestUtils.getManagedWebsite(actionWeblog));
        action.getBean().setId(requestedTemplateId);
        return action;
    }

    @Test
    public void testDoesNotLoadTemplateBelongingToAnotherWeblog() throws Exception {

        String foreignTemplateId = createTemplate(weblogTwo, "tmplEditForeign");

        TemplateEdit action = actionFor(weblogOne, foreignTemplateId);
        action.myPrepare();

        assertNull(action.getTemplate(),
                "editor must not load a template owned by another weblog");
    }

    @Test
    public void testLoadsTemplateBelongingToTheActionWeblog() throws Exception {

        String ownTemplateId = createTemplate(weblogOne, "tmplEditOwn");

        TemplateEdit action = actionFor(weblogOne, ownTemplateId);
        action.myPrepare();

        assertNotNull(action.getTemplate(),
                "editor must load a template owned by the action weblog");
        assertEquals(ownTemplateId, action.getTemplate().getId());
    }
}
