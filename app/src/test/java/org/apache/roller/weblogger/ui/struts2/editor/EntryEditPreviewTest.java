/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
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
