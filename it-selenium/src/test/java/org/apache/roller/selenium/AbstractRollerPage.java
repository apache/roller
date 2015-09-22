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
package org.apache.roller.selenium;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Abstract class holding functionality common to Selenium Page Objects
 * used in Roller.
 */
public abstract class AbstractRollerPage {

    protected WebDriver driver;
    protected String pageName;

    protected void verifyPageTitle(String pageTitle) {
        if(!driver.getTitle().equals(pageTitle)) {
            throw new IllegalStateException("This is not the " + pageName + ", current page is: "
                    + driver.getTitle());
        }
    }

    /*
    * Alternative method of identifying a page, by an HTML ID uniquely on it.
    * Use when multiple views share the same page title.  This method will require
    * adding an id to an element specific to that page if one not already available.
    */
    protected void verifyIdOnPage(String idOnPage) {
        try {
            WebElement div = driver.findElement(By.id(idOnPage));
        } catch (NoSuchElementException e) {
            throw new IllegalStateException("This is not the " + pageName + ", HTML ID: "
                    + idOnPage + " not found.");
        }
    }

    protected void setFieldValue(String fieldId, String value) {
        WebElement field = driver.findElement(By.id(fieldId));
        field.clear();
        field.sendKeys(value);
    }

    protected void clickById(String buttonId) {
        driver.findElement(By.id(buttonId)).click();
    }

    protected void clickByLinkText(String buttonText) {
        driver.findElement(By.linkText(buttonText)).click();
    }

    protected String getTextByCSS(String cssSelector) {
        return driver.findElement(By.cssSelector(cssSelector)).getText();
    }

    protected String getTextById(String fieldId) {
        return driver.findElement(By.id(fieldId)).getText();
    }

    protected void selectOptionByVisibleText(String selectId, String visibleText) {
        Select select = new Select(driver.findElement(By.id(selectId)));
        select.selectByVisibleText(visibleText);
    }
}