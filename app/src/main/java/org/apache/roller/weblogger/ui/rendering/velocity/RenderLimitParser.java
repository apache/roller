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
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.Parser;
import org.apache.velocity.runtime.parser.StandardParser;
import org.apache.velocity.runtime.parser.node.ASTAddNode;
import org.apache.velocity.runtime.parser.node.ASTBlock;
import org.apache.velocity.runtime.parser.node.ASTEQNode;
import org.apache.velocity.runtime.parser.node.ASTMulNode;
import org.apache.velocity.runtime.parser.node.ASTNENode;
import org.apache.velocity.runtime.parser.node.ASTprocess;
import org.apache.velocity.runtime.parser.node.Node;
import org.apache.velocity.runtime.parser.node.SimpleNode;

/**
 * Velocity builds some strings in memory rather than in the output: string
 * literals such as "$a$b", the + operator, and #define blocks or macro bodies
 * used as strings. This parser, set as parser.class in velocity.properties,
 * swaps the nodes that do so for ones that stop at the output limit while the
 * string is built:
 *
 * <ul>
 * <li>a template's root node and every block (the body of #if, #foreach,
 * #define, #macro and so on) count what they write into any writer that isn't
 * already counted, such as the buffer of a string literal;</li>
 * <li>+ checks the length of two strings before joining them;</li>
 * <li>== and != check the length of a list or map before comparing it with
 * a string as text;</li>
 * <li>* checks the size of two big numbers before multiplying them.</li>
 * </ul>
 *
 * <p>The strings they build count toward the render's budget (RenderSession).
 *
 * <p>The nodes keep no state of their own between renders, so a parsed
 * template is still safe to share between threads.
 */
public class RenderLimitParser extends StandardParser {

    public RenderLimitParser(RuntimeServices rs) {
        super(rs);
    }

    @Override
    public SimpleNode parse(Reader reader, Template template) throws ParseException {
        SimpleNode root = super.parse(reader, template);
        if (root == null) {
            return null;
        }
        SimpleNode limited = root.getClass() == ASTprocess.class
                ? new LimitedProcess(this, (ASTprocess) root) : root;
        limitChildren(limited);
        return limited;
    }

    private void limitChildren(Node node) {
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            Node child = node.jjtGetChild(i);
            Node limited = child;
            if (child.getClass() == ASTBlock.class) {
                limited = new LimitedBlock(this, (ASTBlock) child);
            } else if (child.getClass() == ASTAddNode.class) {
                limited = new LimitedAddNode(this, (ASTAddNode) child);
            } else if (child.getClass() == ASTEQNode.class) {
                limited = new LimitedEQNode(this, (ASTEQNode) child);
            } else if (child.getClass() == ASTNENode.class) {
                limited = new LimitedNENode(this, (ASTNENode) child);
            } else if (child.getClass() == ASTMulNode.class) {
                limited = new LimitedMulNode(this, (ASTMulNode) child);
            }
            if (limited != child) {
                node.jjtAddChild(limited, i);
                limited.jjtSetParent(node);
            }
            limitChildren(limited);
        }
    }

    /**
     * Moves the children and source position of a parsed node to its
     * replacement. The replacement's constructor copies the fields only a
     * subclass can set: the last token and the template, which the parser no
     * longer has once parsing is over and Velocity needs to say where in a
     * template something went wrong.
     */
    private static void moveChildren(SimpleNode from, SimpleNode to) {
        to.setFirstToken(from.getFirstToken());
        to.setInfo(from.getInfo());
        if (from.isInvalid()) {
            to.setInvalid();
        }
        // last child first, so the child array is allocated once
        for (int i = from.jjtGetNumChildren() - 1; i >= 0; i--) {
            Node child = from.jjtGetChild(i);
            to.jjtAddChild(child, i);
            child.jjtSetParent(to);
        }
    }

    /**
     * @return the writer to render into: the same writer when it already
     *         counts toward the output limit, otherwise one that counts what
     *         this render writes into it
     */
    static Writer limited(Writer writer) {
        if (writer instanceof RenderLimitWriter || writer instanceof CountingWriter
                || RenderSession.current() == null) {
            return writer;
        }
        return new CountingWriter(writer);
    }

    /** Counts what one render writes into a buffer such as a string literal's. */
    static final class CountingWriter extends Writer {

        private final Writer out;
        private long written = 0;

        CountingWriter(Writer out) {
            this.out = out;
        }

        private void count(long len) {
            written += len;
            RenderSession.checkLength(written);
            RenderSession.chargeBuilt(len);
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

    static final class LimitedProcess extends ASTprocess {

        LimitedProcess(Parser parser, ASTprocess from) {
            super(parser, from.getType());
            moveChildren(from, this);
            this.last = from.getLastToken();
            this.template = from.getTemplate();
        }

        @Override
        public boolean render(InternalContextAdapter context, Writer writer) throws IOException {
            return super.render(context, limited(writer));
        }
    }

    static final class LimitedBlock extends ASTBlock {

        // ASTBlock has no getter for the whitespace the parser keeps after it
        private static final Field MORE_POSTFIX;

        static {
            try {
                MORE_POSTFIX = ASTBlock.class.getDeclaredField("morePostfix");
                MORE_POSTFIX.setAccessible(true);
            } catch (NoSuchFieldException ex) {
                throw new IllegalStateException(
                        "this Velocity version's ASTBlock has no morePostfix field", ex);
            }
        }

        LimitedBlock(Parser parser, ASTBlock from) {
            super(parser, from.getType());
            moveChildren(from, this);
            this.last = from.getLastToken();
            this.template = from.getTemplate();
            setPrefix(from.getPrefix());
            setPostfix(from.getPostfix());
            endsWithNewline = from.endsWithNewline;
            try {
                setMorePostfix((String) MORE_POSTFIX.get(from));
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }

        @Override
        public boolean render(InternalContextAdapter context, Writer writer) throws IOException {
            return super.render(context, limited(writer));
        }
    }

    static final class LimitedAddNode extends ASTAddNode {

        LimitedAddNode(Parser parser, ASTAddNode from) {
            super(parser, from.getType());
            moveChildren(from, this);
            this.last = from.getLastToken();
            this.template = from.getTemplate();
        }

        @Override
        protected Object handleSpecial(Object left, Object right, InternalContextAdapter context) {
            if (left instanceof CharSequence || right instanceof CharSequence) {
                RenderSession.checkLength(textLength(left) + textLength(right));
            }
            Object result = super.handleSpecial(left, right, context);
            if (result instanceof CharSequence) {
                RenderSession.checkLength(((CharSequence) result).length());
                RenderSession.chargeBuilt(((CharSequence) result).length());
            }
            return result;
        }

        @Override
        public Number perform(Number left, Number right, InternalContextAdapter context) {
            Number result = super.perform(left, right, context);
            ValueLimits.checkResult(result);
            return result;
        }
    }

    /**
     * @return the length of a string, or of the text of a list or map, that
     *         Velocity is about to build; 0 for other values, whose text is
     *         short or checked once it is built
     */
    private static long textLength(Object value) {
        RenderSession session = RenderSession.current();
        if (session == null) {
            return 0;
        }
        if (value instanceof CharSequence || value instanceof Collection
                || value instanceof Map) {
            return ValueLimits.textLength(value, session.getMaxOutputChars());
        }
        return 0;
    }

    /** Checks the text of a list or map before == compares it with a string. */
    private static void checkComparedText(Object left, Object right) {
        if (!left.getClass().isAssignableFrom(right.getClass())
                && !right.getClass().isAssignableFrom(left.getClass())) {
            RenderSession.checkLength(textLength(left));
            RenderSession.checkLength(textLength(right));
        }
    }

    static final class LimitedEQNode extends ASTEQNode {

        LimitedEQNode(Parser parser, ASTEQNode from) {
            super(parser, from.getType());
            moveChildren(from, this);
            this.last = from.getLastToken();
            this.template = from.getTemplate();
        }

        @Override
        public boolean compareNonNumber(Object left, Object right) {
            checkComparedText(left, right);
            return super.compareNonNumber(left, right);
        }
    }

    static final class LimitedNENode extends ASTNENode {

        LimitedNENode(Parser parser, ASTNENode from) {
            super(parser, from.getType());
            moveChildren(from, this);
            this.last = from.getLastToken();
            this.template = from.getTemplate();
        }

        @Override
        public boolean compareNonNumber(Object left, Object right) {
            checkComparedText(left, right);
            return super.compareNonNumber(left, right);
        }
    }

    static final class LimitedMulNode extends ASTMulNode {

        LimitedMulNode(Parser parser, ASTMulNode from) {
            super(parser, from.getType());
            moveChildren(from, this);
            this.last = from.getLastToken();
            this.template = from.getTemplate();
        }

        @Override
        public Number perform(Number left, Number right, InternalContextAdapter context) {
            ValueLimits.checkBits(ValueLimits.bits(left) + ValueLimits.bits(right));
            return super.perform(left, right, context);
        }
    }
}
