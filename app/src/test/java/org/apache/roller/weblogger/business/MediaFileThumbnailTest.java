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
package org.apache.roller.weblogger.business;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import javax.imageio.ImageIO;

import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.RollerMessages;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

/**
 * Thumbnails are made for ordinary images, and skipped, without decoding the
 * image, for images whose pixel count is over uploads.thumbnail.maxPixels.
 * The upload itself succeeds either way.
 */
public class MediaFileThumbnailTest {

    private User user;
    private Weblog weblog;

    @BeforeEach
    public void setUp() throws Exception {
        TestUtils.setupWeblogger();
        Map<String, RuntimeConfigProperty> config =
                WebloggerFactory.getWeblogger().getPropertiesManager().getProperties();
        config.get("uploads.enabled").setValue("true");
        user = TestUtils.setupUser("thumbnailTestUser");
        weblog = TestUtils.setupWeblog("thumbnailTestWeblog", user);
        TestUtils.endSession(true);
    }

    @AfterEach
    public void tearDown() throws Exception {
        TestUtils.teardownWeblog(weblog.getId());
        TestUtils.teardownUser(user.getUserName());
        TestUtils.endSession(true);
    }

    /** Upload an image and reload it with its content and thumbnail. */
    private MediaFile upload(String name, String type, InputStream in, long length)
            throws Exception {
        MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();
        Weblog managed = TestUtils.getManagedWebsite(weblog);
        MediaFileDirectory dir = mgr.getDefaultMediaFileDirectory(managed);

        MediaFile mediaFile = new MediaFile();
        mediaFile.setName(name);
        mediaFile.setLength(length);
        mediaFile.setDirectory(dir);
        mediaFile.setWeblog(managed);
        mediaFile.setContentType(type);
        mediaFile.setInputStream(in);
        dir.getMediaFiles().add(mediaFile);

        RollerMessages errors = new RollerMessages();
        mgr.createMediaFile(managed, mediaFile, errors);
        TestUtils.endSession(true);
        assertEquals(0, errors.getErrorCount(), "upload should succeed");
        assertNotNull(mediaFile.getId());
        return mgr.getMediaFile(mediaFile.getId(), true);
    }

    @Test
    public void testThumbnailIsMadeForNormalImage() throws Exception {
        MediaFile saved = upload("hawk.jpg", "image/jpeg",
                getClass().getResourceAsStream(MediaFileTest.TEST_IMAGE), 2000);

        assertEquals(500, saved.getWidth());
        assertEquals(373, saved.getHeight());
        try (InputStream in = saved.getThumbnailInputStream()) {
            assertNotNull(in, "a normal image should get a thumbnail");
            BufferedImage thumbnail = ImageIO.read(in);
            assertEquals(MediaFileManager.MAX_WIDTH, thumbnail.getWidth());
            assertTrue(thumbnail.getHeight() <= MediaFileManager.MAX_HEIGHT);
        }
    }

    /**
     * A few hundred bytes of PNG whose header claims 50000 x 50000 pixels
     * must not be decoded.
     */
    @Test
    public void testHugeImageIsStoredWithoutThumbnail() throws Exception {
        byte[] png = pngHeaderOnly(50000, 50000);
        MediaFile saved = upload("huge.png", "image/png",
                new ByteArrayInputStream(png), png.length);

        assertNotNull(saved.getInputStream(), "the upload itself must be kept");
        assertNull(saved.getThumbnailInputStream());
        // the size still comes from the header, for the upload result page
        assertEquals(50000, saved.getWidth());
        assertEquals(50000, saved.getHeight());
        assertTrue(saved.isThumbnailSkipped());
    }

    /**
     * Replacing an image with one over the limit removes the old image's
     * thumbnail and records the new image's size.
     */
    @Test
    public void testReplacingWithHugeImageRemovesOldThumbnail() throws Exception {
        MediaFile saved = upload("hawk.jpg", "image/jpeg",
                getClass().getResourceAsStream(MediaFileTest.TEST_IMAGE), 2000);
        assertNotNull(saved.getThumbnailInputStream());
        saved.getThumbnailInputStream().close();
        assertFalse(saved.isThumbnailSkipped());

        byte[] png = pngHeaderOnly(50000, 50000);
        MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();
        MediaFile replaced = mgr.getMediaFile(saved.getId());
        replaced.setContentType("image/png");
        replaced.setLength(png.length);
        mgr.updateMediaFile(TestUtils.getManagedWebsite(weblog), replaced,
                new ByteArrayInputStream(png));
        TestUtils.endSession(true);

        MediaFile reloaded = mgr.getMediaFile(saved.getId(), true);
        assertNull(reloaded.getThumbnailInputStream(),
                "the previous image's thumbnail must not be kept");
        assertEquals(50000, reloaded.getWidth());
        assertEquals(50000, reloaded.getHeight());
    }

    /** The pixel limit comes from uploads.thumbnail.maxPixels. */
    @Test
    public void testThumbnailIsSkippedOverConfiguredLimit() throws Exception {
        WebloggerConfig.init();
        MediaFile saved;
        try (MockedStatic<WebloggerConfig> config =
                mockStatic(WebloggerConfig.class, CALLS_REAL_METHODS)) {
            config.when(() -> WebloggerConfig.getIntProperty(
                    eq("uploads.thumbnail.maxPixels"), anyInt())).thenReturn(100000);
            // 500 x 373 is 186500 pixels, over the limit set above
            saved = upload("hawk.jpg", "image/jpeg",
                    getClass().getResourceAsStream(MediaFileTest.TEST_IMAGE), 2000);
        }
        assertNotNull(saved.getInputStream());
        assertNull(saved.getThumbnailInputStream());
        assertEquals(500, saved.getWidth());
        assertEquals(373, saved.getHeight());
    }

    /**
     * An image stored without a thumbnail because it was over the limit is
     * served in full once uploads.thumbnail.maxPixels is raised above its
     * size, like any other image without a thumbnail under the limit.
     */
    @Test
    public void testRaisingLimitAfterUploadServesOriginal() throws Exception {
        WebloggerConfig.init();
        MediaFile saved;
        try (MockedStatic<WebloggerConfig> config =
                mockStatic(WebloggerConfig.class, CALLS_REAL_METHODS)) {
            config.when(() -> WebloggerConfig.getIntProperty(
                    eq("uploads.thumbnail.maxPixels"), anyInt())).thenReturn(100000);
            saved = upload("hawk.jpg", "image/jpeg",
                    getClass().getResourceAsStream(MediaFileTest.TEST_IMAGE), 2000);
        }
        assertNull(saved.getThumbnailInputStream());

        // back at the default limit, which 500 x 373 is well under
        TestUtils.endSession(true);
        MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();
        assertFalse(loadedWithoutContent(saved.getId()).isThumbnailSkipped());
        assertFalse(mgr.getMediaFile(saved.getId(), true).isThumbnailSkipped());
    }

    /**
     * An image under the limit that has no thumbnail file, as for files
     * carried over from older versions or by migrations, is served in full.
     */
    @Test
    public void testSmallImageWithoutThumbnailFileIsNotSkipped() throws Exception {
        MediaFile saved = upload("hawk.jpg", "image/jpeg",
                getClass().getResourceAsStream(MediaFileTest.TEST_IMAGE), 2000);
        WebloggerFactory.getWeblogger().getFileContentManager().deleteFile(
                saved.getWeblog(), saved.getId() + "_sm");
        TestUtils.endSession(true);

        MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();
        MediaFile reloaded = mgr.getMediaFile(saved.getId(), true);
        assertNull(reloaded.getThumbnailInputStream());
        assertEquals(500, reloaded.getWidth());
        assertEquals(373, reloaded.getHeight());
        assertFalse(reloaded.isThumbnailSkipped());
        TestUtils.endSession(true);
        assertFalse(loadedWithoutContent(saved.getId()).isThumbnailSkipped());
    }

    /**
     * An image stored with a thumbnail keeps showing as having one after
     * uploads.thumbnail.maxPixels is lowered below its size.
     */
    @Test
    public void testLoweringLimitAfterUploadKeepsThumbnail() throws Exception {
        MediaFile saved = upload("hawk.jpg", "image/jpeg",
                getClass().getResourceAsStream(MediaFileTest.TEST_IMAGE), 2000);
        try (InputStream in = saved.getThumbnailInputStream()) {
            assertNotNull(in);
        }

        TestUtils.endSession(true);
        WebloggerConfig.init();
        MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();
        try (MockedStatic<WebloggerConfig> config =
                mockStatic(WebloggerConfig.class, CALLS_REAL_METHODS)) {
            config.when(() -> WebloggerConfig.getIntProperty(
                    eq("uploads.thumbnail.maxPixels"), anyInt())).thenReturn(100000);
            assertFalse(loadedWithoutContent(saved.getId()).isThumbnailSkipped());
            assertFalse(mgr.getMediaFile(saved.getId(), true)
                    .isThumbnailSkipped());
        }
    }

    /**
     * Load a media file the way the list views do, without its content, so
     * that whether it has a thumbnail has to be looked up in the file store.
     */
    private static MediaFile loadedWithoutContent(String id) throws Exception {
        MediaFile mediaFile = WebloggerFactory.getWeblogger().getMediaFileManager()
                .getMediaFile(id);
        assertNull(mediaFile.getThumbnailInputStream(),
                "the thumbnail content should not be loaded");
        return mediaFile;
    }

    /**
     * A limit that is not a positive int is ignored in favour of the default.
     */
    @Test
    public void testInvalidLimitFallsBackToDefault() throws Exception {
        WebloggerConfig.init();
        for (String value : new String[] {"50_000_000", "3000000000", "", "0", "-1"}) {
            MediaFile saved;
            try (MockedStatic<WebloggerConfig> config =
                    mockStatic(WebloggerConfig.class, CALLS_REAL_METHODS)) {
                config.when(() -> WebloggerConfig.getProperty(
                        "uploads.thumbnail.maxPixels")).thenReturn(value);

                assertEquals(MediaFileManager.DEFAULT_THUMBNAIL_MAX_PIXELS,
                        MediaFile.thumbnailMaxPixels(), "limit '" + value + "'");

                saved = upload("hawk" + value.length() + ".jpg", "image/jpeg",
                        getClass().getResourceAsStream(MediaFileTest.TEST_IMAGE), 2000);
            }
            try (InputStream in = saved.getThumbnailInputStream()) {
                assertNotNull(in, "limit '" + value + "' should not stop thumbnails");
            }
        }
    }

    /**
     * Replacing an image with content no image reader recognizes forgets the
     * previous image's size.
     */
    @Test
    public void testReplacingWithUnrecognizedContentForgetsOldSize() throws Exception {
        assertOldSizeIsForgotten("unrecognized.png",
                "not an image".getBytes(StandardCharsets.US_ASCII));
    }

    /**
     * Replacing an image with one whose header can't be read forgets the
     * previous image's size.
     */
    @Test
    public void testReplacingWithUnreadableHeaderForgetsOldSize() throws Exception {
        // a PNG signature with a header cut off before the height
        assertOldSizeIsForgotten("truncated.png",
                java.util.Arrays.copyOf(pngHeaderOnly(50000, 50000), 22));
    }

    private void assertOldSizeIsForgotten(String name, byte[] replacement)
            throws Exception {
        byte[] png = pngHeaderOnly(50000, 50000);
        MediaFile saved = upload(name, "image/png",
                new ByteArrayInputStream(png), png.length);
        assertTrue(saved.isThumbnailSkipped());

        MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();
        MediaFile replaced = mgr.getMediaFile(saved.getId());
        replaced.setLength(replacement.length);
        mgr.updateMediaFile(TestUtils.getManagedWebsite(weblog), replaced,
                new ByteArrayInputStream(replacement));
        TestUtils.endSession(true);

        MediaFile reloaded = mgr.getMediaFile(saved.getId(), true);
        assertNull(reloaded.getThumbnailInputStream());
        assertEquals(-1, reloaded.getWidth());
        assertEquals(-1, reloaded.getHeight());
        assertFalse(reloaded.isThumbnailSkipped(),
                "the previous image's size must not count against the new content");
    }

    /**
     * An image so much wider than tall that its thumbnail would be less than
     * a pixel high is stored with its size and without a thumbnail.
     */
    @Test
    public void testVeryWideImageIsStoredWithoutThumbnail() throws Exception {
        byte[] png = encode(new BufferedImage(30000, 10,
                BufferedImage.TYPE_BYTE_GRAY), "png");
        MediaFile saved = upload("banner.png", "image/png",
                new ByteArrayInputStream(png), png.length);

        assertNull(saved.getThumbnailInputStream());
        assertEquals(30000, saved.getWidth());
        assertEquals(10, saved.getHeight());
        assertFalse(saved.isThumbnailSkipped());
    }

    private static byte[] encode(BufferedImage image, String format)
            throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, format, out), "no " + format + " writer");
        return out.toByteArray();
    }

    /** A valid PNG signature and header followed by a token amount of data. */
    private static byte[] pngHeaderOnly(int width, int height) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(out);
        data.write(new byte[] {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'});

        ByteArrayOutputStream ihdr = new ByteArrayOutputStream();
        DataOutputStream header = new DataOutputStream(ihdr);
        header.writeInt(width);
        header.writeInt(height);
        header.writeByte(8);  // bit depth
        header.writeByte(2);  // truecolour
        header.writeByte(0);  // compression
        header.writeByte(0);  // filter
        header.writeByte(0);  // no interlace
        chunk(data, "IHDR", ihdr.toByteArray());

        Deflater deflater = new Deflater();
        deflater.setInput(new byte[1024]);
        deflater.finish();
        byte[] buf = new byte[1024];
        int n = deflater.deflate(buf);
        deflater.end();
        chunk(data, "IDAT", java.util.Arrays.copyOf(buf, n));
        chunk(data, "IEND", new byte[0]);
        return out.toByteArray();
    }

    private static void chunk(DataOutputStream out, String type, byte[] body)
            throws Exception {
        byte[] name = type.getBytes(StandardCharsets.US_ASCII);
        CRC32 crc = new CRC32();
        crc.update(name);
        crc.update(body);
        out.writeInt(body.length);
        out.write(name);
        out.write(body);
        out.writeInt((int) crc.getValue());
    }
}
