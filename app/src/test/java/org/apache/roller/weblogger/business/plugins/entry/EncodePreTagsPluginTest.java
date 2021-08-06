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

package org.apache.roller.weblogger.business.plugins.entry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author mbien
 */
public class EncodePreTagsPluginTest {
    
    @Test
    public void passthrough() {

        EncodePreTagsPlugin instance = new EncodePreTagsPlugin();
        
        String input = "Stay a while and listen.";
        assertEquals(input, instance.render(null, input));
        
        input = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>Hi!</title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <p>Hello There</p>\n" +
                "  </body>\n" +
                "</html>";
        assertEquals(input, instance.render(null, input));
        
        input = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>Hi!</title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "    <pre>Hello There</pre>\n" + // pre
                "    <code>Hello There</code>\n" + // code
                "  </body>\n" +
                "</html>";
        assertEquals(input, instance.render(null, input));
        
    }

    @Test
    public void substitution1() {

        EncodePreTagsPlugin instance = new EncodePreTagsPlugin();
        
        String input = "<pre><></pre>";
        String expected = "<pre>&lt;></pre>";
        
        assertEquals(expected, instance.render(null, input));
        
    }
    
    @Test
    public void substitution2() {

        EncodePreTagsPlugin instance = new EncodePreTagsPlugin();
        
        String input = "\n<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>Hi!</title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "      <pre><code class='language-java'>private final Map<String, List<?>> map = new HashMap<>();</code></pre>\n"+
                "      <pre><code class='language-bash'>$JDK/bin/java -version</code></pre>\n"+
                "      <pre><code class='language-slash'>\\\\</code></pre>\n"+
                "  </body>\n" +
                "</html>";
        
        String expected = "\n<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>Hi!</title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "      <pre><code class='language-java'>private final Map&lt;String, List&lt;?>> map = new HashMap&lt;>();</code></pre>\n"+
                "      <pre><code class='language-bash'>$JDK/bin/java -version</code></pre>\n"+
                "      <pre><code class='language-slash'>\\\\</code></pre>\n"+
                "  </body>\n" +
                "</html>";
        assertEquals(expected, instance.render(null, input));
        
    }
    
    @Test
    public void substitution3() {

        EncodePreTagsPlugin instance = new EncodePreTagsPlugin();
        
        String input = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>Hi!</title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "      <h3>some java</h3>\n" +
                "      <pre>\n" +
                "          <code class='language-java'>\n"+
                "              private final Map<String, List<?>> map = new HashMap<>();\n"+
                "          </code>\n" +
                "      </pre>\n" +
                "      <h3>some xml</h3>\n" +
                "      <pre>\n" +
                "          <code class='language-xml'>\n"+
                "              <foo id = '5'>\n"+
                "                  <bar>asdf</bar>\n"+
                "              </foo>\n"+
                "          </code>\n" +
                "      </pre>\n" +
                "  </body>\n" +
                "</html>";
        
        String expected = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>Hi!</title>\n" +
                "  </head>\n" +
                "  <body>\n" +
                "      <h3>some java</h3>\n" +
                "      <pre>\n" +
                "          <code class='language-java'>\n"+
                "              private final Map&lt;String, List&lt;?>> map = new HashMap&lt;>();\n"+
                "          </code>\n" +
                "      </pre>\n" +
                "      <h3>some xml</h3>\n" +
                "      <pre>\n" +
                "          <code class='language-xml'>\n"+
                "              &lt;foo id = '5'>\n"+
                "                  &lt;bar>asdf&lt;/bar>\n"+
                "              &lt;/foo>\n"+
                "          </code>\n" +
                "      </pre>\n" +
                "  </body>\n" +
                "</html>";
        assertEquals(expected, instance.render(null, input));
        
    }
    
}
