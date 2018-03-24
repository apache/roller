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
package org.apache.roller.selenium.core;

import org.apache.roller.selenium.AbstractRollerPage;
import org.openqa.selenium.WebDriver;

import java.lang.String;

/**
 * represents core/CreateWeblog.jsp
 */
public class CreateWeblogPage extends AbstractRollerPage {

    // additional properties needed: locale, timezone, theme

    public CreateWeblogPage(WebDriver driver) {
        this.driver = driver;
        String pageTitle = "Front Page: Create Weblog";
        verifyPageTitle("createWeblog", pageTitle);
    }

    public MainMenuPage createWeblog() {
        clickById("createWeblog_0");
        return new MainMenuPage(driver);
    }

    public MainMenuPage createWeblog(String name, String handle, String email) {
        setName(name);
        setHandle(handle);
        setEmail(email);
        setLocale("English");
        return createWeblog();
    }

    public void setName(String value) {
        setFieldValue("createWeblog_bean_name", value);
    }

    public void setDescription(String value) {
        setFieldValue("createWeblog_bean_description", value);
    }

    public void setHandle(String value) {
        setFieldValue("createWeblog_bean_handle", value);
    }

    public void setEmail(String value) {
        setFieldValue("createWeblog_bean_emailAddress", value);
    }

    public void setLocale(String value) {
        selectOptionByVisibleText("createWeblog_bean_locale", value);
    }

}
