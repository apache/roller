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
 * represents core/login.jsp
 * Page Object that handles user login to Roller
 */
public class LoginPage extends AbstractRollerPage {

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        String pageTitle = "Front Page: Welcome to Roller";
        verifyPageTitle("loginForm", pageTitle);
    }

    public MainMenuPage loginToRoller() {
        clickById("login");
        return new MainMenuPage(driver);
    }

    public MainMenuPage loginToRoller(String username, String password) {
        setUsername(username);
        setPassword(password);
        return loginToRoller();
    }

    public void setUsername(String username) {
        setFieldValue("j_username", username);
    }

    public void setPassword(String password) {
        setFieldValue("j_password", password);
    }

}
