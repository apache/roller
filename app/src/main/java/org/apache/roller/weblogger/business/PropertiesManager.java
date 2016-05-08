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
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business;

import java.util.Map;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.util.Blacklist;

/**
 * Manages global runtime properties for Roller (those which may be altered
 * and put into effect while Weblogger is running, as opposed to the static
 * configuration read only when Weblogger restarts).
 */
public interface PropertiesManager {

    /**
     * Initialize the properties manager.
     */
    void initialize();

    /**
     * Save a single property
     */
    void saveProperty(RuntimeConfigProperty property);

    /**
     * Save a list of properties
     */
    void saveProperties(Map properties);

    /**
     * Retrieve a single property by name
     */
    RuntimeConfigProperty getProperty(String name);

    /**
     * Retrieve a list of all properties
     */
    Map<String, RuntimeConfigProperty> getProperties();

    /**
     * Get the list of supported RuntimeConfigDef objects
     */
    RuntimeConfigDefs getRuntimeConfigDefs();

    /**
     * Obtain String value of a property
     * @return String value of property, null if missing or an error
     */
    String getStringProperty(String name);

    /**
     * Obtain boolean value of a property
     * @return boolean value of property, false if a parsing or other error
     */
    boolean getBooleanProperty(String name);

    /**
     * Obtain integer value of a property
     * @return int value of property, -1 if a parsing or other error
     */
    int getIntProperty(String name);

    /**
     * Return true if given weblog handle points to the front-page weblog and
     * that weblog is also configured to have site-wide data available.
     */
    boolean isSiteWideWeblog(String weblogHandle);

    /**
     * Get the Blacklist object for the Weblogger instance (not including any
     * weblog-specific blacklist terms.)
     */
    Blacklist getSiteBlacklist();

}
