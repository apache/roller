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

package org.apache.roller.ui.rendering.velocity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.velocity.context.Context;
import org.apache.roller.pojos.CommentData;


/**
 * Interface for comment authentication plugin.
 */
public interface CommentAuthenticator {
    
    
    /**
     * Plugin should write out HTML for the form fields and other UI elements
     * needed inside the Roller comment form. Called when HTML form is being
     * displayed by Velocity template (see comments.vm).
     *
     * @param context Plugin can access objects in context of calling page.
     * @param request Plugin can access request parameters
     * @param response Plugin should write to response
     */
    public String getHtml(
            Context context,
            HttpServletRequest request,
            HttpServletResponse response);
    
    
    /**
     * Plugin should return true only if comment passes authentication test.
     * Called when a comment is posted, not called when a comment is previewed.
     *
     * @param comment Comment data that was posted
     * @param request Plugin can access request parameters
     * @return True if comment passes authentication test
     */
    public boolean authenticate(
            CommentData comment,
            HttpServletRequest request);
    
}
