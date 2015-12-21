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
package org.apache.roller.weblogger.business.plugins;

import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;

import static org.junit.Assert.*;


/**
 * Test comment plugins.
 */
public class CommentPluginsTest extends WebloggerTest {

    @Resource
    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager manager) {
        this.weblogEntryManager = manager;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testAutoFormatPlugin() {
        String convertLinesStart = "paragraph1\n\nparagraph2\nline2\nline3\n\nparagraph3";
        String convertLinesFormatted = "\n<p>paragraph1</p>\n\n\n<p>paragraph2<br/>\nline2<br/>\nline3</p>\n\n\n<p>paragraph3</p>\n\n";

        // setup test comment
        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setContent(convertLinesStart); 
        comment.setPlugins("AutoFormat Plugin");
        
        // reformat
        String output = weblogEntryManager.applyCommentPlugins(comment, comment.getContent());
        
        // make sure it turned out how we planned
        assertEquals(convertLinesFormatted, output);        
    }
    
}
