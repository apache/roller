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

package org.apache.roller.weblogger.business.startup;

import java.util.Collections;
import java.util.List;
import org.apache.roller.weblogger.WebloggerException;


/**
 * Exception generated from Weblogger startup process.
 */
public class StartupException extends WebloggerException {
    
    private final List<String> startupLog;
    
    
    public StartupException(String msg) {
        super(msg);
        this.startupLog = Collections.emptyList();
    }
    
    public StartupException(String msg, Throwable t) {
        super(msg, t);
        this.startupLog = Collections.emptyList();
    }
    
    public StartupException(String msg, List<String> log) {
        super(msg);
        
        if(log != null) {
            this.startupLog = log;
        } else {
            this.startupLog = Collections.emptyList();
        }
    }
    
    public StartupException(String msg, Throwable t, List<String> log) {
        super(msg, t);
        
        if(log != null) {
            this.startupLog = log;
        } else {
            this.startupLog = Collections.emptyList();
        }
    }
    
    
    public List<String> getStartupLog() {
        return startupLog;
    }
    
}
