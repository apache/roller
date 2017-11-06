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
package org.apache.roller.selenium.view;

import org.apache.roller.selenium.AbstractRollerPage;
import org.openqa.selenium.WebDriver;

import java.lang.String;

/**
 * represents a URL that displays a single blog entry
 * (URL similar to http://localhost:8080/roller/myblog/entry/my_blog_entry)
 */
public class SingleBlogEntryPage extends AbstractRollerPage {

    public SingleBlogEntryPage(WebDriver driver) {
        this.driver = driver;
        String pageTitle = "Single blog entry view";
        /* id_permalink added to basic template's permalink.vm only to distinguish
           this page from by-month or by-day views of blog entries */
        verifyIdOnPage("id_permalink");
    }

    public String getBlogTitle() {
        return getTextByCSS("p.entryTitle");
    }

    public String getBlogText() {
        return getTextByCSS("p.entryContent");
    }

}
