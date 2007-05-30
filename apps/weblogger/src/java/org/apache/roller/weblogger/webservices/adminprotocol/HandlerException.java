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
package org.apache.roller.weblogger.webservices.adminprotocol;

/**
 * Abstract base class for all handler exceptions.
 *
 * Subclasses of this class allow handler implementations to indicate to
 * callers a particular HTTP error type, while still providing
 * a textual description of the problem.
 * 
 * Callers may use the 
 * <code>getStatus()</code> method to discover the HTTP status
 * code that should be returned to the client.
 */
public abstract class HandlerException extends Exception { 
    public HandlerException(String msg) {
        super(msg);
    }    

    public HandlerException(String msg, Throwable t) {
        super(msg);
    }    
    
    public abstract int getStatus();
}
