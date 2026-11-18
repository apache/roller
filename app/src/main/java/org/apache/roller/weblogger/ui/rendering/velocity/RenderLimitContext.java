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

import java.util.Map;

import org.apache.velocity.VelocityContext;

/**
 * The context a weblog template renders against. Every variable read and
 * write, and every macro call, checks the render deadline, so a template that
 * only sets variables or calls macros, without output or method calls, still
 * stops in time; a string stored in a variable may be no longer than the
 * output limit.
 */
class RenderLimitContext extends VelocityContext {

    RenderLimitContext(Map<String, Object> model) {
        super(model);
    }

    @Override
    public Object internalGet(String key) {
        RenderSession.checkDeadline();
        return super.internalGet(key);
    }

    @Override
    public Object internalPut(String key, Object value) {
        RenderSession.checkDeadline();
        if (value instanceof CharSequence) {
            RenderSession.checkLength(((CharSequence) value).length());
        }
        return super.internalPut(key, value);
    }

    @Override
    public void pushCurrentMacroName(String name) {
        RenderSession.checkDeadline();
        super.pushCurrentMacroName(name);
    }
}
