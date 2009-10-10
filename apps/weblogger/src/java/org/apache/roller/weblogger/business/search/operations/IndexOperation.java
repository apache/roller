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
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.roller.weblogger.business.search.IndexManagerImpl;
import org.apache.roller.weblogger.business.search.FieldConstants;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.config.WebloggerConfig;

/**
 * This is the base class for all index operation. 
 * These operations include:<br>
 *    SearchOperation<br>
 *    AddWeblogOperation<br>
 *    RemoveWeblogOperation<br>
 *    RebuildUserIndexOperation
 *
 * @author Mindaugas Idzelis (min@idzelis.com)
 */
public abstract class IndexOperation implements Runnable {

    private static Log mLogger = LogFactory.getFactory().getInstance(IndexOperation.class);

    //~ Instance fields
    // ========================================================
    protected IndexManagerImpl manager;
    private IndexReader reader;
    private IndexWriter writer;

    //~ Constructors
    // ===========================================================
    public IndexOperation(IndexManagerImpl manager) {
        this.manager = manager;
    }

    //~ Methods
    // ================================================================
    protected Document getDocument(WeblogEntry data) {

        // Actual comment content is indexed only if search.index.comments
        // is true or absent from the (static) configuration properties.
        // If false in the configuration, comments are treated as if empty.
        boolean indexComments =
            WebloggerConfig.getBooleanProperty("search.index.comments", true);

        String commentContent = "";
        String commentEmail = "";
        String commentName = "";
        if (indexComments) {
            List comments = data.getComments();
            if (comments != null) {
                StringBuffer commentEmailBuf = new StringBuffer();
                StringBuffer commentContentBuf = new StringBuffer();
                StringBuffer commentNameBuf = new StringBuffer();
                for (Iterator cItr = comments.iterator(); cItr.hasNext();) {
                    WeblogEntryComment comment = (WeblogEntryComment) cItr.next();
                    if (comment.getContent() != null) {
                        commentContentBuf.append(comment.getContent());
                        commentContentBuf.append(",");
                    }
                    if (comment.getEmail() != null) {
                        commentEmailBuf.append(comment.getEmail());
                        commentEmailBuf.append(",");
                    }
                    if (comment.getName() != null) {
                        commentNameBuf.append(comment.getName());
                        commentNameBuf.append(",");
                    }
                }
                commentEmail = commentEmailBuf.toString();
                commentContent = commentContentBuf.toString();
                commentName = commentNameBuf.toString();
            }
        }

        Document doc = new Document();

        // keyword
        doc.add(new Field(FieldConstants.ID, data.getId(),
                Field.Store.YES, Field.Index.NOT_ANALYZED));

        // keyword
        doc.add(new Field(FieldConstants.WEBSITE_HANDLE,
                data.getWebsite().getHandle(),
                Field.Store.YES, Field.Index.NOT_ANALYZED));

        // unindexed
        doc.add(new Field(FieldConstants.ANCHOR,
                data.getAnchor(),
                Field.Store.YES, Field.Index.NO));

        // text
        doc.add(new Field(FieldConstants.USERNAME,
                data.getCreator().getUserName(),
                Field.Store.YES, Field.Index.ANALYZED));

        // text
        doc.add(new Field(FieldConstants.TITLE,
                data.getTitle(), 
                Field.Store.YES, Field.Index.ANALYZED));

        // index the entry text, but don't store it - moved to end of block
        // unstored
        doc.add(new Field(FieldConstants.CONTENT,
                data.getText(),
                Field.Store.NO, Field.Index.ANALYZED));

        // store an abbreviated version of the entry text, but don't index
        // unindexed
        doc.add(new Field(FieldConstants.CONTENT_STORED,
                Utilities.truncateNicely(Utilities.removeHTML(data.getText()), 240, 260, "..."),
                Field.Store.YES, Field.Index.NO));

        // keyword
        doc.add(new Field(FieldConstants.UPDATED,
                data.getUpdateTime().toString(),
                Field.Store.YES, Field.Index.NOT_ANALYZED));

        // keyword
        doc.add(new Field(FieldConstants.PUBLISHED,
                data.getPubTime().toString(),
                Field.Store.YES, Field.Index.NOT_ANALYZED));

        // index Comments
        // unstored
        doc.add(new Field(FieldConstants.C_CONTENT,
                commentContent,
                Field.Store.NO, Field.Index.ANALYZED));
        // unstored
        doc.add(new Field(FieldConstants.C_EMAIL,
                commentEmail,
                Field.Store.NO, Field.Index.ANALYZED));
        // unstored
        doc.add(new Field(FieldConstants.C_NAME,
                commentName,
                Field.Store.NO, Field.Index.ANALYZED));

        // unstored
        doc.add(new Field(FieldConstants.CONSTANT,
                FieldConstants.CONSTANT_V,
                Field.Store.NO, Field.Index.ANALYZED));

        // index Category
        WeblogCategory categorydata = data.getCategory();
        Field category = (categorydata == null)
                // unstored
                ? new Field(FieldConstants.CATEGORY, "", Field.Store.NO, Field.Index.ANALYZED)
                // text
                : new Field(FieldConstants.CATEGORY, categorydata.getName(), Field.Store.YES, Field.Index.ANALYZED);
        doc.add(category);

        return doc;
    }

    protected IndexReader beginDeleting() {
        try {
            reader = IndexReader.open(manager.getIndexDirectory());
        } catch (IOException e) {
        }

        return reader;
    }

    protected void endDeleting() {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                mLogger.error("ERROR closing reader");
            }
        }
    }

    protected IndexWriter beginWriting() {
        try {
            writer = new IndexWriter(manager.getIndexDirectory(), IndexManagerImpl.getAnalyzer(), false);
        } catch (IOException e) {
            mLogger.error("ERROR creating writer", e);
        }

        return writer;
    }

    protected void endWriting() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                mLogger.error("ERROR closing writer", e);
            }
        }
    }

    public void run() {
        doRun();
    }

    protected abstract void doRun();
}
