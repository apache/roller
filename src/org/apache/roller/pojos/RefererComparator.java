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
package org.apache.roller.pojos;


import java.io.Serializable;
import java.util.Comparator;

/** Compares referers based on day hits and then alphabetical order */
public class RefererComparator implements Comparator, Serializable
{
    static final long serialVersionUID = -1658901752434218888L;
    
    public int compare(Object val1, Object val2)
    throws ClassCastException
    {
        RefererData r1 = (RefererData)val1;
        RefererData r2 = (RefererData)val2;
        int hits1 = r1.getDayHits().intValue();
        int hits2 = r2.getDayHits().intValue();

        if (hits1 > hits2)
        {
            return -1;
        }
        else if (hits1 < hits2)
        {
            return 1;
        }

        // if hits are the same, return
        // results of String.compareTo()
        return r1.getRefererUrl().compareTo(r2.getRefererUrl());
    }
}
