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

package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;


/**
 * Bean for managing folder data.
 */
public class FolderBean {
    
    protected String id = null;
    protected String name = null;
    protected String description = null;
    
    
    public String getId() {
        return this.id;
    }
    
    public void setId( String id ) {
        this.id = id;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription( String description ) {
        this.description = description;
    }
    
    
    public void copyTo(WeblogBookmarkFolder dataHolder) throws WebloggerException {
        
        if(!dataHolder.getName().equals(this.name)) {
            dataHolder.updateName(this.name);
        }
        
        dataHolder.setDescription(this.description);
    }
    
    
    public void copyFrom(WeblogBookmarkFolder dataHolder) {
        this.id = dataHolder.getId();
        this.name = dataHolder.getName();
        this.description = dataHolder.getDescription();
    }
    
}
