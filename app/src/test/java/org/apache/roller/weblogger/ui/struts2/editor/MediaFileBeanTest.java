/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.roller.weblogger.pojos.MediaFile;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MediaFileBeanTest {

    /**
     * The original path decides which resource URL serves a file, so a value
     * posted with the upload form must not reach the stored file.
     */
    @Test
    void copyToLeavesTheOriginalPathAlone() throws Exception {
        MediaFileBean bean = new MediaFileBean();
        bean.setName("page.html");
        bean.setOriginalPath("/page.html");

        MediaFile added = new MediaFile();
        bean.copyTo(added);
        assertEquals("page.html", added.getName());
        assertNull(added.getOriginalPath());

        MediaFile imported = new MediaFile();
        imported.setOriginalPath("/css/site.css");
        bean.copyTo(imported);
        assertEquals("/css/site.css", imported.getOriginalPath());
    }
}
