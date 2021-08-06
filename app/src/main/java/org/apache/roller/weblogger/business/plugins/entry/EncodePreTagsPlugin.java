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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;


import static java.util.regex.Pattern.*;

/**
 * Encodes angle brackets inside pre tags (code tags which follow right after pre are not encoded).
 */
public class EncodePreTagsPlugin implements WeblogEntryPlugin {
    
    private static final String LT = "&lt;"; // '<'

    private static final Pattern PRE_PATTERN = Pattern.compile(
            "<pre\\s*[^>]*>" + "(.*?)" + "</pre\\s*>", MULTILINE | DOTALL | CASE_INSENSITIVE);
    
    private static final Pattern CODE_PATTERN = Pattern.compile(
            "<code\\s*.[^>]*>" + "(.*?)" + "</code\\s*>", MULTILINE | DOTALL | CASE_INSENSITIVE);
    
    @Override
    public String getName() {
        return "Pre Tag Encoder";
    }

    @Override
    public String getDescription() {
        return "Encodes angle brackets inside pre tags, code tags are kept unaltered.";
    }

    @Override
    public void init(Weblog weblog) throws WebloggerException {}

    @Override
    public String render(WeblogEntry entry, String str) {
        
        StringBuilder result = new StringBuilder(str.length()+32);
        
        Matcher pre_matcher = PRE_PATTERN.matcher(str);
        
        while(pre_matcher.find()) {
            
            String pre_full = pre_matcher.group(0);
            String pre_inner = pre_matcher.group(1);
            
            Matcher code_matcher = CODE_PATTERN.matcher(pre_inner);
            
            if (code_matcher.find()) {
                String code_inner = code_matcher.group(1);
                pre_matcher.appendReplacement(result, pre_full.replace(code_inner, encode(code_inner)));
            } else {
                pre_matcher.appendReplacement(result, pre_full.replace(pre_inner, encode(pre_inner)));
            }
            
        }
        pre_matcher.appendTail(result);
        
        return result.toString();
    }

    // we only have to encode the opening angle bracket for valid html/xhtml
    private static String encode(String code_inner) {
        return Matcher.quoteReplacement(code_inner.replace("<", LT)); // matchers hate $ and \
    }

}
