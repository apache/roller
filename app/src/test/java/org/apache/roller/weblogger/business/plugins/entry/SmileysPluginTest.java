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

package org.apache.roller.weblogger.business.plugins.entry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class SmileysPluginTest {

    private final SmileysPlugin plugin = new SmileysPlugin();

    @Test
    public void emoticonBecomesEmoji() {
        assertEquals("Hi <span class=\"smiley\" title=\":-)\">&#x1F642;</span> there",
                plugin.render(null, "Hi :-) there"));
    }

    @Test
    public void longerEmoticonWinsOverItsPrefix() {
        // ":))" laughs; it is not ":)" followed by ")"
        assertEquals("<span class=\"smiley\" title=\":))\">&#x1F606;</span>",
                plugin.render(null, ":))"));
        assertEquals("<span class=\"smiley\" title=\";;)\">&#x1F60F;</span>",
                plugin.render(null, ";;)"));
    }

    @Test
    public void titleIsEscapedAndNotMatchedAgain() {
        String html = plugin.render(null, ":-& and :\">");
        assertEquals("<span class=\"smiley\" title=\":-&amp;\">&#x1F922;</span> and "
                + "<span class=\"smiley\" title=\":&quot;&gt;\">&#x1F60A;</span>", html);
    }

    @Test
    public void textWithoutEmoticonsIsUnchanged() {
        String text = "<p>No faces here.</p>";
        assertEquals(text, plugin.render(null, text));
        assertFalse(plugin.render(null, ":-) ;-) :-(").contains("<img"));
    }
}
