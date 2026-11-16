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
/*
 * RuntimeConfigDefs.java
 *
 * Created on June 4, 2005, 1:06 PM
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
    
    private List<ConfigDef> configDefs = null;
    
    
    public RuntimeConfigDefs() {
        this.configDefs = new ArrayList<>();
    }

    public RuntimeConfigDefs(List<ConfigDef> configs) {
        this.configDefs = configs;
    }
    
    
    public boolean addConfigDef(ConfigDef config) {
        return this.configDefs.add(config);
    }
    
    public boolean removeConfigDef(ConfigDef config) {
        return this.configDefs.remove(config);
    }
    
    
    public List<ConfigDef> getConfigDefs() {
        return configDefs;
    }

    public void setConfigDefs(List<ConfigDef> configDefs) {
        this.configDefs = configDefs;
    }
    
}
