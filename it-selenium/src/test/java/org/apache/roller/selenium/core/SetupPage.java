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

/**
 * represents core/Setup.jsp
 * Page Object encapsulates the Setup.jsp index page
 * that appears on initial startup of Roller, after
 * the database tables were created but before the
 * first user account and blog made.
 */
public class SetupPage extends AbstractRollerPage {

    public SetupPage(WebDriver driver) {
        this.driver = driver;
        this.pageName = "Initial Setup Page";
        verifyPageTitle("Front Page: Welcome to Roller!");
    }

    public RegisterPage createNewUser() {
        clickByLinkText("New User Registration Page");
        return new RegisterPage(driver);
    }

}