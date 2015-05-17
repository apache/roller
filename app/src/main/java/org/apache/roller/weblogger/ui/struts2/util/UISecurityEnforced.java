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
 * are under same ASF license.
 */

package org.apache.roller.weblogger.ui.struts2.util;

import java.util.List;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.WeblogRole;

/**
 * Implemented by struts2 actions which want to enforce some level of security
 * protection on their action.  Actions are of two types: GlobalRoles (system-wide
 * permissions such as server settings and user management) and WeblogRoles (attached
 * to each blog for which a single user has a specified role.)
 */
public interface UISecurityEnforced {
    
    /**
     * Minimum global role required for the action
     */
    WeblogRole requiredWeblogRole();

    /** 
     * Minimum global role required for the action
     */
    GlobalRole requiredGlobalRole();
}
