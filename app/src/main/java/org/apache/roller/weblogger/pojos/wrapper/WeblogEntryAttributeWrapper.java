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

package org.apache.roller.weblogger.pojos.wrapper;

import org.apache.roller.weblogger.pojos.WeblogEntryAttribute;


/**
 * Pojo safety wrapper for WeblogEntryAttribute object.
 */
public final class WeblogEntryAttributeWrapper {
    
    // keep a reference to the wrapped pojo
    private final WeblogEntryAttribute pojo;
    
    // this is private so that we can force the use of the .wrap(pojo) method
    private WeblogEntryAttributeWrapper(WeblogEntryAttribute toWrap) {
        this.pojo = toWrap;
    }
    
    
    // wrap the given pojo if it is not null
    public static WeblogEntryAttributeWrapper wrap(WeblogEntryAttribute toWrap) {
        if (toWrap != null) {
            return new WeblogEntryAttributeWrapper(toWrap);
        }
        return null;
    }
    
    public String getName() {
        return this.pojo.getName();
    }
    
    
    public String getValue() {
        return this.pojo.getValue();
    }
    
}
