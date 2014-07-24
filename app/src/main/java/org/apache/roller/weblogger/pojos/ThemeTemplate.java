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


/**
 * A Theme specific implementation of a Template.
 * 
 * A ThemeTemplate represents a template which is part of a Theme.
 */
public interface ThemeTemplate extends Template {

    public enum ComponentType {WEBLOG, PERMALINK, SEARCH, TAGSINDEX, STYLESHEET, CUSTOM}

    /**
     * The action this template is defined for.
     */
    ComponentType getAction();
    
    
    /**
     * The url link value for this Template.  If this template is not
     * private this is the url that it can be accessed at.
     */
    String getLink();
    
    
    /**
     * Is the Template hidden?  A hidden template cannot be accessed directly.
     */
    boolean isHidden();
    
    
    /**
     * Is the Template to be included in the navbar?
     */
    boolean isNavbar();

}
