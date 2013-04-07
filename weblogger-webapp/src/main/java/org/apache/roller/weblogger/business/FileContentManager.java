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
package org.apache.roller.weblogger.business;

import java.io.InputStream;

import org.apache.roller.weblogger.pojos.FileContent;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.RollerMessages;

/**
 * Interface for managing contents of the files uploaded to Roller.
 */
public interface FileContentManager {

    /**
     * Get a reference to the content of a specific file in a weblog's uploads area.
     * 
     * This method always returns a valid file content object or will throw an exception
     * if the specificed path doesn't exist, or can't be read.
     * 
     * @param weblog The weblog we are working on.
     * @param fileId file identifier from database.
     *
     * @throws FileNotFoundException If path does not exist.
     * @throws FilePathException If path is invalid, or can't be read.
     */
    public FileContent getFileContent(Weblog weblog, String fileId)
            throws FileNotFoundException, FilePathException;

    /**
     * Save a file's content to weblog's uploads area.
     * 
     * @param weblog The weblog we are working on.
     * @param fileId file identifier from database.
     * @param is InputStream to read the file from.
     *
     * @throws FileNotFoundException If path to save location does not exist.
     * @throws FilePathException If path is invalid, is not a directory, or can't be read.
     * @throws FileIOException If there is an unexpected error during the save.
     */
    public void saveFileContent(Weblog weblog,
            String fileId,
            InputStream is)
            throws FileNotFoundException, FilePathException, FileIOException;

    /**
     * Delete file content from weblog's uploads area.
     * 
     * @param weblog The weblog we are working on.
     * @param fileId file identifier from database.
     *
     * @throws FileNotFoundException If path does not exist.
     * @throws FilePathException If path is invalid, or can't be read.
     * @throws FileIOException If there is an unexpected error during the delete.
     */
    public void deleteFile(Weblog weblog, String fileId)
            throws FileNotFoundException, FilePathException, FileIOException;

    /**
     * Delete all files associated with a given weblog.
     *
     * The only real use of this method is for when a weblog is being deleted.
     *
     * @param weblog The weblog to delete all files from.
     * @throws FileIOException If there is an unexpected error during the delete.
     */
    public void deleteAllFiles(Weblog weblog)
            throws FileIOException;

    /** 
     * Is the given weblog over the file-upload quota limit?
     *
     * @param weblog The weblog we are working on.
     * @return True if weblog is over set quota, False otherwise.
     */
    public boolean overQuota(Weblog weblog);

    /**
     * Determine if file can be saved given current WebloggerConfig settings.
     * 
     * @param weblog The weblog we are working on.
     * @param fileName name of the file to be saved
     * @param contentType content type of the file
     * @param size size of the file in bytes.
     * @param messages output parameter for adding messages.
     * @return true if the file can be saved, false otherwise. 
     */
    public boolean canSave(Weblog weblog,
            String fileName,
            String contentType,
            long size,
            RollerMessages messages);

    /**
     * Release all resources associated with Roller session.
     */
    public void release();

}
