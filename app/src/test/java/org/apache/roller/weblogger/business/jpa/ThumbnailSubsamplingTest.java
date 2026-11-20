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
package org.apache.roller.weblogger.business.jpa;

import org.apache.roller.weblogger.pojos.MediaFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Images are decoded for their thumbnail at about twice the thumbnail size
 * in both directions, and images too narrow to scale get no thumbnail.
 */
class ThumbnailSubsamplingTest {

    @Test
    void photoIsDecodedAtAboutTwiceThumbnailSize() {
        // a 6000 x 4000 photo has a 120 x 80 thumbnail
        assertEquals(25, JPAMediaFileManagerImpl.thumbnailSubsampling(6000, 4000, 120, 80));
    }

    @Test
    void smallImageIsDecodedInFull() {
        assertEquals(1, JPAMediaFileManagerImpl.thumbnailSubsampling(100, 50, 100, 50));
        assertEquals(1, JPAMediaFileManagerImpl.thumbnailSubsampling(300, 200, 120, 80));
    }

    @Test
    void longerSideDecidesTheStep() {
        // 12000 x 100 has a 120 x 1 thumbnail; both sides are subsampled
        assertEquals(50, JPAMediaFileManagerImpl.thumbnailSubsampling(12000, 100, 120, 1));
        assertEquals(50, JPAMediaFileManagerImpl.thumbnailSubsampling(100, 12000, 1, 120));
    }

    @Test
    void imageTooNarrowForThumbnailIsNotDecoded() {
        // the thumbnail of a 50,000,000 x 1 image would be 120 x 0
        assertEquals(0, JPAMediaFileManagerImpl.thumbnailSubsampling(50_000_000, 1, 120, 0));
        assertEquals(0, JPAMediaFileManagerImpl.thumbnailSubsampling(1, 50_000_000, 0, 120));
    }

    /** A new size gives a new thumbnail size, as when an image is replaced. */
    @Test
    void thumbnailSizeFollowsNewImageSize() {
        MediaFile mediaFile = new MediaFile();
        mediaFile.setContentType("image/png");
        mediaFile.setWidth(500);
        mediaFile.setHeight(373);
        assertEquals(120, mediaFile.getThumbnailWidth());
        assertEquals(89, mediaFile.getThumbnailHeight());

        mediaFile.setWidth(400);
        mediaFile.setHeight(2000);
        assertEquals(24, mediaFile.getThumbnailWidth());
        assertEquals(120, mediaFile.getThumbnailHeight());
    }
}
