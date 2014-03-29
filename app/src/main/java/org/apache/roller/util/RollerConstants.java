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
package org.apache.roller.util;

public final class RollerConstants {

    public static final int PERCENT_100 = 100;

    public static final int ONE_KB_IN_BYTES = 1024;
    public static final int FOUR_KB_IN_BYTES = 4096;
    public static final int EIGHT_KB_IN_BYTES = 8192;
    public static final int TWENTYFOUR_KB_IN_BYTES = 24576;

    public static final int ONE_MB_IN_BYTES = ONE_KB_IN_BYTES * ONE_KB_IN_BYTES;

    public static final int HALF_SEC_IN_MS = 500;
    public static final int SEC_IN_MS = 1000;
    public static final int MIN_IN_MS = 60 * SEC_IN_MS;

    private RollerConstants() {
        // never instantiable
        throw new AssertionError();
    }
}
