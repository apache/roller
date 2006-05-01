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
/*
 * Created on May 29, 2003
 */
package org.roller.presentation.velocity.plugins.jspwiki;

import com.ecyrd.jspwiki.providers.FileSystemProvider;

/**
 * Override page provider so that Wiki links always appear as hyperlinks
 * to the external Wiki's Wiki.jsp?page=MyPageName page. Without this, they
 * would appear as links tp Edit.jsp?page=MyPageName, which is not quite as
 * nice. Eventually, it may be a good idea to use JSPWiki's XML-RPC interface
 * to figure out if the page exists or not.
 * @author David M Johnson
 */
public class RollerPageProvider extends FileSystemProvider
{
    public boolean pageExists(String arg0)
    {
        return true;
    }
}
