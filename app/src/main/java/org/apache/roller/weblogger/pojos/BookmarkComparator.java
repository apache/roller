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

import java.io.Serializable;
import java.util.Comparator;

public class BookmarkComparator implements Comparator, Serializable
{
    static final long serialVersionUID = 4009699640952161148L;
    
    public int compare(Object val1, Object val2)
    throws ClassCastException
    {
        WeblogBookmark bd1 = (WeblogBookmark)val1;
        WeblogBookmark bd2 = (WeblogBookmark)val2;
        int priority1 = bd1.getPriority();
        int priority2 = bd2.getPriority();

        if (priority1 > priority2)
        {
            return 1;
        }
        else if (priority1 < priority2)
        {
            return -1;
        }

        // if priorities are the same, return
        // results of String.compareTo()
        return bd1.getName().compareTo(bd2.getName());

    }

}
