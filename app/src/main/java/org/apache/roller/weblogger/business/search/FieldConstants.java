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
/* Created on Jul 19, 2003 */
package org.apache.roller.weblogger.business.search;

import org.apache.lucene.util.Version;

/**
 * Field constants for indexing blog entries and comments.
 * 
 * @author Mindaugas Idzelis (min@idzelis.com)
 */
public final class FieldConstants {

    // Set what version we are on
    public static final Version LUCENE_VERSION = Version.LUCENE_43;

    public static final String ANCHOR = "anchor";
    public static final String UPDATED = "updated";
    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String CATEGORY = "cat";
    public static final String TITLE = "title";
    public static final String PUBLISHED = "published";
    public static final String CONTENT = "content";
    public static final String CONTENT_STORED = "content_stored";
    public static final String C_CONTENT = "comment";
    public static final String C_EMAIL = "email";
    public static final String C_NAME = "name";
    public static final String CONSTANT = "constant";
    public static final String CONSTANT_V = "v"; // must be lowercase, or match
                                                 // the transform rules of
                                                 // the analyzer
    public static final String WEBSITE_HANDLE = "handle";
}
