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

package org.apache.roller.presentation.velocity.plugins.readmore;

import java.util.Map;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogEntryPlugin;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.util.Utilities;


/**
 * DEPRECATED. Truncates entry text and displays a link to read more.
 *
 * This plugin is no longer functional.  It now just returns the orginal
 * text without performing any transformation.
 */
public class ReadMorePlugin implements WeblogEntryPlugin {
    
    private static Log mLogger = LogFactory.getLog(ReadMorePlugin.class);
    
    protected String name = "Read More Summary";
    protected String description = "This plugin is no longer functional.  "+
            "Please use Roller's entry summary field instead.";
    
    private String baseURL = "";
    
    
    public ReadMorePlugin() {
        mLogger.debug("ReadMorePlugin instantiated.");
    }
    
    
    public String getName() {
        return name;
    }
    
    
    public String getDescription() {
        return StringEscapeUtils.escapeJavaScript(description);
    }
    
    
    public void init(WebsiteData website, Map model) throws RollerException {}
    
    
    public String render(WeblogEntryData entry, String str) {
        
        // this plugin has been deprecated now that Roller supports multiple
        // entry text fields (summary & content).  this plugin now just returns
        // the text it is passed in.
        return str;
    }
    
}
