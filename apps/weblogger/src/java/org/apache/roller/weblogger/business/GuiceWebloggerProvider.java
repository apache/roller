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

package org.apache.roller.weblogger.business;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.roller.weblogger.config.WebloggerConfig;


/**
 * A Guice specific implementation of a WebloggerProvider.
 */
public class GuiceWebloggerProvider implements WebloggerProvider {
    
    // Guice injector
    protected final Injector injector;
    
    // maintain our own singleton instance of Weblogger
    protected Weblogger webloggerInstance = null;
    
    
    /**
     * Instantiate a new GuiceWebloggerProvider using default guice module 
     * configured in WebloggerConfig via 'guice.backend.module' property.
     */
    public GuiceWebloggerProvider() {
        
        String moduleClassname = WebloggerConfig.getProperty("guice.backend.module");
        if(moduleClassname == null) {
            throw new NullPointerException("unable to lookup default guice module via property 'guice.backend.module'");
        }
        
        try {
            Class moduleClass = Class.forName(moduleClassname);
            Module module = (Module)moduleClass.newInstance();
            injector = Guice.createInjector(module);
        } catch (Throwable e) {                
            // Fatal misconfiguration, cannot recover
            throw new RuntimeException("Error instantiating backend module " + moduleClassname, e);
        }
    }
    
    
    /**
     * Instantiate a new GuiceWebloggerProvider using the given Guice module.
     *
     * @param moduleClassname The full classname of the Guice module to use.
     */
    public GuiceWebloggerProvider(String moduleClassname) {
        
        if(moduleClassname == null) {
            throw new NullPointerException("moduleClassname cannot be null");
        }
        
        try {
            Class moduleClass = Class.forName(moduleClassname);
            Module module = (Module)moduleClass.newInstance();
            injector = Guice.createInjector(module);
        } catch (Throwable e) {                
            // Fatal misconfiguration, cannot recover
            throw new RuntimeException("Error instantiating backend module " + moduleClassname, e);
        }
    }
    
    
    /**
     * @inheritDoc
     */
    public void bootstrap() {
        webloggerInstance =  injector.getInstance(Weblogger.class);
    }
    
    
    /**
     * @inheritDoc
     */
    public Weblogger getWeblogger() {
        return webloggerInstance;
    }
    
}
