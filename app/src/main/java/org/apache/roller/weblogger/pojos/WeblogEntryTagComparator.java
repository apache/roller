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

package org.apache.roller.weblogger.pojos;

import java.util.Comparator;
import java.io.Serializable;

/**
 * Sorts tags by name
 */
public class WeblogEntryTagComparator implements Comparator<WeblogEntryTag>,
        Serializable {

    private static final long serialVersionUID = 3720914385178339406L;
   
    /**
     * Instantiates a new weblog entry tag comparator.
     */
    public WeblogEntryTagComparator() {
    }

    /**
     * Compares two <em>WeblogEntryTag</em> instances according to their tag
     * name.
     */
    public int compare(WeblogEntryTag o1, WeblogEntryTag o2) {
        return o1.getName().toString().compareTo(o2.getName().toString());
    }

}
