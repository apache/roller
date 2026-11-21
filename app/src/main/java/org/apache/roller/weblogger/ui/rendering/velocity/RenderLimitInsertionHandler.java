/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.ui.rendering.velocity;

import org.apache.velocity.app.event.ReferenceInsertionEventHandler;
import org.apache.velocity.context.Context;

/**
 * Checks every value a template inserts, including into a string literal such
 * as "$s$s", so a string built up in memory stops at the output limit before
 * it is ever written.
 */
public class RenderLimitInsertionHandler implements ReferenceInsertionEventHandler {

    @Override
    public Object referenceInsert(Context context, String reference, Object value) {
        RenderSession.checkDeadline();
        if (value instanceof CharSequence) {
            RenderSession.checkLength(((CharSequence) value).length());
        }
        return value;
    }
}
