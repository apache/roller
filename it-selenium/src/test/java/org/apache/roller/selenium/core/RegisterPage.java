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
 * represents core/Register.jsp
 * index page that does step #1 of setup.jsp, the adding of a new user
 */
public class RegisterPage extends AbstractRollerPage {

    public RegisterPage(WebDriver driver) {
        this.driver = driver;
        this.pageName = "New User Registration Page";
        verifyPageTitle("Front Page: New User Registration");
    }

    public WelcomePage submitUserRegistration() {
        clickById("submit");
        return new WelcomePage(driver);
    }

    public WelcomePage submitUserRegistration(String userAndScreenName, String email,
                                       String password) {
        setUserName(userAndScreenName);
        setScreenName(userAndScreenName);
        setEmail(email);
        setPassword(password);
        setPasswordConfirm(password);
        return submitUserRegistration();
    }

    public void setUserName(String value) {
        setFieldValue("register_bean_userName", value);
    }

    public void setScreenName(String value) {
        setFieldValue("register_bean_screenName", value);
    }

    public void setEmail(String value) {
        setFieldValue("register_bean_emailAddress", value);
    }

    public void setPassword(String value) {
        setFieldValue("register_bean_passwordText", value);
    }

    public void setPasswordConfirm(String value) {
        setFieldValue("register_bean_passwordConfirm", value);
    }
}
