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

package org.apache.roller.weblogger.business.plugins;

import junit.framework.TestCase;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;


/**
 * Test comment plugins.
 */
public class CommentPluginsTest extends TestCase {

    private String convertLinesStart = "paragraph1\n\nparagraph2\nline2\nline3\n\nparagraph3";
    private String convertLinesFormatted = "\n<p>paragraph1</p>\n\n\n<p>paragraph2<br/>\nline2<br/>\nline3</p>\n\n\n<p>paragraph3</p>\n\n";
    
    
    protected void setUp() throws Exception {
        TestUtils.setupWeblogger();
    }
    
    protected void tearDown() throws Exception {
        // no-op
    }
    
    
    public void testAutoFormatPlugin() {
        
        PluginManager pmgr = WebloggerFactory.getWeblogger().getPagePluginManager();
        
        // setup test comment
        WeblogEntryComment comment = new WeblogEntryComment();
        comment.setContent(convertLinesStart); 
        comment.setPlugins("AutoFormat Plugin");
        
        // reformat
        String output = pmgr.applyCommentPlugins(comment);
        
        // make sure it turned out how we planned
        assertEquals(convertLinesFormatted, output);        
    }
    
}
