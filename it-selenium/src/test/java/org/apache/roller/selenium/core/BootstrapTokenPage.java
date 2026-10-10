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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.roller.selenium.AbstractRollerPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

/** The operator-only token step before first-user registration. */
public class BootstrapTokenPage extends AbstractRollerPage {

    private static final Pattern TOKEN = Pattern.compile(
            "Enter this one-time setup token[^\\r\\n]*\\R\\| ([A-Za-z0-9_-]{43}) +\\|");

    public BootstrapTokenPage(WebDriver driver) {
        this.driver = driver;
        verifyPageTitle("Roller initial setup");
    }

    public SetupPage unlock(Path serverLog) {
        // Read the same server-side log as an operator; never print its token.
        // Logging is asynchronous, so allow time for the startup message to flush.
        String token = new WebDriverWait(driver, Duration.ofSeconds(10))
                .withMessage("No initial setup token found in " + serverLog)
                .until(ignored -> readToken(serverLog));
        setFieldValue("setup-token", token);
        clickById("setup-token-submit");
        new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                ExpectedConditions.titleIs("Front Page: Welcome to Roller!"));
        return new SetupPage(driver);
    }

    private static String readToken(Path serverLog) {
        try {
            return Files.exists(serverLog) ? latestToken(Files.readString(serverLog)) : null;
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot read Roller server log: " + serverLog, ex);
        }
    }

    static String latestToken(String log) {
        Matcher matcher = TOKEN.matcher(log);
        String token = null;
        while (matcher.find()) {
            token = matcher.group(1);
        }
        return token;
    }
}
