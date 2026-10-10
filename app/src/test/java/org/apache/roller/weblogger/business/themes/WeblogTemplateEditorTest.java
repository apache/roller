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

package org.apache.roller.weblogger.business.themes;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the template rules shared by the template editor pages and the
 * AtomPub template collection.
 */
public class WeblogTemplateEditorTest {

    private static final String HANDLE = "templateeditorblog";

    private User user;
    private Weblog weblog;

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
        user = TestUtils.setupUser("templateeditoruser");
        weblog = TestUtils.setupWeblog(HANDLE, user);
        TestUtils.endSession(true);
    }

    @AfterEach
    public void tearDown() throws Exception {
        TestUtils.teardownWeblog(weblog.getId());
        TestUtils.teardownUser(user.getUserName());
        TestUtils.endSession(true);
    }

    @Test
    public void sharedThemeMayAddCustomTemplatesAndOneWeblogTemplate() throws Exception {
        Set<ComponentType> actions = editor().availableActions(managedWeblog());
        assertEquals(Set.of(ComponentType.CUSTOM, ComponentType.WEBLOG), actions);

        editor().create(managedWeblog(), ComponentType.WEBLOG, "main", "x", null);
        TestUtils.endSession(true);
        assertEquals(Set.of(ComponentType.CUSTOM), editor().availableActions(managedWeblog()));
    }

    @Test
    public void customThemeMayAddEachActionOnce() throws Exception {
        useCustomTheme();
        assertEquals(Set.of(ComponentType.CUSTOM, ComponentType.PERMALINK, ComponentType.SEARCH,
                ComponentType.WEBLOG, ComponentType.TAGSINDEX),
                editor().availableActions(managedWeblog()));

        editor().create(managedWeblog(), ComponentType.PERMALINK, "permalink", "x", null);
        TestUtils.endSession(true);
        assertFalse(editor().availableActions(managedWeblog()).contains(ComponentType.PERMALINK));

        TemplateRuleException taken = assertThrows(TemplateRuleException.class, () ->
                editor().create(managedWeblog(), ComponentType.PERMALINK, "permalink2", "x", null));
        assertTrue(taken.isConflict());
        assertEquals("Template.error.actionNotAvailable",
                taken.getViolations().get(0).getMessageKey());
    }

    @Test
    public void newTemplatesNeedAUniqueNameOfValidLength() throws Exception {
        editor().create(managedWeblog(), ComponentType.CUSTOM, "sidebar", "x", null);
        TestUtils.endSession(true);

        List<TemplateRuleException.Violation> duplicate =
                editor().validateNew(managedWeblog(), ComponentType.CUSTOM, "sidebar");
        assertEquals("pagesForm.error.alreadyExists", duplicate.get(0).getMessageKey());
        assertTrue(duplicate.get(0).isConflict());

        List<TemplateRuleException.Violation> missing =
                editor().validateNew(managedWeblog(), null, "");
        assertEquals(List.of("Template.error.nameNull", "Template.error.actionNull"),
                missing.stream().map(TemplateRuleException.Violation::getMessageKey).collect(Collectors.toList()));

        List<TemplateRuleException.Violation> tooLong =
                editor().validateNew(managedWeblog(), ComponentType.CUSTOM, "x".repeat(256));
        assertEquals("Template.error.nameSize", tooLong.get(0).getMessageKey());
    }

    @Test
    public void createSetsLinkAndRenditions() throws Exception {
        WeblogTemplate created = editor().create(managedWeblog(), ComponentType.CUSTOM,
                "sidebar", "standard", "mobile");
        TestUtils.endSession(true);

        WeblogTemplate stored = weblogManager().getTemplate(managedWeblog(), created.getId());
        assertEquals("sidebar", stored.getLink());
        assertEquals("standard", stored.getTemplateRendition(RenditionType.STANDARD).getTemplate());
        assertEquals("mobile", stored.getTemplateRendition(RenditionType.MOBILE).getTemplate());
    }

    @Test
    public void mobileRenditionCanBeRemovedButStandardCannot() throws Exception {
        WeblogTemplate created = editor().create(managedWeblog(), ComponentType.CUSTOM,
                "sidebar", "standard", "mobile");
        TestUtils.endSession(true);

        WeblogTemplate stored = weblogManager().getTemplate(managedWeblog(), created.getId());
        editor().removeMobileRendition(stored);
        TestUtils.endSession(true);

        stored = weblogManager().getTemplate(managedWeblog(), created.getId());
        assertNull(stored.getTemplateRendition(RenditionType.MOBILE));
        assertNotNull(stored.getTemplateRendition(RenditionType.STANDARD));

        // removing a missing mobile rendition is a no-op
        editor().removeMobileRendition(stored);

        WeblogTemplate reloaded = stored;
        assertThrows(org.apache.roller.weblogger.WebloggerException.class, () -> weblogManager()
                .removeTemplateRendition(reloaded.getTemplateRendition(RenditionType.STANDARD)));
    }

    @Test
    public void renameMayNotTakeAnotherTemplatesNameOrLink() throws Exception {
        editor().create(managedWeblog(), ComponentType.CUSTOM, "one", "x", null);
        WeblogTemplate two = editor().create(managedWeblog(), ComponentType.CUSTOM, "two", "x", null);
        TestUtils.endSession(true);

        WeblogTemplate stored = weblogManager().getTemplate(managedWeblog(), two.getId());
        assertEquals(2, editor().validateUpdate(managedWeblog(), stored, "one", "one").size());
        assertTrue(editor().validateUpdate(managedWeblog(), stored, "two", "two").isEmpty());
    }

    @Test
    public void requiredTemplatesOfACustomThemeAreKept() throws Exception {
        useCustomTheme();
        WeblogTemplate main = editor().create(managedWeblog(), ComponentType.WEBLOG, "main", "x", null);
        TestUtils.endSession(true);

        WeblogTemplate stored = weblogManager().getTemplate(managedWeblog(), main.getId());
        assertTrue(stored.isRequired());
        TemplateRuleException refused = assertThrows(TemplateRuleException.class,
                () -> editor().remove(managedWeblog(), stored));
        assertEquals("editPages.remove.requiredTemplate",
                refused.getViolations().get(0).getMessageKey());
    }

    @Test
    public void removeDeletesTheTemplate() throws Exception {
        WeblogTemplate created = editor().create(managedWeblog(), ComponentType.CUSTOM,
                "sidebar", "x", null);
        TestUtils.endSession(true);

        editor().remove(managedWeblog(), weblogManager().getTemplate(managedWeblog(), created.getId()));
        TestUtils.endSession(true);
        assertNull(weblogManager().getTemplate(managedWeblog(), created.getId()));
        assertNotNull(managedWeblog());
    }

    private void useCustomTheme() throws Exception {
        Weblog managed = managedWeblog();
        managed.setEditorTheme(WeblogTheme.CUSTOM);
        weblogManager().saveWeblog(managed);
        TestUtils.endSession(true);
    }

    private static WeblogTemplateEditor editor() {
        return new WeblogTemplateEditor(WebloggerFactory.getWeblogger());
    }

    private static WeblogManager weblogManager() {
        return WebloggerFactory.getWeblogger().getWeblogManager();
    }

    private static Weblog managedWeblog() throws Exception {
        return weblogManager().getWeblogByHandle(HANDLE);
    }
}
