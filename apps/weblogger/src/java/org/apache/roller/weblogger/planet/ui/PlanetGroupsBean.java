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

package org.apache.roller.weblogger.planet.ui;

import org.apache.roller.planet.pojos.PlanetGroup;


/**
 * A simple bean for managing the form data used by the PlanetGroups.
 */
public class PlanetGroupsBean {
    
    private String id = null;
    private String title = null;
    private String handle = null;
    
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }
    
    
    public void copyTo(PlanetGroup dataHolder) {
        
        dataHolder.setTitle(getTitle());
        dataHolder.setHandle(getHandle());
    }
    
    
    public void copyFrom(PlanetGroup dataHolder) {
        
        setId(dataHolder.getId());
        setTitle(dataHolder.getTitle());
        setHandle(dataHolder.getHandle());
    }
    
}
