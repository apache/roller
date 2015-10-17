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
/* Created on Jul 16, 2003 */
package org.apache.roller.weblogger.business.search.operations;

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.roller.weblogger.business.search.FieldConstants;
import org.apache.roller.weblogger.business.search.IndexManagerImpl;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;

/**
 * This is the base class for all index operation. These operations include:<br>
 * SearchOperation<br>
 * AddWeblogOperation<br>
 * RemoveWeblogOperation<br>
 * RebuildUserIndexOperation
 * 
 * @author Mindaugas Idzelis (min@idzelis.com)
 */
public abstract class IndexOperation implements Runnable {

    private static Log mLogger = LogFactory.getFactory().getInstance(
            IndexOperation.class);

    // ~ Instance fields
    // ========================================================
    protected IndexManagerImpl manager;
    private IndexWriter writer;

    // ~ Constructors
    // ===========================================================
    public IndexOperation(IndexManagerImpl manager) {
        this.manager = manager;
    }

    // ~ Methods
    // ================================================================
    protected Document getDocument(WeblogEntry data) {

        // Actual comment content is indexed only if search.index.comments
        // is true or absent from the (static) configuration properties.
        // If false in the configuration, comments are treated as if empty.
        boolean indexComments = WebloggerConfig.getBooleanProperty(
                "search.index.comments", true);

        String commentContent = "";
        String commentEmail = "";
        String commentName = "";
        if (indexComments) {
            List<WeblogEntryComment> comments = data.getComments();
            if (comments != null) {
                StringBuilder commentEmailBld = new StringBuilder();
                StringBuilder commentContentBld = new StringBuilder();
                StringBuilder commentNameBld = new StringBuilder();
                for (WeblogEntryComment comment : comments) {
                    if (comment.getContent() != null) {
                        commentContentBld.append(comment.getContent());
                        commentContentBld.append(",");
                    }
                    if (comment.getEmail() != null) {
                        commentEmailBld.append(comment.getEmail());
                        commentEmailBld.append(",");
                    }
                    if (comment.getName() != null) {
                        commentNameBld.append(comment.getName());
                        commentNameBld.append(",");
                    }
                }
                commentEmail = commentEmailBld.toString();
                commentContent = commentContentBld.toString();
                commentName = commentNameBld.toString();
            }
        }

        Document doc = new Document();

        // keyword
        doc.add(new StringField(FieldConstants.ID, data.getId(),
                Field.Store.YES));

        // keyword
        doc.add(new StringField(FieldConstants.WEBSITE_HANDLE, data
                .getWebsite().getHandle(), Field.Store.YES));

        // text, don't index deleted/disabled users of a group blog
        if (data.getCreator() != null) {
            doc.add(new TextField(FieldConstants.USERNAME, data.getCreator()
                    .getUserName().toLowerCase(), Field.Store.YES));
        }

        // text
        doc.add(new TextField(FieldConstants.TITLE, data.getTitle(),
                Field.Store.YES));

        // keyword needs to be in lower case as we are used in a term
        doc.add(new StringField(FieldConstants.LOCALE, data.getLocale()
                .toLowerCase(), Field.Store.YES));

        // index the entry text, but don't store it
        doc.add(new TextField(FieldConstants.CONTENT, data.getText(),
                Field.Store.NO));

        // keyword
        doc.add(new StringField(FieldConstants.UPDATED, data.getUpdateTime()
                .toString(), Field.Store.YES));

        // keyword
        if (data.getPubTime() != null) {
            doc.add(new StringField(FieldConstants.PUBLISHED, data.getPubTime()
                    .toString(), Field.Store.YES));
        }

        // index Category, needs to be in lower case as it is used in a term
        WeblogCategory categorydata = data.getCategory();
        if (categorydata != null) {
            doc.add(new StringField(FieldConstants.CATEGORY, categorydata
                    .getName().toLowerCase(), Field.Store.YES));
        }

        // index Comments, unstored
        doc.add(new TextField(FieldConstants.C_CONTENT, commentContent,
                Field.Store.NO));

        // keyword
        doc.add(new StringField(FieldConstants.C_EMAIL, commentEmail,
                Field.Store.YES));

        // keyword
        doc.add(new StringField(FieldConstants.C_NAME, commentName,
                Field.Store.YES));

        return doc;
    }

    /**
     * Begin writing.
     * 
     * @return the index writer
     */
    protected IndexWriter beginWriting() {
        try {

            // Limit to 1000 tokens.
            LimitTokenCountAnalyzer analyzer = new LimitTokenCountAnalyzer(
                    IndexManagerImpl.getAnalyzer(), 1000);

            IndexWriterConfig config = new IndexWriterConfig(
                    FieldConstants.LUCENE_VERSION, analyzer);

            writer = new IndexWriter(manager.getIndexDirectory(), config);

        } catch (IOException e) {
            mLogger.error("ERROR creating writer", e);
        }

        return writer;
    }

    /**
     * End writing.
     */
    protected void endWriting() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                mLogger.error("ERROR closing writer", e);
            }
        }
    }

    /**
     * @see java.lang.Runnable#run()
     */
    public void run() {
        doRun();
    }

    protected abstract void doRun();
}
