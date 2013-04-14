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

package org.apache.roller.planet.business.fetcher;

import java.io.PrintStream;
import java.io.PrintWriter;
import org.apache.roller.RollerException;


/**
 * Exception generated from FeedFetcher.
 */
public class FetcherException extends RollerException {
    
    public FetcherException(String msg) {
        super(msg);
    }
    
    public FetcherException(String msg, Throwable t) {
        super(msg, t);
    }
    
    
    /**
     * Print stack trace for exception and for root cause exception if htere is one.
     * @see java.lang.Throwable#printStackTrace()
     */
    @Override
    public void printStackTrace() {
        // just print our message since we know this exception should be wrapping
        // a more detailed exception from whatever fetching solution is used
        System.out.println(super.getMessage());
        if (getRootCause() != null) {
            System.out.println("--- ROOT CAUSE ---");
            getRootCause().printStackTrace();
        }
    }
    
    
    /**
     * Print stack trace for exception and for root cause exception if htere is one.
     * @param s Stream to print to.
     */
    @Override
    public void printStackTrace(PrintStream s) {
        // just print our message since we know this exception should be wrapping
        // a more detailed exception from whatever fetching solution is used
        s.println(super.getMessage());
        if (getRootCause() != null) {
            s.println("--- ROOT CAUSE ---");
            getRootCause().printStackTrace(s);
        }
    }
    
    
    /**
     * Print stack trace for exception and for root cause exception if htere is one.
     * @param s Writer to write to.
     */
    @Override
    public void printStackTrace(PrintWriter s) {
        // just print our message since we know this exception should be wrapping
        // a more detailed exception from whatever fetching solution is used
        s.println(super.getMessage());
        if (getRootCause() != null) {
            s.println("--- ROOT CAUSE ---");
            getRootCause().printStackTrace(s);
        }
    }
    
}
