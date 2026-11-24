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
import java.util.Iterator;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.directive.Foreach;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * Velocity's #foreach, checking the render deadline on every pass so a loop
 * that writes nothing still stops, and failing the render on a loop of more
 * than MAX_PASSES passes. Velocity's own directive.foreach.max_loops ends the
 * loop without an error, so a page cut short that way would be served and
 * cached as if it were whole. Registered in velocity.properties under
 * runtime.custom_directives, where it replaces the built-in directive.
 */
public class RollerForeach extends Foreach {

    /** Most passes one #foreach may make. */
    static final int MAX_PASSES = 100_000;

    @Override
    protected Iterator getIterator(Object iterable, Node node) {
        Iterator<?> items = super.getIterator(iterable, node);
        return items == null || RenderSession.current() == null ? items : new Limited(items);
    }

    @Override
    protected void renderBlock(InternalContextAdapter context, Writer writer, Node block)
            throws IOException {
        RenderSession.checkDeadline();
        super.renderBlock(context, writer, block);
    }

    /** The items of one loop, failing the render on the one after the last pass allowed. */
    private static final class Limited implements Iterator<Object> {

        private final Iterator<?> items;
        private int passes = 0;

        Limited(Iterator<?> items) {
            this.items = items;
        }

        @Override
        public boolean hasNext() {
            return items.hasNext();
        }

        @Override
        public Object next() {
            if (++passes > MAX_PASSES) {
                throw new RenderLimitException("#foreach made more than "
                        + MAX_PASSES + " passes");
            }
            return items.next();
        }
    }
}
