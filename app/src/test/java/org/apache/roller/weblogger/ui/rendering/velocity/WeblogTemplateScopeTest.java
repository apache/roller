/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.ui.rendering.velocity;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.CustomTemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.pojos.wrapper.WeblogWrapper;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository.DeviceType;
import org.apache.velocity.exception.VelocityException;
import org.apache.velocity.util.ExtProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Renders weblogs' own stored templates through the shipped engine and
 * VelocityRenderer, as the page servlets do, to check that one weblog's
 * templates and macros stay its own: another weblog cannot read them with
 * #include or #parse, or replace them by defining a macro of the same name.
 */
public class WeblogTemplateScopeTest {

    private static final Log LOG = LogFactory.getLog(WeblogTemplateScopeTest.class);

    private User userA;
    private User userB;
    private Weblog weblogA;
    private Weblog weblogB;

    /** The page model a weblog template sees, reduced to what these use. */
    public static class StubModel {
        private final WeblogWrapper weblog;

        StubModel(Weblog weblog) {
            this.weblog = WeblogWrapper.wrap(weblog,
                    WebloggerFactory.getWeblogger().getUrlStrategy());
        }

        public WeblogWrapper getWeblog() {
            return weblog;
        }

        public String getDeviceType() {
            return DeviceType.standard.toString();
        }
    }

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
        RollerVelocityTestSupport.install();
        try {
            userA = TestUtils.setupUser("scopeRenderUserA");
            userB = TestUtils.setupUser("scopeRenderUserB");
            weblogA = customTheme(TestUtils.setupWeblog("scopeRenderWeblogA", userA));
            weblogB = customTheme(TestUtils.setupWeblog("scopeRenderWeblogB", userB));
            TestUtils.endSession(true);
        } catch (Exception ex) {
            LOG.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }

    @AfterEach
    public void tearDown() throws Exception {
        try {
            TestUtils.teardownWeblog(weblogA.getId());
            TestUtils.teardownWeblog(weblogB.getId());
            TestUtils.teardownUser(userA.getUserName());
            TestUtils.teardownUser(userB.getUserName());
            TestUtils.endSession(true);
        } catch (Exception ex) {
            LOG.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
    }

    private static Weblog customTheme(Weblog weblog) throws Exception {
        weblog.setEditorTheme(WeblogTheme.CUSTOM);
        WebloggerFactory.getWeblogger().getWeblogManager().saveWeblog(weblog);
        return weblog;
    }

    private static String createTemplate(Weblog weblog, String name, String source)
            throws Exception {
        WeblogTemplate template = new WeblogTemplate();
        template.setAction(ComponentType.CUSTOM);
        template.setName(name);
        template.setDescription(name);
        template.setLastModified(new java.util.Date());
        template.setWeblog(TestUtils.getManagedWebsite(weblog));
        CustomTemplateRendition rendition =
                new CustomTemplateRendition(template, RenditionType.STANDARD);
        rendition.setTemplate(source);
        rendition.setTemplateLanguage(TemplateLanguage.VELOCITY);
        WebloggerFactory.getWeblogger().getWeblogManager().saveTemplate(template);
        TestUtils.endSession(true);
        return template.getId();
    }

    private static void editTemplate(String id, String source) throws Exception {
        WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
        CustomTemplateRendition rendition = mgr.getTemplate(id)
                .getTemplateRendition(RenditionType.STANDARD);
        rendition.setTemplate(source);
        mgr.saveTemplateRendition(rendition);
        TestUtils.endSession(true);
    }

    private static String render(Weblog weblog, String templateId) throws Exception {
        WeblogTemplate template = WebloggerFactory.getWeblogger().getWeblogManager()
                .getTemplate(templateId);
        Map<String, Object> model = new HashMap<>();
        model.put("model", new StubModel(TestUtils.getManagedWebsite(weblog)));
        StringWriter out = new StringWriter();
        new VelocityRenderer(template, DeviceType.standard).render(model, out);
        return out.toString();
    }

    @Test
    public void includeCannotReadAnotherWeblogsTemplate() throws Exception {
        String secret = createTemplate(weblogB, "secret", "B-PRIVATE-SOURCE #set($x = 1)");
        String page = createTemplate(weblogA, "page", "[#include(\"" + secret + "|standard\")]");

        String output = render(weblogA, page);
        assertFalse(output.contains("B-PRIVATE-SOURCE"),
                "#include returned another weblog's template:\n" + output);
    }

    @Test
    public void parseCannotRenderAnotherWeblogsTemplate() throws Exception {
        String secret = createTemplate(weblogB, "secret", "B-PRIVATE-SOURCE");
        String page = createTemplate(weblogA, "page", "[#parse(\"" + secret + "|standard\")]");

        String output = render(weblogA, page);
        assertFalse(output.contains("B-PRIVATE-SOURCE"),
                "#parse rendered another weblog's template:\n" + output);
    }

    @Test
    public void aWeblogCanStillIncludeItsOwnTemplates() throws Exception {
        String part = createTemplate(weblogA, "part", "A-PART");
        String page = createTemplate(weblogA, "page",
                "[#include(\"" + part + "|standard\")]"
                        + "[#parse(\"" + part + "|standard\")]"
                        + "[#includeTemplate($model.weblog \"part\")]");

        assertEquals("[A-PART][A-PART][A-PART]", render(weblogA, page).trim());
    }

    @Test
    public void errorPageDoesNotShowTemplateIds() throws Exception {
        String page = createTemplate(weblogA, "broken-page", "#foreach(");
        String part = createTemplate(weblogA, "broken-part", "#foreach(");
        String includer = createTemplate(weblogA, "includer",
                "#includeTemplate($model.weblog \"broken-part\")");

        String output = render(weblogA, page);
        assertTrue(output.contains("Velocity template error"),
                "expected the error page:\n" + output);
        assertFalse(output.contains(page), "the error page showed the template id:\n" + output);
        assertTrue(output.contains("line 1, column"),
                "the error page should still say where the error is:\n" + output);

        output = render(weblogA, includer);
        assertTrue(output.contains("Velocity template error"),
                "expected the error page:\n" + output);
        assertFalse(output.contains(includer) || output.contains(part),
                "the error page showed a template id:\n" + output);
    }

    @Test
    public void inlineMacrosBelongToTheWeblogThatDefinesThem() throws Exception {
        String pageA = createTemplate(weblogA, "page", "#macro(scopeProbe)A-MACRO#end[#scopeProbe()]");
        String pageB = createTemplate(weblogB, "page", "#macro(scopeProbe)B-MACRO#end[#scopeProbe()]");

        assertEquals("[A-MACRO]", render(weblogA, pageA).trim());
        assertEquals("[B-MACRO]", render(weblogB, pageB).trim(),
                "weblog B rendered weblog A's macro");
        assertEquals("[A-MACRO]", render(weblogA, pageA).trim());

        editTemplate(pageA, "#macro(scopeProbe)A-EDITED#end[#scopeProbe()]");
        assertEquals("[A-EDITED]", render(weblogA, pageA).trim(),
                "an edited macro was not picked up");
        assertEquals("[B-MACRO]", render(weblogB, pageB).trim());
    }

    @Test
    public void anInlineMacroDoesNotReplaceALibraryMacroForOtherWeblogs() throws Exception {
        String pageA = createTemplate(weblogA, "page",
                "#macro(includeTemplate $weblog $name)A-REPLACED#end"
                        + "[#includeTemplate($model.weblog \"part\")]");
        createTemplate(weblogB, "part", "B-PART");
        String pageB = createTemplate(weblogB, "page",
                "[#includeTemplate($model.weblog \"part\")]");

        assertEquals("[A-REPLACED]", render(weblogA, pageA).trim());
        assertEquals("[B-PART]", render(weblogB, pageB).trim(),
                "weblog A's macro replaced the library macro for weblog B");
    }

    @Test
    public void macrosStillReachAcrossAWeblogsOwnTemplates() throws Exception {
        createTemplate(weblogA, "macros", "#macro(fromLibrary)LIB-OF-A#end");
        createTemplate(weblogA, "child", "[child:#fromParent()]");
        createTemplate(weblogA, "uses-library", "[sibling:#fromLibrary()]");
        String defs = createTemplate(weblogA, "defs", "#macro(fromDefs)DEFS-OF-A#end");

        // a macro defined in a template the page includes, through the
        // library macro themes use and through #parse directly
        String viaIncludeTemplate = createTemplate(weblogA, "page1",
                "#includeTemplate($model.weblog \"macros\")[#fromLibrary()]");
        assertEquals("[LIB-OF-A]", render(weblogA, viaIncludeTemplate).trim());

        String viaParse = createTemplate(weblogA, "page2",
                "#parse(\"" + defs + "|standard\")[#fromDefs()]");
        assertEquals("[DEFS-OF-A]", render(weblogA, viaParse).trim());

        // a macro the page defines, called from a template it includes
        String parentToChild = createTemplate(weblogA, "page3",
                "#macro(fromParent)PARENT-OF-A#end#includeTemplate($model.weblog \"child\")");
        assertEquals("[child:PARENT-OF-A]", render(weblogA, parentToChild).trim());

        // a macro one included template defines, called from the next
        String siblings = createTemplate(weblogA, "page4",
                "#includeTemplate($model.weblog \"macros\")"
                        + "#includeTemplate($model.weblog \"uses-library\")");
        assertEquals("[sibling:LIB-OF-A]", render(weblogA, siblings).trim());

        // weblog B does not see any of weblog A's macros
        String pageB = createTemplate(weblogB, "page", "[#fromLibrary()]");
        assertEquals("[#fromLibrary()]", render(weblogB, pageB).trim());
    }

    /**
     * Velocity's cache would hand a template loaded for one weblog to another
     * without asking the loader, so the stored template loader won't start
     * with caching on.
     */
    @Test
    public void theStoredTemplateLoaderDoesNotStartWithCachingOn() {
        ExtProperties config = new ExtProperties();
        config.setProperty("cache", "true");
        assertThrows(VelocityException.class, () -> new RollerResourceLoader().init(config));

        ExtProperties shipped = new ExtProperties();
        shipped.setProperty("cache", "false");
        new RollerResourceLoader().init(shipped);
    }
}
