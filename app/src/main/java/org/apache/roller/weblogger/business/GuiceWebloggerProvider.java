/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.business;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.Objects;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.util.Reflection;


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
        this(Objects.requireNonNull(
                WebloggerConfig.getProperty("guice.backend.module"),
                "unable to lookup default guice module via property 'guice.backend.module'"));
    }
    
    
    /**
     * Instantiate a new GuiceWebloggerProvider using the given Guice module.
     *
     * @param moduleClassname The full classname of the Guice module to use.
     */
    public GuiceWebloggerProvider(String moduleClassname) {
        
        Objects.requireNonNull(moduleClassname, "moduleClassname cannot be null");
        
        try {
            Module module = (Module) Reflection.newInstance(moduleClassname);
            injector = Guice.createInjector(module);
        } catch (ThreadDeath t) {
            throw t;
        } catch (Throwable e) {
            // Fatal misconfiguration, cannot recover
            throw new RuntimeException("Error instantiating backend module " + moduleClassname + "; exception message: " + e.getMessage(), e);
        }
    }
    
    
    /**
     * @inheritDoc
     */
    @Override
    public void bootstrap() {
        webloggerInstance =  injector.getInstance(Weblogger.class);
    }
    
    
    /**
     * @inheritDoc
     */
    @Override
    public Weblogger getWeblogger() {
        return webloggerInstance;
    }
    
}
