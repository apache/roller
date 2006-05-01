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
/*
 * Created on Jul 16, 2003
 * Authored by: Mindaugas Idzelis  (min@idzelis.com)
 */
package org.roller.business.search.operations;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.roller.business.IndexManagerImpl;
import org.roller.business.search.FieldConstants;
import org.roller.pojos.CommentData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.util.Utilities;
import org.roller.config.RollerConfig;

/**
 * @author aim4min
 *         <p/>
 *         This is the base class for all index operation. These operations include:
 *         <p/>
 *         SearchOperation AddWeblogOperation RemoveWeblogOperation
 *         RebuildUserIndexOperation
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

    protected Document getDocument(WeblogEntryData data) {

        // Actual comment content is indexed only if search.index.comments
        // is true or absent from the (static) configuration properties.
        // If false in the configuration, comments are treated as if empty.
        boolean indexComments = RollerConfig.getBooleanProperty("search.index.comments", true);

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
                    CommentData comment = (CommentData) cItr.next();
                    if (comment.getSpam() == null || !comment.getSpam().booleanValue()) {
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
                }
                commentEmail = commentEmailBuf.toString();
                commentContent = commentContentBuf.toString();
                commentName = commentNameBuf.toString();
            }
        }

        Document doc = new Document();

        doc.add(Field.Keyword(FieldConstants.ID, data.getId()));

        doc.add(Field.Keyword(FieldConstants.WEBSITE_HANDLE, data.getWebsite().getHandle()));

        doc.add(Field.UnIndexed(FieldConstants.ANCHOR, data.getAnchor()));
        doc.add(Field.Text(FieldConstants.USERNAME, data.getCreator().getUserName()));
        doc.add(Field.Text(FieldConstants.TITLE, data.getTitle()));

        // index the entry text, but don't store it - moved to end of block
        doc.add(Field.UnStored(FieldConstants.CONTENT, data.getText()));

        // store an abbreviated version of the entry text, but don't index
        doc.add(Field.UnIndexed(FieldConstants.CONTENT_STORED, Utilities
                .truncateNicely(Utilities.removeHTML(data.getText()), 240, 260, "...")));

        doc.add(Field.Keyword(FieldConstants.UPDATED, data.getUpdateTime()
                .toString()));
        doc.add(Field.Keyword(FieldConstants.PUBLISHED, data.getPubTime()
                .toString()));

        // index Comments
        doc.add(Field.UnStored(FieldConstants.C_CONTENT, commentContent));
        doc.add(Field.UnStored(FieldConstants.C_EMAIL, commentEmail));
        doc.add(Field.UnStored(FieldConstants.C_NAME, commentName));

        doc.add(Field.UnStored(FieldConstants.CONSTANT, FieldConstants.CONSTANT_V));

        // index Category
        WeblogCategoryData categorydata = data.getCategory();
        Field category = (categorydata == null) ? Field.UnStored(FieldConstants.CATEGORY, "") : Field.Text(FieldConstants.CATEGORY, categorydata.getName());
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
            }
        }
    }

    protected IndexWriter beginWriting() {
        try {
            writer = new IndexWriter(manager.getIndexDirectory(), IndexManagerImpl.getAnalyzer(), false);
        } catch (IOException e) {
            mLogger.error("ERROR creating writer");
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
