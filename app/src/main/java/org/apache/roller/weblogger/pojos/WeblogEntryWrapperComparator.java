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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.util.Comparator;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;

/**
 * Sorts WeblogEntryData objects in reverse chronological order
 * (most recently published entries first).  If they happen to
 * have the same pubTime, then sort alphabetically by title.
 * 
 * @author lance.lavandowska
 */
public class WeblogEntryWrapperComparator implements Comparator<WeblogEntryWrapper>, Serializable {
    
    static final long serialVersionUID = -9067148992322255150L;
    
    @Override
    public int compare(WeblogEntryWrapper entry1, WeblogEntryWrapper entry2) {
        long pubTime1 = entry1.getPubTime().getTime();
        long pubTime2 = entry2.getPubTime().getTime();

        if (pubTime1 > pubTime2) {
            return -1;
        }
        else if (pubTime1 < pubTime2) {
            return 1;
        }

        // if pubTimes are the same, return results of String.compareTo() on Title
        return entry1.getTitle().compareTo(entry2.getTitle());
    }
}
