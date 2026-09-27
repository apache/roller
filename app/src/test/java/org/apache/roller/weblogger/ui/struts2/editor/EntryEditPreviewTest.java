/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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

import java.net.URI;
import org.apache.roller.weblogger.business.*;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.*;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class EntryEditPreviewTest {
    @Test
    void previewUsesTheEditorsOrigin() {
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getUrlStrategy()).thenReturn(new MultiWeblogURLStrategy());
        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class);
             MockedStatic<WebloggerRuntimeConfig> config = mockStatic(WebloggerRuntimeConfig.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            config.when(WebloggerRuntimeConfig::getAbsoluteContextURL).thenReturn("http://other.example/roller");
            Weblog weblog = new Weblog();
            weblog.setHandle("mainpage");
            WeblogEntry entry = new WeblogEntry();
            entry.setAnchor("test entry");
            EntryEdit action = new EntryEdit();
            action.setActionWeblog(weblog);
            action.setEntry(entry);
            for (String context : new String[]{"", "/roller"}) {
                config.when(WebloggerRuntimeConfig::getRelativeContextURL).thenReturn(context);
                String preview = action.getPreviewURL();
                assertEquals(context + "/roller-ui/authoring/preview/mainpage/?previewEntry=test+entry", preview);
                for (String scheme : new String[]{"http", "https"}) {
                    URI editor = URI.create(scheme + "://example.org:8443" + context + "/roller-ui/authoring/entryEdit.rol");
                    URI target = editor.resolve(preview);
                    assertEquals(editor.getScheme(), target.getScheme());
                    assertEquals(editor.getAuthority(), target.getAuthority());
                }
            }
        }
    }
}
