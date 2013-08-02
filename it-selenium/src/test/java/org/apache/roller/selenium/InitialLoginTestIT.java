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

public class InitialLoginTestIT {
  private WebDriver driver;
  private String baseUrl;
  private boolean acceptNextAlert = true;
  private StringBuffer verificationErrors = new StringBuffer();

  @Before
  public void setUp() throws Exception {
    driver = new FirefoxDriver();
    baseUrl = "http://localhost:8080/roller/";
    driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
  }

  @Test
  public void testInitialLogin() throws Exception {
    driver.get(baseUrl);
    driver.findElement(By.linkText("New User Registration Page")).click();
    driver.findElement(By.id("register_bean_userName")).clear();
    driver.findElement(By.id("register_bean_userName")).sendKeys("bsmith");
    driver.findElement(By.id("register_bean_screenName")).clear();
    driver.findElement(By.id("register_bean_screenName")).sendKeys("bsmith");
    driver.findElement(By.id("register_bean_fullName")).clear();
    driver.findElement(By.id("register_bean_fullName")).sendKeys("Bob Smith");
    driver.findElement(By.id("register_bean_emailAddress")).clear();
    driver.findElement(By.id("register_bean_emailAddress")).sendKeys("bsmith@email.com");
    driver.findElement(By.id("register_bean_passwordText")).clear();
    driver.findElement(By.id("register_bean_passwordText")).sendKeys("roller123");
    driver.findElement(By.id("register_bean_passwordConfirm")).clear();
    driver.findElement(By.id("register_bean_passwordConfirm")).sendKeys("roller123");
    driver.findElement(By.id("submit")).click();
    driver.findElement(By.linkText("Click here")).click();
    driver.findElement(By.id("j_username")).clear();
    driver.findElement(By.id("j_username")).sendKeys("bsmith");
    driver.findElement(By.id("j_password")).clear();
    driver.findElement(By.id("j_password")).sendKeys("roller123");
    driver.findElement(By.id("login")).click();
    driver.findElement(By.linkText("create one?")).click();
    driver.findElement(By.id("createWeblog_bean_name")).clear();
    driver.findElement(By.id("createWeblog_bean_name")).sendKeys("Bob's Blog");
    driver.findElement(By.id("createWeblog_bean_description")).clear();
    driver.findElement(By.id("createWeblog_bean_description")).sendKeys("A blog suitable for Selenium Testing");
    driver.findElement(By.id("createWeblog_bean_handle")).clear();
    driver.findElement(By.id("createWeblog_bean_handle")).sendKeys("bobsblog");
    driver.findElement(By.id("createWeblog_0")).click();
    driver.findElement(By.linkText("New Entry")).click();
    driver.findElement(By.id("entry_bean_title")).clear();
    driver.findElement(By.id("entry_bean_title")).sendKeys("My First Blog Entry");
    driver.findElement(By.id("entry_bean_text")).clear();
    driver.findElement(By.id("entry_bean_text")).sendKeys("<p>Hello!  I'm looking forward to blogging lots of entries!</p>");
    driver.findElement(By.id("entry_2")).click();
    driver.findElement(By.linkText("http://localhost:8080/roller/bobsblog/entry/my_first_blog_entry")).click();
  }

  @After
  public void tearDown() throws Exception {
    driver.quit();
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
