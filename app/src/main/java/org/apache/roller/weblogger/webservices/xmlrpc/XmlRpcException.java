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

package org.apache.roller.weblogger.webservices.xmlrpc;

/**
 * An XML-RPC fault: the client receives {@link #code} as <code>faultCode</code>
 * and the message as <code>faultString</code>.
 */
public class XmlRpcException extends Exception {
    private static final long serialVersionUID = 1L;

    /** The fault code sent to the client. */
    public final int code;

    public XmlRpcException(int code, String message) {
        super(message);
        this.code = code;
    }

    /** A fault with code 0. */
    public XmlRpcException(String message) {
        this(0, message);
    }

    public int getCode() {
        return code;
    }
}
