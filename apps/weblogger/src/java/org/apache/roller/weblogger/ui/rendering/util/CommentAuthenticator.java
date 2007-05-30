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

package org.apache.roller.weblogger.ui.rendering.util;

import javax.servlet.http.HttpServletRequest;


/**
 * Interface for comment authentication plugin.
 */
public interface CommentAuthenticator {
    
    
    /**
     * Plugin should write out HTML for the form fields and other UI elements
     * needed to display the comment authentication widget.
     *
     * @param request comment form request object
     */
    public String getHtml(HttpServletRequest request);
    
    
    /**
     * Plugin should return true only if comment posting passes the 
     * authentication test.
     *
     * @param request comment posting request object
     * @return true if authentication passed, false otherwise
     */
    public boolean authenticate(HttpServletRequest request);
    
}
