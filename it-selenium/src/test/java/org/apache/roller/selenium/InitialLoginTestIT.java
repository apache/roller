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
package org.apache.roller.selenium;

import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;
import org.junit.*;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.Select;
import org.apache.roller.selenium.core.CreateWeblogPage;
import org.apache.roller.selenium.core.LoginPage;
import org.apache.roller.selenium.core.MainMenuPage;
import org.apache.roller.selenium.core.RegisterPage;
import org.apache.roller.selenium.core.SetupPage;
import org.apache.roller.selenium.core.WelcomePage;
import org.apache.roller.selenium.editor.EntryAddPage;
import org.apache.roller.selenium.editor.EntryEditPage;
import org.apache.roller.selenium.view.BlogHomePage;
import org.apache.roller.selenium.view.SingleBlogEntryPage;
import org.openqa.selenium.firefox.FirefoxProfile;

public class InitialLoginTestIT {
    private WebDriver driver;
    private String baseUrl;
    private boolean acceptNextAlert = true;
    private StringBuffer verificationErrors = new StringBuffer();

    @Before
    public void setUp() throws Exception {
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("intl.accept_languages", "en_US");
        driver = new FirefoxDriver(profile);
        baseUrl = "http://localhost:8080/roller/";
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    @Test
    public void testInitialLogin() throws Exception {
        // create new user and first blog
        driver.get(baseUrl);
        SetupPage sp = new SetupPage(driver);
        RegisterPage rp = sp.createNewUser();
        WelcomePage wp = rp.submitUserRegistration("bsmith", "Bob Smith", "bsmith@email.com", "roller123");
        LoginPage lp = wp.doRollerLogin();
        MainMenuPage mmp = lp.loginToRoller("bsmith", "roller123");
        CreateWeblogPage cwp = mmp.createWeblog();
        cwp.createWeblog("Bob's Blog", "bobsblog", "bsmith@email.com");

        // set bobsblog as the front page blog
        driver.get(baseUrl);
        sp = new SetupPage(driver);
        driver.navigate().refresh();
        BlogHomePage bhp = sp.chooseFrontPageBlog();

        // create and read first blog entry
        String blogEntryTitle = "My First Blog Entry";
        String blogEntryContent = "Welcome to my blog!";
        EntryAddPage eap = bhp.createNewBlogEntry();
        eap.setTitle(blogEntryTitle);
        eap.setText(blogEntryContent);
        EntryEditPage eep = eap.postBlogEntry();
        SingleBlogEntryPage sbep = eep.viewBlogEntry();
        System.out.println("title/text: " + sbep.getBlogTitle() + " / " + sbep.getBlogText());
        assertEquals(blogEntryTitle, sbep.getBlogTitle());
        assertEquals(blogEntryContent, sbep.getBlogText());
    }


    @After
    public void tearDown() throws Exception {
        String verificationErrorString = verificationErrors.toString();
        if (!"".equals(verificationErrorString)) {
            fail(verificationErrorString);
        }
    }

    private boolean isElementPresent(By by) {
        try {
            driver.findElement(by);
            return true;
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    private boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException e) {
            return false;
        }
    }

    private String closeAlertAndGetItsText() {
        try {
            Alert alert = driver.switchTo().alert();
            String alertText = alert.getText();
            if (acceptNextAlert) {
                alert.accept();
            } else {
                alert.dismiss();
            }
            return alertText;
        } finally {
            acceptNextAlert = true;
        }
    }
}
