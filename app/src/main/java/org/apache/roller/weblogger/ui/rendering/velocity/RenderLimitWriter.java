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

/**
 * Passes rendered output through, failing the render once it has written more
 * than the session allows or run past its deadline.
 */
class RenderLimitWriter extends Writer {

    private final Writer out;
    private final long maxChars;
    private long written = 0;

    RenderLimitWriter(Writer out, RenderSession session) {
        this.out = out;
        this.maxChars = session.getMaxOutputChars();
    }

    private void count(long len) {
        written += len;
        if (written > maxChars) {
            throw new RenderLimitException("rendering produced more than "
                    + maxChars + " characters");
        }
        RenderSession.checkDeadline();
    }

    @Override
    public void write(int c) throws IOException {
        count(1);
        out.write(c);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        count(len);
        out.write(cbuf, off, len);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        count(len);
        out.write(str, off, len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
