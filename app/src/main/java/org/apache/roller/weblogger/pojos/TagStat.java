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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
*/
package org.apache.roller.weblogger.pojos;


import java.util.Comparator;

/**
 * Tag bean.
 * 
 * @author Elias Torres
 * 
 */
public class TagStat implements java.io.Serializable {

    private static final long serialVersionUID = 1142064841813545198L;

    private String name;

    private int count;
    
    private int intensity;

    public TagStat() {
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String toString() {
        StringBuilder str = new StringBuilder("{");

        str.append("name=" + name + " " + "count=" + count);
        str.append('}');

        return (str.toString());
    }

    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    public static Comparator<TagStat> Comparator = new Comparator<TagStat>() {
        public int compare(TagStat ts1, TagStat ts2) {
            return ts1.getName().compareToIgnoreCase(ts2.getName());
        }
    };

    public static Comparator<TagStat> CountComparator = new Comparator<TagStat>() {
        public int compare(TagStat st1, TagStat st2) {
            // higher numbers first for counts
            int compVal = Integer.valueOf(st2.getCount()).compareTo(st1.getCount());

            // still alpha order if tied
            if (compVal == 0) {
                compVal = st1.getName().compareTo(st2.getName());
            }
            return compVal;
        }
    };
}
