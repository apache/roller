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

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.directive.Foreach;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * Velocity's #foreach, checking the render deadline on every pass so a loop
 * that writes nothing still stops. Registered in velocity.properties under
 * runtime.custom_directives, where it replaces the built-in directive.
 */
public class RollerForeach extends Foreach {

    @Override
    protected void renderBlock(InternalContextAdapter context, Writer writer, Node block)
            throws IOException {
        RenderSession.checkDeadline();
        super.renderBlock(context, writer, block);
    }
}
