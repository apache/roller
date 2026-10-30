/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.business.search.lucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Searches may name only the fields they are meant to cover.
 */
class SearchFieldsQueryParserTest {

    private static SearchOperation.SearchFieldsQueryParser parser() {
        return new SearchOperation.SearchFieldsQueryParser(
                new String[] {FieldConstants.CONTENT, FieldConstants.TITLE, FieldConstants.C_CONTENT},
                new StandardAnalyzer());
    }

    @Test
    void plainAndSearchFieldQueriesParse() throws Exception {
        assertNotNull(parser().parse("roller weblog"));
        assertNotNull(parser().parse("roll*"));
        assertNotNull(parser().parse("title:roller"));
        assertNotNull(parser().parse("content:roll*"));
        assertNotNull(parser().parse("\"apache roller\"~2"));
    }

    @Test
    void matchAllParses() throws Exception {
        assertEquals(new MatchAllDocsQuery(), parser().parse("*:*"));
    }

    @Test
    void otherFieldsAreRefused() {
        for (String query : new String[] {
                FieldConstants.C_EMAIL + ":a*",
                FieldConstants.C_EMAIL + ":reader@example.com",
                FieldConstants.C_NAME + ":reader",
                FieldConstants.USERNAME + ":admin",
                FieldConstants.ANCHOR + ":[a TO z]",
                FieldConstants.C_EMAIL + ":/re.*/",
                FieldConstants.C_EMAIL + ":reader~",
                "*:a*",
                FieldConstants.C_EMAIL + ":*",
                "roller AND " + FieldConstants.C_EMAIL + ":r?ader"}) {
            assertThrows(ParseException.class, () -> parser().parse(query), query);
        }
    }
}
