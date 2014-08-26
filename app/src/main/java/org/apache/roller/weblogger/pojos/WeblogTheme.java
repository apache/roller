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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;


/**
 * A Theme which is specifically tied to a given weblog.
 *
 * A WeblogTheme is what is used throughout the rendering process to do the
 * rendering for a given weblog design.
 */
public abstract class WeblogTheme implements Theme, Serializable {
    
    // this is the name that will be used to identify a user customized theme
    public static final String CUSTOM = "custom";

    protected Weblog weblog = null;
    
    
    public WeblogTheme(Weblog weblog) {
        this.weblog = weblog;
    }
    
    
    public Weblog getWeblog() {
        return this.weblog;
    }
    
}
