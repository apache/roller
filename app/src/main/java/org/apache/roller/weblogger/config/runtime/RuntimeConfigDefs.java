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
package org.apache.roller.weblogger.config.runtime;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents the collection of all ConfigDefs.
 *
 * @author Allen Gilliland
 */
public class RuntimeConfigDefs {

    private List<DisplayGroup> displayGroups = null;

    public RuntimeConfigDefs() {
        this.displayGroups = new ArrayList<>();
    }

    public RuntimeConfigDefs(List<DisplayGroup> displaygroups) {
        this.displayGroups = displaygroups;
    }

    public boolean addDisplayGroup(DisplayGroup group) {
        return this.displayGroups.add(group);
    }

    public boolean removeDisplayGroup(DisplayGroup group) {
        return this.displayGroups.remove(group);
    }

    public List<DisplayGroup> getDisplayGroups() {
        return displayGroups;
    }

    public void setDisplayGroups(List<DisplayGroup> displayGroups) {
        this.displayGroups = displayGroups;
    }
}
