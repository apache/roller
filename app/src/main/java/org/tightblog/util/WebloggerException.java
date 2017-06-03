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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.util;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Weblogger base exception class.
 */
public class WebloggerException extends Exception {

    public WebloggerException() {
        super();
    }

    /**
     * Construct WebloggerException with message string.
     *
     * @param message Error message string.
     */
    public WebloggerException(String message) {
        super(message);
    }

    /**
     * Construct WebloggerException, wrapping existing throwable.
     *
     * @param message   Error message
     * @param throwable Existing connection to wrap.
     */
    public WebloggerException(String message, Throwable throwable) {
        super(message, throwable);
    }

    /**
     * Construct WebloggerException, wrapping existing throwable.
     *
     * @param throwable Existing exception to be wrapped.
     */
    public WebloggerException(Throwable throwable) {
        super(throwable);
    }

    /**
     * Get root cause object, or null if none.
     *
     * @return Root cause or null if none.
     */
    public Throwable getRootCause() {
        return getCause();
    }

    /**
     * Get root cause message.
     *
     * @return Root cause message.
     */
    public String getErrorMessageChain() {
        String rcmessage = "Top level: " + getMessage();
        if (getCause() != null) {
            rcmessage += "; 2nd level: " + getCause().getMessage();
            if (getCause().getCause() != null) {
                rcmessage += "; 3rd level: " + getCause().getCause().getMessage();
            }
        }
        return rcmessage;
    }

    /**
     * Print stack trace for exception and for root cause exception if there is one.
     *
     * @see java.lang.Throwable#printStackTrace()
     */
    public void printStackTrace() {
        super.printStackTrace();
        if (getCause() != null) {
            System.out.println("--- ROOT CAUSE ---");
            getCause().printStackTrace();
        }
    }

    /**
     * Print stack trace for exception and for root cause exception if there is one.
     *
     * @param s Stream to print to.
     */
    public void printStackTrace(PrintStream s) {
        super.printStackTrace(s);
        if (getCause() != null) {
            s.println("--- ROOT CAUSE ---");
            getCause().printStackTrace(s);
        }
    }

    /**
     * Print stack trace for exception and for root cause exception if there is one.
     *
     * @param s Writer to write to.
     */
    public void printStackTrace(PrintWriter s) {
        super.printStackTrace(s);
        if (getCause() != null) {
            s.println("--- ROOT CAUSE ---");
            getCause().printStackTrace(s);
        }
    }

}
