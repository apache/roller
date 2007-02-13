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

package org.apache.roller.planet.ui.rendering.pagers;

import java.util.List;

/**
 * Common pager interface.
 */
public interface Pager {
    /**
     * Link value for returning to pager home
     */
    public String getHomeLink();

    /**
     * Name of pager home.
     */
    public String getHomeName();

    /**
     * Link value for next page in current collection view
     */
    public String getNextLink();

    /**
     * Name for next page in current collection view
     */
    public String getNextName();

    /**
     * Link value for prev page in current collection view
     */
    public String getPrevLink();

    /**
     * Link value for prev page in current collection view
     */
    public String getPrevName();
    
    /**
     * Get current list of items available from the pager.
     */
    public List getItems();
    
}
