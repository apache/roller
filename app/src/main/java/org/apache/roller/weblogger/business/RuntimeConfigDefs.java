/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.weblogger.business;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name="runtime-configs")
public class RuntimeConfigDefs {

    private List<DisplayGroup> displayGroups = null;

    public RuntimeConfigDefs() {
        this.displayGroups = new ArrayList<>();
    }

    public List<DisplayGroup> getDisplayGroups() {
        return displayGroups;
    }

    @XmlElement(name="display-group")
    public void setDisplayGroups(List<DisplayGroup> displayGroups) {
        this.displayGroups = displayGroups;
    }

    public static class DisplayGroup {

        private List<PropertyDef> propertyDefs = null;
        private String name = null;
        private String key = null;

        public DisplayGroup() {
            this.propertyDefs = new ArrayList<>();
        }

        public String getName() {
            return name;
        }

        @XmlAttribute
        public void setName(String name) {
            this.name = name;
        }

        public String getKey() {
            return key;
        }

        @XmlAttribute
        public void setKey(String key) {
            this.key = key;
        }

        public List<PropertyDef> getPropertyDefs() {
            return propertyDefs;
        }

        @XmlElement(name = "property-def")
        public void setPropertyDefs(List<PropertyDef> propertyDefs) {
            this.propertyDefs = propertyDefs;
        }

        public String toString() {
            return name + "," + key;
        }
    }

    public static class PropertyDef {
        private String name = null;
        private String key = null;
        private String type = null;
        private String defaultValue = null;
        private int rows = 5;
        private int cols = 25;

        /**
         * Creates a new instance of PropertyDef
         */
        public PropertyDef() {
        }

        public String getName() {
            return name;
        }

        @XmlAttribute
        public void setName(String name) {
            this.name = name;
        }

        public String getKey() {
            return key;
        }

        @XmlAttribute
        public void setKey(String key) {
            this.key = key;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        @XmlElement(name = "default-value")
        public void setDefaultValue(String defaultvalue) {
            this.defaultValue = defaultvalue;
        }

        public int getRows() {
            return rows;
        }

        public void setRows(int rows) {
            this.rows = rows;
        }

        public void setRows(String rows) {
            //convert to int
            try {
                int r = Integer.parseInt(rows);
                this.rows = r;
            } catch (Exception e) {
                // hmmm ... bogus value
            }
        }

        public int getCols() {
            return cols;
        }

        public void setCols(int cols) {
            this.cols = cols;
        }

        public void setCols(String cols) {
            //convert to int
            try {
                int c = Integer.parseInt(cols);
                this.cols = c;
            } catch (Exception e) {
                // hmmm ... bogus value
            }
        }

        public String toString() {
            return "[" + name + "," + key + "," + type + "," + defaultValue + "," + rows + "," + cols + "]";
        }
    }

    public enum RegistrationOption {
        EMAIL("Register and activate via email (OK for LDAP but not recommended for DB auth)"),
        APPROVAL_REQUIRED("Register, activate via email, and admin approval"),
        DISABLED("Disabled -- No new accounts allowed");

        private String description;

        RegistrationOption(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
