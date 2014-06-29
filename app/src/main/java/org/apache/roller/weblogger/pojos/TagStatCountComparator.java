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
 * @author Markus Fuchs
 */
public class TagStatCountComparator implements Comparator<TagStat>, Serializable {

    private static final long serialVersionUID = 1155112837815739929L;

    private static TagStatCountComparator instance = new TagStatCountComparator();
    
    /**
     * 
     */
    public TagStatCountComparator() {

    }

    /** 
     * Compares two <em>TagStat</em> instances according to their count values.
     * 
     * @throws ClassCastException if arguments are not instances of <em>TagStat</em>
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(TagStat st1, TagStat st2) {
        int compVal = st1.getCount() < st2.getCount() ? -1 :
                (st1.getCount() == st2.getCount() ? 0 : 1);
        
        if (compVal == 0) {
            compVal = st1.getName().compareTo(st2.getName());
        }
        return compVal;
    }

    public static TagStatCountComparator getInstance() {
        return instance;
    }
}
