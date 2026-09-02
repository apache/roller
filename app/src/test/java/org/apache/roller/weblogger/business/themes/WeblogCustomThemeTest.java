/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.themes;

import java.io.ByteArrayInputStream;

import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.ThemeResource;
import org.apache.roller.weblogger.pojos.Weblog;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class WeblogCustomThemeTest {

    @Test
    void mediaFileBackedResourcesAreReturnedAsThemeResources() throws Exception {
        Weblog weblog = mock(Weblog.class);
        Weblogger roller = mock(Weblogger.class);
        MediaFileManager manager = mock(MediaFileManager.class);
        MediaFile mediaFile = mock(MediaFile.class);
        ByteArrayInputStream content = new ByteArrayInputStream(new byte[]{1, 2, 3});

        when(roller.getMediaFileManager()).thenReturn(manager);
        when(manager.getMediaFileByOriginalPath(weblog, "css/site.css"))
                .thenReturn(mediaFile);
        when(mediaFile.getName()).thenReturn("site.css");
        when(mediaFile.getOriginalPath()).thenReturn("css/site.css");
        when(mediaFile.getLength()).thenReturn(3L);
        when(mediaFile.getLastModified()).thenReturn(7L);
        when(mediaFile.getInputStream()).thenReturn(content);

        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(roller);

            ThemeResource resource = new WeblogCustomTheme(weblog)
                    .getResource("css/site.css");

            assertNotNull(resource);
            assertEquals("site.css", resource.getName());
            assertEquals("css/site.css", resource.getPath());
            assertEquals(3L, resource.getLength());
            assertEquals(7L, resource.getLastModified());
            assertSame(content, resource.getInputStream());
        }
    }
}
