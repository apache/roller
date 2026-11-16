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

package org.apache.roller.weblogger.ui.rendering;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * Interface for object that can handle requests, return true if handled.
 */
public interface RequestMapper {
    
    /**
     * Handle an incoming request.
     *
     * RequestMappers are not required to handle all requests and are instead
     * encouraged to inspect the request and only take action when it
     * wants to.  If action is taken then the RequestMapper should return a 
     * boolean "true" value indicating that no further action is required.
     */
    boolean handleRequest(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException;
    
}
