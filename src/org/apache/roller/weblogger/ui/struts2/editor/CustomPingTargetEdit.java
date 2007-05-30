/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.common.PingTargetEditBase;


/**
 * Action for modifying a weblog custom ping target.
 */
public class CustomPingTargetEdit extends PingTargetEditBase {
    
    private static Log log = LogFactory.getLog(CustomPingTargetEdit.class);
    
    
    public CustomPingTargetEdit() {
        this.actionName = "customPingTargetEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "pingTarget.pingTarget";
    }
    
    
    // admin perms required
    public short requiredWeblogPermissions() {
        return WeblogPermission.ADMIN;
    }
    
    
    protected Log getLogger() {
        return log;
    }
    
}
