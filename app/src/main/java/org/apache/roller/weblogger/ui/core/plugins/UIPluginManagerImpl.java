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

package org.apache.roller.weblogger.ui.core.plugins;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.util.Reflection;


/**
 * Plugin management for UI layer plugins.
 */
public final class UIPluginManagerImpl implements UIPluginManager {
    
    private static final Log log = LogFactory.getLog(UIPluginManagerImpl.class);
    
    // singleton instance
    private static final UIPluginManagerImpl instance;
    
    // list of configured WeblogEntryEditor classes
    private final Map<String, WeblogEntryEditor> editors = new LinkedHashMap<>();
    
    // the default WeblogEntryEditor
    WeblogEntryEditor defaultEditor = null;
    
    
    static {
        instance = new UIPluginManagerImpl();
    }
    
    
    // private to enforce singleton pattern
    private UIPluginManagerImpl() {
        loadEntryEditorClasses();
    }
    
    
    // static singleton accessor
    public static UIPluginManager getInstance() {
        return instance;
    }
    
    
    @Override
    public List<WeblogEntryEditor> getWeblogEntryEditors() {
        // TODO: sort list of returned editors
        return new ArrayList<>(this.editors.values());
    }
    
    
    @Override
    public WeblogEntryEditor getWeblogEntryEditor(String id) {
        
        WeblogEntryEditor editor;
        
        // see if this editor is configured
        editor = (id == null) ? null : this.editors.get(id);
        if(editor == null) {
            editor = this.defaultEditor;
        }
        
        return editor;
    }
    
    
    /**
     * Initialize the set of configured editors and define the default editor.
     */
    private void loadEntryEditorClasses() {
        
        log.debug("Initializing entry editor plugins");
        
        try {
            Reflection.<WeblogEntryEditor>newInstancesFromProperty("plugins.weblogEntryEditors")
                    .forEach(editor -> this.editors.put(editor.getId(), editor));
        } catch(ClassCastException cce) {
            log.error("It appears that your editor does not implement "+
                    "the WeblogEntryEditor interface", cce);
        } catch(Exception e) {
            log.error("Unable to instantiate editors", e);
        }
        
        if(this.editors.isEmpty()) {
            log.warn("No entry editors configured, this means that publishing "+
                    "entries will be impossible.");
            return;
        }
        
        // make sure the default editor is defined
        String defaultEditorId = WebloggerConfig.getProperty("plugins.defaultEditor");
        if(defaultEditorId != null) {
            this.defaultEditor = this.editors.get(defaultEditorId);
        }
        
        if(this.defaultEditor == null) {
            // someone didn't configure the default editor properly
            // guess we'll just have to pick one for them
            log.warn("Default editor was not properly configured, picking one at random instead.");
            
            this.defaultEditor = this.editors.values().iterator().next();
        }
    }
    
}
