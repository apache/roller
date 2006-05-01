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
/*
 * PropertyDef.java
 *
 * Created on June 4, 2005, 1:13 PM
 */

package org.apache.roller.config.runtime;

/**
 * Represents the definition of a single runtime property.
 *
 * Each property definition may contain these elements
 *   - name (required)
 *   - key (required)
 *   - type (required)
 *   - default-value (required)
 *   - rows (optional)
 *   - cols (options)
 *
 * @author Allen Gilliland
 */
public class PropertyDef {
    
    private String name = null;
    private String key = null;
    private String type = null;
    private String defaultValue = null;
    private int rows = 5;
    private int cols = 25;
    
    
    /** Creates a new instance of PropertyDef */
    public PropertyDef() {}

    public String toString() {
        return "["+name+","+key+","+type+","+defaultValue+","+rows+","+cols+"]";
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

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
        } catch(Exception e) {
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
        } catch(Exception e) {
            // hmmm ... bogus value
        }
    }
}
