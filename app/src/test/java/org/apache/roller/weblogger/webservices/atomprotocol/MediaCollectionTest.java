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
