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
package org.apache.roller.weblogger.business.themes;

import java.util.List;

import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Manager interface for accessing Theme related objects.
 */
public interface ThemeManager {

    /**
     * Initialize the theme system.
     */
    void initialize();

    /**
     * Get the SharedTheme object with the given id.
     *
     * @return Theme The SharedTheme object with the given id.
     * @throws IllegalArgumentException If the named theme cannot be found.
     **/
    SharedTheme getSharedTheme(String id);

    /**
     * Get the WeblogTheme for a given weblog.
     *
     * @param weblog The weblog to get the theme for.
     * @return WeblogTheme The theme to be used for the given weblog
     */
    WeblogTheme getWeblogTheme(Weblog weblog);

    /**
     * Get a list of all shared themes that are currently enabled. This list is
     * ordered alphabetically by default.
     *
     * @return List A list of SharedTheme objects which are enabled.
     */
    List<SharedTheme> getEnabledSharedThemesList();

    /**
     * Create a weblog template (database-stored, weblog-specific)
     * from a shared (file) template, including the latter's renditions
     *
     * @param weblog
     *            The weblog to import the template into
     * @param sharedTemplate
     *            The sharedTemplate that should copied from
     * @return WeblogTemplate instance, not persisted to the database.
     *            (Caller is expected to do persistence if and when desired.)
     */
    WeblogTemplate createWeblogTemplate(Weblog weblog, Template sharedTemplate);
}
