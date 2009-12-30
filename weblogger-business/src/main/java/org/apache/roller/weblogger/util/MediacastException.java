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

package org.apache.roller.weblogger.util;

import org.apache.roller.weblogger.WebloggerException;


/**
 * An exception thrown when dealing with Mediacast files.
 */
public class MediacastException extends WebloggerException {
    
    private int errorCode = 0;
    private String errorKey = null;
    
    
    public MediacastException(int code, String msgKey) {
        this.errorCode = code;
        this.errorKey = msgKey;
    }
    
    
    public MediacastException(int code, String msgKey, Throwable t) {
        super(t);
        this.errorCode = code;
        this.errorKey = msgKey;
    }
    
    
    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorKey() {
        return errorKey;
    }
    
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setErrorKey(String errorKey) {
        this.errorKey = errorKey;
    }
    
}
