/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.weblogger.ui.struts2.pagers;

import java.util.List;

import org.apache.roller.weblogger.pojos.MediaFile;

/**
 * Pager for the media library. The library can sort by name or type as well
 * as by date, so its buttons say Previous and Next rather than Newer and Older.
 */
public class MediaFilePager extends LinkPager<MediaFile> {

    public MediaFilePager(String url, int page, List<MediaFile> mediaFiles, long totalCount, int pageSize) {
        super(url, page, mediaFiles, totalCount, pageSize);
    }

    @Override
    public String getPrevKey() {
        return "generic.previous";
    }

    @Override
    public String getNextKey() {
        return "generic.next";
    }
}
