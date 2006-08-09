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

package org.apache.roller.util;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * A utilities class for interacting with the Roller resource bundles.
 */
public class MessageUtilities {
    
    private static Log log = LogFactory.getLog(MessageUtilities.class);
    
    private static final ResourceBundle bundle =
            ResourceBundle.getBundle("ApplicationResources");
    
    
    // no instantiation
    private MessageUtilities() {
    }
    
    
    /**
     * Get a message from the bundle.
     */
    public static final String getString(String key) {
        
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            // send a warning in the logs
            log.warn("Error getting key "+key, e);
            return key;
        }
    }
    
    
    /**
     * Get a message from the bundle and substitute the given args into
     * the message contents.
     */
    public static final String getString(String key, List args) {
        
        try {
            String msg = bundle.getString(key);
            return MessageFormat.format(msg, args.toArray());
        } catch (Exception e) {
            // send a warning in the logs
            log.warn("Error getting key "+key, e);
            return key;
        }
    }
    
    
    /**
     * Get a message from the bundle and substitute the given args into
     * the message contents.
     */
    public static final String getString(String key, Object[] args) {
        
        try {
            String msg = bundle.getString(key);
            return MessageFormat.format(msg, args);
        } catch (Exception e) {
            // send a warning in the logs
            log.warn("Error getting key "+key, e);
            return key;
        }
    }
    
}
