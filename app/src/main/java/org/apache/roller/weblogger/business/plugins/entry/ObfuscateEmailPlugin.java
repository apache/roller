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

package org.apache.roller.weblogger.business.plugins.entry;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.util.RegexUtil;


/**
 * Obfuscate email addresses in entry text.
 */
public class ObfuscateEmailPlugin implements WeblogEntryPlugin {
    
    private static final Log mLogger = LogFactory.getLog(ObfuscateEmailPlugin.class);
    
    protected String name = "Email Scrambler";
    
    protected String description = "Automatically converts email addresses " +
            "to me-AT-mail-DOT-com format.  Also &quot;scrambles&quot; mailto: links.";
    
    
    public ObfuscateEmailPlugin() {
        mLogger.debug("ObfuscateEmailPlugin instantiated.");
    }
    
    
    @Override
    public String getName() {
        return name;
    }
    
    
    @Override
    public String getDescription() {
        return StringEscapeUtils.escapeEcmaScript(description);
    }
    
    
    @Override
    public void init(Weblog website) throws WebloggerException {}
    
    
    @Override
    public String render(WeblogEntry entry, String str) {
        return RegexUtil.encodeEmail(str);
    }
    
}
