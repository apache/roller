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
/* Created on Jul 20, 2003 */
package org.apache.roller.weblogger.business.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.index.Term;
import org.apache.roller.weblogger.business.search.IndexManagerImpl;

import java.io.IOException;
import java.io.StringReader;

/**
 * Class containing helper methods.
 * @author Mindaugas Idzelis (min@idzelis.com)
 */
public class IndexUtil {
    
    /**
     * Create a lucene term from the first token of the input string.
     *
     * @param field The lucene document field to create a term with
     * @param input The input you wish to convert into a term
     * @return Lucene search term
     */
    public static final Term getTerm(String field, String input) {
        if (input==null || field==null) return null;
        Analyzer analyer = IndexManagerImpl.getAnalyzer();
        TokenStream tokens = analyer.tokenStream(field,
                new StringReader(input));
        
        Token token = null;
        Term term = null;
        try {
            token = tokens.next();
        } catch (IOException e) {}
        if (token!=null) {
            String termt = token.termText();
            term = new Term(field,termt);
        }
        return term;
    }
    
}
