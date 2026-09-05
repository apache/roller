/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.webservices.atomprotocol;

import java.io.ByteArrayInputStream;

import javax.servlet.http.HttpServletResponse;

import com.rometools.propono.atom.server.AtomMediaResource;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.util.MediaTypePolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MediaCollectionTest {

    @Test
    void activeStoredTypeIsReturnedAsADownload() throws Exception {
        MediaFile mediaFile = mediaFile("page.html", "text/html");
        HttpServletResponse response = mock(HttpServletResponse.class);

        AtomMediaResource resource = MediaCollection.createMediaResource(mediaFile, response);

        assertEquals(MediaTypePolicy.DEFAULT_TYPE, resource.getContentType());
        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response).setHeader("Content-Disposition",
                "attachment; filename=\"page.html\"");
        verify(response).setContentType(MediaTypePolicy.DEFAULT_TYPE);
    }

    @Test
    void passiveStoredTypeRemainsInline() throws Exception {
        MediaFile mediaFile = mediaFile("notes.txt", "text/plain");
        HttpServletResponse response = mock(HttpServletResponse.class);

        AtomMediaResource resource = MediaCollection.createMediaResource(mediaFile, response);

        assertEquals("text/plain", resource.getContentType());
        verify(response).setHeader("X-Content-Type-Options", "nosniff");
        verify(response).setContentType("text/plain");
    }

    private MediaFile mediaFile(String name, String type) {
        MediaFile mediaFile = mock(MediaFile.class);
        when(mediaFile.getName()).thenReturn(name);
        when(mediaFile.getContentType()).thenReturn(type);
        when(mediaFile.getLength()).thenReturn(3L);
        when(mediaFile.getLastModified()).thenReturn(7L);
        when(mediaFile.getInputStream())
                .thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        return mediaFile;
    }
}
