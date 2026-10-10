/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.webservices.atomprotocol;

import java.io.ByteArrayInputStream;
import java.util.Collections;

import org.apache.roller.weblogger.business.FileContentManager;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.util.RollerMessages;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MediaCollectionPathTest {

    private MockedStatic<WebloggerFactory> factory;
    private MockedStatic<WebloggerRuntimeConfig> runtimeConfig;
    private MediaFileManager files;
    private FileContentManager content;
    private Weblog weblog;
    private MediaFileDirectory defaultDir;
    private MediaCollection collection;

    @BeforeEach
    void setUp() throws Exception {
        factory = mockStatic(WebloggerFactory.class);
        runtimeConfig = mockStatic(WebloggerRuntimeConfig.class);
        runtimeConfig.when(WebloggerRuntimeConfig::getAbsoluteContextURL)
                .thenReturn("https://blog.example");

        files = mock(MediaFileManager.class);
        content = mock(FileContentManager.class);
        WeblogManager weblogs = mock(WeblogManager.class);
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getMediaFileManager()).thenReturn(files);
        when(weblogger.getFileContentManager()).thenReturn(content);
        when(weblogger.getWeblogManager()).thenReturn(weblogs);
        factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);

        weblog = mock(Weblog.class);
        when(weblog.getHandle()).thenReturn("blog");
        when(weblog.getName()).thenReturn("Blog");
        when(weblog.hasUserPermission(any(), eq(WeblogPermission.POST))).thenReturn(true);
        when(weblogs.getWeblogByHandle("blog")).thenReturn(weblog);

        defaultDir = mock(MediaFileDirectory.class);
        when(defaultDir.getMediaFiles()).thenReturn(Collections.emptySet());
        when(files.getDefaultMediaFileDirectory(weblog)).thenReturn(defaultDir);
        when(files.getMediaFileDirectoryByName(weblog, "default")).thenReturn(defaultDir);

        collection = new MediaCollection(new User(), "https://blog.example/roller-services/app");
    }

    @AfterEach
    void tearDown() {
        runtimeConfig.close();
        factory.close();
    }

    @Test
    void namedCollectionIsLookedUpByItsName() throws Exception {
        AtomFeed feed = collection.getCollection(request("/blog/resources/default"));

        assertTrue(feed.getEntries().isEmpty());
        verify(files).getMediaFileDirectoryByName(weblog, "default");
    }

    @Test
    void unknownCollectionIsNotFound() {
        assertThrows(AtomNotFoundException.class,
                () -> collection.getCollection(request("/blog/resources/missing")));
    }

    @Test
    void mediaPostedToTheRootCollectionGoesToTheDefaultDirectory() throws Exception {
        refuseUploads();

        AtomException refused = assertThrows(AtomException.class,
                () -> collection.postMedia(request("/blog/resources"), mediaEntry(null)));

        assertTrue(refused.getMessage().contains("refused"), refused.getMessage());
        verify(files).getDefaultMediaFileDirectory(weblog);
    }

    @Test
    void mediaPostedToAnUnknownDirectoryIsNotFound() {
        assertThrows(AtomNotFoundException.class,
                () -> collection.postMedia(request("/blog/resources/missing"), mediaEntry("a")));
    }

    @Test
    void mediaWithoutSlugOrTitleIsNamedByDate() throws Exception {
        refuseUploads();

        assertThrows(AtomException.class,
                () -> collection.postMedia(request("/blog/resources/default"), mediaEntry(null)));

        ArgumentCaptor<String> name = ArgumentCaptor.forClass(String.class);
        verify(content).canSave(eq(weblog), name.capture(), eq("image/png"), anyLong(), any());
        assertTrue(name.getValue().matches("blog-\\d+\\.png"), name.getValue());
        verify(files, never()).createMediaFile(any(), any(), any());
    }

    @Test
    void deleteThroughTheEditUriRemovesTheMediaFile() throws Exception {
        MediaFile file = mock(MediaFile.class);
        when(files.getMediaFileByPath(weblog, "default/a.png")).thenReturn(file);

        collection.deleteEntry(request("/blog/resource/default/a.png.media-link"));

        verify(files).removeMediaFile(weblog, file);
    }

    @Test
    void deleteOfAnUnknownFileIsNotFound() throws Exception {
        assertThrows(AtomNotFoundException.class,
                () -> collection.deleteEntry(request("/blog/resource/default/missing.png.media-link")));
        verify(files, never()).removeMediaFile(any(), any());
    }

    @Test
    void mediaLinkEntryOfAnUnknownWeblogIsNotFound() {
        assertThrows(AtomNotFoundException.class,
                () -> collection.getEntry(request("/nosuchblog/resource/default/a.png.media-link")));
    }

    @Test
    void mediaLinkEntryNeedsPermissionOnTheWeblog() throws Exception {
        when(weblog.hasUserPermission(any(), eq(WeblogPermission.POST))).thenReturn(false);

        assertThrows(AtomNotAuthorizedException.class,
                () -> collection.getEntry(request("/blog/resource/default/a.png.media-link")));
        verify(files, never()).getMediaFileByPath(any(), anyString());
    }

    @Test
    void getOfAnUnknownMediaResourceIsNotFound() {
        assertThrows(AtomNotFoundException.class,
                () -> collection.getMediaResource(request("/blog/resource/default/missing.png")));
    }

    @Test
    void putOfAnUnknownMediaResourceIsNotFound() {
        assertThrows(AtomNotFoundException.class,
                () -> collection.putMedia(request("/blog/resource/default/missing.png")));
    }

    private void refuseUploads() throws Exception {
        when(content.canSave(any(), anyString(), anyString(), anyLong(), any()))
                .thenAnswer(call -> {
                    call.<RollerMessages>getArgument(4).addError("refused");
                    return false;
                });
    }

    private static AtomRequest request(String pathInfo) throws Exception {
        AtomRequest request = mock(AtomRequest.class);
        when(request.getPathInfo()).thenReturn(pathInfo);
        when(request.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        return request;
    }

    private static AtomEntry mediaEntry(String title) {
        AtomContent body = new AtomContent();
        body.setType("image/png");
        AtomEntry entry = new AtomEntry();
        entry.setTitle(title);
        entry.setContent(body);
        return entry;
    }
}
