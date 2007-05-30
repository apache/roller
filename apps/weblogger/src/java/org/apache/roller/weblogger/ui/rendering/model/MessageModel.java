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

package org.apache.roller.weblogger.ui.rendering.model;

import java.util.List;
import java.util.Map;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.ui.rendering.util.WeblogRequest;
import org.apache.roller.weblogger.util.I18nMessages;


/**
 * Provides access to application resources required for I18N.
 * Uses model name 'text' because that's what the Velocity Tools did.
 */
public class MessageModel implements Model {  
    
    I18nMessages messages = null;
    
    
    /** Template context name to be used for model */
    public String getModelName() {
        return "text";
    }
    
    
    /** Init page model based on request */
    public void init(Map initData) throws RollerException {
        
        // we expect the init data to contain a weblogRequest object
        WeblogRequest weblogRequest = (WeblogRequest) initData.get("weblogRequest");
        if(weblogRequest == null) {
            throw new RollerException("expected weblogRequest from init data");
        }
        
        // get messages util based on desired locale
        this.messages = I18nMessages.getMessages(weblogRequest.getLocaleInstance());
    }
    
    
    /** Return message string */
    public String get(String key) {
        return messages.getString(key);
    }
    
    
    /** Return parameterized message string */
    public String get(String key, List args) {
        return messages.getString(key, args);
    }

}
