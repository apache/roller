/*
 *  Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 *  Use is subject to license terms.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you
 *  may not use this file except in compliance with the License. You may
 *  obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or  implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.roller.weblogger.ui.core;

import javax.servlet.ServletContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Initialize the Roller web application/context for container managed
 * authentication.
 *
 * @author Shing Wai Chan
 */
public class CmaRollerContext extends RollerContext { 
    
    private static Log log = LogFactory.getLog(CmaRollerContext.class);
    
    public CmaRollerContext() {
        super();
    }
    
    /**
     * Setup Acegi security features.
     */
    protected void initializeSecurityFeatures(ServletContext context) { 
        // no need to setup Acegi security
    }
}
