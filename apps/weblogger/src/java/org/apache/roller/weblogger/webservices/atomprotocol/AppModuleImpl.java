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
package org.apache.roller.weblogger.webservices.atomprotocol;

import com.sun.syndication.feed.module.ModuleImpl;
import java.util.Date;

public class AppModuleImpl extends ModuleImpl implements AppModule {
    private boolean draft = false;
    private Date edited = null;
    
    public AppModuleImpl() {
        super(AppModule.class, AppModule.URI);
    }
    
    public boolean getDraft() {
        return draft;
    }
    
    public void setDraft(boolean draft) {
        this.draft = draft;
    }
    
    public Date getEdited() {
        return edited;
    }

    public void setEdited(Date edited) {
        this.edited = edited;
    }
    
    public Class getInterface() {
        return AppModule.class;
    }
    
    public void copyFrom(Object obj) {
        AppModule m = (AppModule)obj;
        setDraft(m.getDraft());
    }
}
