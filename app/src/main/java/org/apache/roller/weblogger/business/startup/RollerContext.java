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
package org.apache.roller.weblogger.business.startup;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.springframework.web.context.ContextLoaderListener;

/**
 * Initialize the Roller web application/context.
 */
public class RollerContext extends ContextLoaderListener {

    private static ServletContext servletContext = null;

    /**
     * Get the ServletContext.
     * @return ServletContext
     */
    public static ServletContext getServletContext() {
        return servletContext;
    }
    
    /**
     * Responds to app-init event and triggers startup procedures.
     */
    public void contextInitialized(ServletContextEvent sce) {

        // Keep a reference to ServletContext object
        servletContext = sce.getServletContext();

        // Prior to initializing the beans defined in the various Spring XML files
        // via the super() call below, need to update one "static" value in tightblog.properties
        // setting the themes directory to the absolute file path of the webapp context, something
        // known only at runtime.  (Some beans need this value.)  The setThemesDir() call below
        // will ignore this call if themes.dir has been overridden to something other than ${webapp.context}.
        WebloggerStaticConfig.setThemesDir(servletContext.getRealPath("/") + "blogthemes");

        super.contextInitialized(sce);
    }
}
