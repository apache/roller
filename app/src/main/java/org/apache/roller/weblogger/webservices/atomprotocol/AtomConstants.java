/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.weblogger.webservices.atomprotocol;

/**
 * Constants shared by the StAX-based AtomPub implementation.
 */
public final class AtomConstants {

    private AtomConstants() {
    }

    /** Atom Syndication Format namespace (RFC 4287). */
    public static final String ATOM_NS = "http://www.w3.org/2005/Atom";

    /** Atom Publishing Protocol namespace (RFC 5023). */
    public static final String APP_NS = "http://www.w3.org/2007/app";

    /** Media type for an Atom entry. */
    public static final String ENTRY_MEDIA_TYPE = "application/atom+xml;type=entry";

    /** Media type for an Atom feed/collection. */
    public static final String FEED_MEDIA_TYPE = "application/atom+xml;type=feed;charset=utf-8";

    /** Media type for an APP service document. */
    public static final String SERVICE_MEDIA_TYPE = "application/atomsvc+xml;charset=utf-8";
}
