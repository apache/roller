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

package org.apache.roller.ui.rendering.model;

import java.util.List;
import java.util.Map;
import org.apache.roller.RollerException;
import org.apache.roller.util.MessageUtilities;


/**
 * Provides access to application resources required for I18N.
 * Uses model name 'text' because that's what the Velocity Tools did.
 */
public class MessageModel implements Model {  
    
    
    /** Template context name to be used for model */
    public String getModelName() {
        return "text";
    }
    
    
    /** Init page model based on request */
    public void init(Map initData) throws RollerException {
        // no-op
    }
    
    
    /** Return message string */
    public String get(String key) {
        return MessageUtilities.getString(key);
    }
    
    
    /** Return parameterized message string */
    public String get(String key, List args) {
        return MessageUtilities.getString(key, args);
    }

}
