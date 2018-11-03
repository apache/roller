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
package org.tightblog.service.indexer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.util.BytesRef;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.domain.WeblogCategory;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Tasks that update an index.
 */
public abstract class AbstractIndexTask extends AbstractTask {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractIndexTask.class);

    AbstractIndexTask(LuceneIndexer mgr) {
        super(mgr);
    }

    public void run() {
        try {
            manager.getReadWriteLock().writeLock().lock();
            LOG.debug("Starting index task");
            doRun();
            LOG.debug("Index task complete");
        } catch (Exception e) {
            LOG.error("Error acquiring write lock on index", e);
        } finally {
            manager.getReadWriteLock().writeLock().unlock();
        }
    }

    IndexWriter beginWriting() {
        IndexWriter writer = null;

        Analyzer analyzer = manager.getAnalyzer();
        if (analyzer != null) {
            try {
                LimitTokenCountAnalyzer ltcAnalyzer = new LimitTokenCountAnalyzer(analyzer, manager.getMaxTokenCount());
                IndexWriterConfig config = new IndexWriterConfig(ltcAnalyzer);
                writer = new IndexWriter(manager.getIndexDirectory(), config);
            } catch (IOException e) {
                LOG.error("ERROR creating writer", e);
            }
        }
        return writer;
    }

    Document getDocument(WeblogEntry data) {
        Document doc = new Document();

        // keyword
        doc.add(new StringField(FieldConstants.ID, data.getId(), Field.Store.YES));

        // keyword
        doc.add(new StringField(FieldConstants.WEBLOG_HANDLE, data.getWeblog().getHandle(),
                Field.Store.YES));

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
            doc.add(new StringField(FieldConstants.PUBLISHED, data.getPubTime().toString(), Field.Store.YES));

            // below included as search task sorts on this field
            doc.add(new SortedDocValuesField(FieldConstants.PUBLISHED,
                    new BytesRef(data.getPubTime().toString())));
        }

        // index Category, needs to be in lower case as it is used in a term
        WeblogCategory categoryData = data.getCategory();
        if (categoryData != null) {
            doc.add(new StringField(FieldConstants.CATEGORY, categoryData
                    .getName().toLowerCase(), Field.Store.YES));
        }

        String commentName = "";
        String commentEmail = "";
        String commentContent = "";

        if (manager.isIndexComments()) {
            List<WeblogEntryComment> comments = data.getComments();
            if (comments != null) {
                StringBuilder commentNameBld = new StringBuilder();
                StringBuilder commentEmailBld = new StringBuilder();
                StringBuilder commentContentBld = new StringBuilder();
                for (WeblogEntryComment comment : comments) {
                    if (comment.getName() != null) {
                        commentNameBld.append(comment.getName());
                        commentNameBld.append(",");
                    }
                    if (comment.getEmail() != null) {
                        commentEmailBld.append(comment.getEmail());
                        commentEmailBld.append(",");
                    }
                    if (comment.getContent() != null) {
                        commentContentBld.append(comment.getContent());
                        commentContentBld.append(",");
                    }
                }
                commentName = commentNameBld.toString();
                commentEmail = commentEmailBld.toString();
                commentContent = commentContentBld.toString();
            }
        }

        // index Comments, not stored
        doc.add(new TextField(FieldConstants.COMMENT_CONTENT, commentContent, Field.Store.NO));

        // keyword
        doc.add(new StringField(FieldConstants.COMMENT_EMAIL, commentEmail, Field.Store.YES));

        // keyword
        doc.add(new StringField(FieldConstants.COMMENT_NAME, commentName, Field.Store.YES));

        return doc;
    }

}
