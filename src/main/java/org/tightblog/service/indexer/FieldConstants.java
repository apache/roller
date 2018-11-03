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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.service.indexer;

/**
 * Field constants for indexing blog entries and comments.
 */
public final class FieldConstants {

    private FieldConstants() {
    }

    public static final String UPDATED = "updated";
    public static final String ID = "id";
    public static final String USERNAME = "username";
    public static final String CATEGORY = "cat";
    public static final String TITLE = "title";
    public static final String PUBLISHED = "published";
    public static final String CONTENT = "content";
    public static final String COMMENT_CONTENT = "comment";
    public static final String COMMENT_EMAIL = "email";
    public static final String COMMENT_NAME = "name";
    public static final String CONSTANT = "constant";
    public static final String WEBLOG_HANDLE = "handle";
    // CONSTANT_V used to retrieve all documents; must be lowercase
    public static final String CONSTANT_V = "v";
}
