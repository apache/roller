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

package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.List;
import java.util.function.Supplier;

import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.ui.struts2.util.UISecurityEnforced;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

/**
 * The Design menu appears only while the site allows custom themes, and the
 * pages behind it must follow the same setting.
 */
public class DesignPagesFeatureTest {

    private static final List<Supplier<UISecurityEnforced>> DESIGN_PAGES = List.of(
            ThemeEdit::new, Templates::new, TemplateEdit::new, StylesheetEdit::new);

    private static void assertDesignPagesEnabled(boolean customThemesAllowed) {
        try (MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class)) {
            config.when(() -> WebloggerRuntimeConfig.getBooleanProperty("themes.customtheme.allowed"))
                    .thenReturn(customThemesAllowed);
            for (Supplier<UISecurityEnforced> page : DESIGN_PAGES) {
                UISecurityEnforced action = page.get();
                String name = action.getClass().getSimpleName();
                if (customThemesAllowed) {
                    assertTrue(action.isFeatureEnabled(), name);
                } else {
                    assertFalse(action.isFeatureEnabled(), name);
                }
            }
        }
    }

    @Test
    public void designPagesAreAvailableWhileCustomThemesAreAllowed() {
        assertDesignPagesEnabled(true);
    }

    @Test
    public void designPagesAreUnavailableWhileCustomThemesAreNotAllowed() {
        assertDesignPagesEnabled(false);
    }
}
