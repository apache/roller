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
 * Created on Mar 4, 2004
 */
package org.apache.roller.weblogger.ui;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.ui.core.RollerContext;

/**
 * @author lance.lavandowska
 */
public class MockRollerContext extends RollerContext {
    private static Log mLogger =
            LogFactory.getFactory().getInstance(MockRollerContext.class);
    
    private static ServletContext mContext = null;
    
    public void init(ServletContext sc) {
        mLogger.debug("MockRollerContext initializing");
        
        // initialize super
        super.contextInitialized(new ServletContextEvent(sc));
        
        // Save context in self and self in context
        mContext = sc;
        mContext.setAttribute("org.apache.roller.absoluteContextURL", "/");
    }
    
    //-----------------------------------------------------------------------
    /** Because I cannot set the super's values, I have to
     * overide the methods as well */
    public static ServletContext getServletContext() {
        return mContext;
    }
        
    //-----------------------------------------------------------------------
    /** Because I cannot set the super's values, I have to
     * overide the methods as well */
    public String getAbsoluteContextUrl() {
        return "";
    }
    
    //-----------------------------------------------------------------------
    /** Because I cannot set the super's values, I have to
     * overide the methods as well */
    public String getAbsoluteContextUrl(HttpServletRequest request) {
        return "http://localhost:8080/roller";
    }
    
}

