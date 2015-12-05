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
package org.apache.roller.weblogger.business;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.themes.ThemeManager;

/**
 * Helper methods to obtain managers of the Weblogger business tier.
 * Deprecated in favor of greater use of Spring DI
 */
public class Weblogger {

    // managers
    private final IndexManager         indexManager;
    private final MediaFileManager     mediaFileManager;
    private final FileContentManager   fileContentManager;
    private final PingTargetManager    pingTargetManager;
    private final PluginManager        pluginManager;
    private final PropertiesManager    propertiesManager;
    private final ThemeManager         themeManager;
    private final UserManager          userManager;
    private final WeblogManager        weblogManager;
    private final WeblogEntryManager   weblogEntryManager;
    private final PlanetManager        planetManager;

    // url strategy
    private final URLStrategy          urlStrategy;

    /**
     * Single constructor.
     * @throws org.apache.roller.weblogger.WebloggerException on any error
     */
    protected Weblogger(
            IndexManager         indexManager,
            MediaFileManager     mediaFileManager,
            FileContentManager   fileContentManager,
            PingTargetManager    pingTargetManager,
            PluginManager        pluginManager,
            PropertiesManager    propertiesManager,
            ThemeManager         themeManager,
            UserManager          userManager,
            WeblogManager        weblogManager,
            WeblogEntryManager   weblogEntryManager,
            PlanetManager        planetManager,
            URLStrategy          urlStrategy) throws WebloggerException {

        this.indexManager        = indexManager;
        this.mediaFileManager    = mediaFileManager;
        this.fileContentManager  = fileContentManager;
        this.pingTargetManager   = pingTargetManager;
        this.pluginManager       = pluginManager;
        this.propertiesManager   = propertiesManager;
        this.themeManager        = themeManager;
        this.userManager         = userManager;
        this.weblogManager       = weblogManager;
        this.weblogEntryManager  = weblogEntryManager;
        this.urlStrategy         = urlStrategy;
        this.planetManager       = planetManager;
    }

    public IndexManager getIndexManager() {
        return indexManager;
    }

    public ThemeManager getThemeManager() {
        return themeManager;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public MediaFileManager getMediaFileManager() {
        return mediaFileManager;
    }

    public FileContentManager getFileContentManager() {
        return fileContentManager;
    }

    public WeblogEntryManager getWeblogEntryManager() {
        return weblogEntryManager;
    }

    public WeblogManager getWeblogManager() {
        return weblogManager;
    }

    public PropertiesManager getPropertiesManager() {
        return propertiesManager;
    }

    public PingTargetManager getPingTargetManager() {
        return pingTargetManager;
    }

    public PluginManager getPluginManager() {
        return pluginManager;
    }

    public URLStrategy getUrlStrategy() {
        return urlStrategy;
    }

    public PlanetManager getPlanetManager() {
        return planetManager;
    }
}
