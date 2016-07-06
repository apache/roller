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
package org.apache.roller.weblogger.business;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.roller.weblogger.pojos.FileContent;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Interface for managing contents of the files uploaded to the weblogger.
 */
public interface FileContentManager {

    /**
     * Get a reference to the content of a specific file in a weblog's uploads area.
     * 
     * This method always returns a valid file content object or will throw an exception
     * if the specified path doesn't exist, or can't be read.
     * 
     * @param weblog The weblog we are working on.
     * @param fileId file identifier from database.
     *
     * @throws FileNotFoundException If file does not exist.
     * @throws IOException Some other problem accessing or reading file.
     */
    FileContent getFileContent(Weblog weblog, String fileId) throws IOException;

    /**
     * Save a file's content to weblog's uploads area.
     * 
     * @param weblog The weblog we are working on.
     * @param fileId file identifier from database.
     * @param is InputStream to read the file from.
     *
     * @throws FileNotFoundException If path to save location does not exist.
     * @throws IOException If there is an unexpected error during the save.
     */
    void saveFileContent(Weblog weblog,
            String fileId,
            InputStream is)
            throws IOException;

    /**
     * Delete file content from weblog's uploads area.
     * 
     * @param weblog The weblog we are working on.
     * @param fileId file identifier from database.
     *
     * @throws FileNotFoundException If file does not exist.
     * @throws IOException If path does not exist or there is an unexpected error during the delete.
     */
    void deleteFile(Weblog weblog, String fileId) throws IOException;

    /**
     * Determine if file can be saved given current WebloggerStaticConfig settings.
     * 
     * @param weblog The weblog we are working on.
     * @param fileName name of the file to be saved
     * @param contentType content type of the file
     * @param size size of the file in bytes.
     * @param messages output parameter for resource bundle messages, or null if not necessary to receive them
     * @return true if the file can be saved, false otherwise. 
     */
    boolean canSave(Weblog weblog,
            String fileName,
            String contentType,
            long size,
            Map<String, List<String>> messages);

}
