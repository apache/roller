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
package org.apache.roller.weblogger.webservices.atomprotocol;

import jakarta.servlet.http.HttpServletResponse;

/**
 * Thrown when an authenticated user is not permitted to perform the requested
 * operation (HTTP 401, matching the previous ROME Propono behavior).
 */
public class AtomNotAuthorizedException extends AtomException {

    public AtomNotAuthorizedException(String msg) {
        super(msg, HttpServletResponse.SC_UNAUTHORIZED, null);
    }
}
