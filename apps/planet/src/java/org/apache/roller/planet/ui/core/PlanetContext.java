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

package org.apache.roller.planet.ui.core;

import java.io.InputStream;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.springframework.web.context.ContextLoaderListener;


/**
 * Responds to app init/destroy events and holds Roller instance.
 */
public class PlanetContext extends ContextLoaderListener 
        implements ServletContextListener {
    
    private static Log log = LogFactory.getLog(PlanetContext.class);
    
    // reference to ServletContext object
    private static ServletContext context = null;
    
    
    public PlanetContext() {
        super();
    }
    
    
    /**
     * Get the ServletContext.
     *
     * @return ServletContext
     */
    public static ServletContext getServletContext() {
        return context;
    }
    

    /**
     * Responds to context initialization event by processing context
     * paramters for easy access by the rest of the application.
     */
    public void contextInitialized(ServletContextEvent sce) {
        
        log.debug("Roller Planet Initializing");
        
        // keep a reverence to ServletContext object
        this.context = sce.getServletContext();
        
        // call Spring's context ContextLoaderListener to initialize
        // all the context files specified in web.xml. This is necessary
        // because listeners don't initialize in the order specified in
        // 2.3 containers
        super.contextInitialized(sce);
        
        try {
            // always upgrade database first
            upgradeDatabaseIfNeeded();
            
            Planet planet = PlanetFactory.getPlanet();
            
            setupRuntimeProperties();
            
            planet.flush();
            planet.release();
            
        } catch (Throwable t) {
            log.fatal("Roller Planet initialization failed", t);
            throw new RuntimeException(t);
        }
        
        log.debug("Initialization Complete");
    }
    
    
    /** 
     * Responds to app-destroy. 
     */
    public void contextDestroyed(ServletContextEvent sce) {
        PlanetFactory.getPlanet().shutdown();
    }
    
    
    private void setupRuntimeProperties() {
        // init property manager by loading it
        PlanetFactory.getPlanet().getPropertiesManager();
    }
    
    
    private void upgradeDatabaseIfNeeded() throws RollerException {
        
//        try {
//            InitialContext ic = new InitialContext();
//            DataSource ds = (DataSource)ic.lookup("java:comp/env/jdbc/rollerdb");
//            Connection con = ds.getConnection();
//            UpgradeDatabase.upgradeDatabase(con, RollerFactory.getRoller().getVersion());
//            con.close();
//        } catch (NamingException e) {
//            log.warn("Unable to access DataSource", e);
//        } catch (SQLException e) {
//            log.warn(e);
//        }
    }

}
