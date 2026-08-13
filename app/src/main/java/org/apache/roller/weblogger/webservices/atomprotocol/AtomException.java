/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.weblogger.webservices.atomprotocol;

import javax.servlet.http.HttpServletResponse;

/**
 * Base exception for the AtomPub implementation. Carries the HTTP status code
 * that the dispatcher servlet should return to the client.
 */
public class AtomException extends Exception {

    private final int status;

    public AtomException(String msg) {
        this(msg, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null);
    }

    public AtomException(String msg, Throwable cause) {
        this(msg, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, cause);
    }

    protected AtomException(String msg, int status, Throwable cause) {
        super(msg, cause);
        this.status = status;
    }

    /** HTTP status code to send to the client for this error. */
    public int getStatus() {
        return status;
    }
}
