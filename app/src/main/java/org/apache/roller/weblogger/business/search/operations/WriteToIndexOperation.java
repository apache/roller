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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business.search.operations;

import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.util.BytesRef;
import org.apache.roller.weblogger.business.search.FieldConstants;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.search.IndexManagerImpl;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * An operation that writes to index.
 */
public abstract class WriteToIndexOperation extends IndexOperation {

    WriteToIndexOperation(IndexManager mgr) {
        super(mgr);
    }

    private static Logger log = LoggerFactory.getLogger(WriteToIndexOperation.class);

    private IndexWriter writer;

    public void run() {
        try {
            manager.getReadWriteLock().writeLock().lock();
            log.debug("Starting search index operation");
            doRun();
            log.debug("Search index operation complete");
        } catch (Exception e) {
            log.error("Error acquiring write lock on index", e);
        } finally {
            manager.getReadWriteLock().writeLock().unlock();
        }
    }

    IndexWriter beginWriting() {
        try {

            // Limit to 1000 tokens.
            LimitTokenCountAnalyzer analyzer = new LimitTokenCountAnalyzer(
                    IndexManagerImpl.getAnalyzer(), 1000);

            IndexWriterConfig config = new IndexWriterConfig(analyzer);

            writer = new IndexWriter(manager.getIndexDirectory(), config);

        } catch (IOException e) {
            log.error("ERROR creating writer", e);
        }

        return writer;
    }

    void endWriting() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                log.error("ERROR closing writer", e);
            }
        }
    }

    Document getDocument(WeblogEntry data) {

        String commentContent = "";
        String commentEmail = "";
        String commentName = "";
        if (manager.isIndexComments()) {
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
                .getWeblog().getHandle(), Field.Store.YES));

        // text, don't index deleted/disabled users of a group blog
        if (data.getCreator() != null) {
            doc.add(new TextField(FieldConstants.USERNAME, data.getCreator().getScreenName()
                    .toLowerCase(), Field.Store.YES));
        }

        // text
        doc.add(new TextField(FieldConstants.TITLE, data.getTitle(), Field.Store.YES));

        // index the entry text, but don't store it
        doc.add(new TextField(FieldConstants.CONTENT, data.getText(), Field.Store.NO));

        // keyword
        doc.add(new StringField(FieldConstants.UPDATED, data.getUpdateTime().toString(), Field.Store.YES));

        // keyword
        if (data.getPubTime() != null) {
            doc.add(new StringField(FieldConstants.PUBLISHED, data.getPubTime()
                    .toString(), Field.Store.YES));
            // below effectively required Lucene 5.0+, as SearchOperation
            // sorts on this field
            doc.add(new SortedDocValuesField(FieldConstants.PUBLISHED,
                    new BytesRef(data.getPubTime().toString())));
        }

        // index Category, needs to be in lower case as it is used in a term
        WeblogCategory categorydata = data.getCategory();
        if (categorydata != null) {
            doc.add(new StringField(FieldConstants.CATEGORY, categorydata
                    .getName().toLowerCase(), Field.Store.YES));
        }

        // index Comments, unstored
        doc.add(new TextField(FieldConstants.COMMENT_CONTENT, commentContent, Field.Store.NO));

        // keyword
        doc.add(new StringField(FieldConstants.COMMENT_EMAIL, commentEmail, Field.Store.YES));

        // keyword
        doc.add(new StringField(FieldConstants.COMMENT_NAME, commentName, Field.Store.YES));

        return doc;
    }

}
