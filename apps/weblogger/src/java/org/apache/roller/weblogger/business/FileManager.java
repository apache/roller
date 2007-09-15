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
import org.apache.roller.weblogger.pojos.ThemeResource;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Interface for managing files uploaded to Roller.
 */
public interface FileManager {
    
    /**
     * Get a reference to a specific file in a weblog's uploads area.
     * 
     * This method always returns a valid file or will throw an exception
     * if the specificed path doesn't exist, is a directory, or can't be read.
     * 
     * @param weblog The weblog we are working on.
     * @param path The relative path to the desired resource within 
     * the weblog's uploads area.
     *
     * @throws FileNotFoundException If path does not exist.
     * @throws FilePathException If path is invalid, is a directory, or can't be read.
     *
     * @return ThemeResource representing the real file resource.
     */
    public ThemeResource getFile(Weblog weblog, String path) 
        throws FileNotFoundException, FilePathException;
    
    
    /**
     * Get list of files from a specific path of the weblog's uploads area.
     * 
     * This method will return a ThemeResource[] array of all files at the 
     * given path if it exists, otherwise it will throw an exception.
     * 
     * This method should return the files at the root of the weblog's uploads
     * area given a path value of null, "" (empty string), or "/"
     * 
     * This method should NOT return any resources which represent directories.
     *
     * @param weblog The weblog we are working on.
     * @param path The relative path to the desired resource within 
     * the weblog's uploads area.
     * 
     * @throws FileNotFoundException If path does not exist.
     * @throws FilePathException If path is invalid, is not a directory, or can't be read.
     *
     * @return ThemeResource[] of files in website's uploads area at given path.
     */
    public ThemeResource[] getFiles(Weblog weblog, String path) 
        throws FileNotFoundException, FilePathException;
    
    
    /**
     * Get list of directories from a weblog's uploads area.
     * 
     * This method will return an array of all dirs in the weblogs' uploads 
     * area, otherwise it will throw an exception.
     * 
     * @param weblog The weblog we are working on.
     * 
     * @throws FileNotFoundException If path does not exist.
     * @throws FilePathException If path is invalid, or can't be read.
     *
     * @return ThemeResource[] of directories in website's uploads area.
     */
    public ThemeResource[] getDirectories(Weblog weblog)
            throws FileNotFoundException, FilePathException;
    
    
    /**
     * Save a file to weblog's uploads area, with can-save check.
     * 
     * @param weblog The weblog we are working on.
     * @param path The relative path to the desired location within 
     * the weblog's uploads area where the file should be saved.
     * @param contentType Content type of the file.
     * @param size Size of file to be saved.
     * @param is InputStream to read the file from.
     *
     * @throws FileNotFoundException If path to save location does not exist.
     * @throws FilePathException If path is invalid, is not a directory, or can't be read.
     * @throws FileIOException If there is an unexpected error during the save.
     */
    public void saveFile(Weblog weblog, 
                         String path, 
                         String contentType, 
                         long size,
                         InputStream is)
        throws FileNotFoundException, FilePathException, FileIOException;
    
    
    /**
     * Save a file to weblog's uploads area, with optional can-save check.
     * 
     * @param weblog The weblog we are working on.
     * @param path The relative path to the desired location within 
     * the weblog's uploads area where the file should be saved.
     * @param contentType Content type of the file.
     * @param size Size of file to be saved.
     * @param is InputStream to read the file from.
     *
     * @throws FileNotFoundException If path to save location does not exist.
     * @throws FilePathException If path is invalid, is not a directory, or can't be read.
     * @throws FileIOException If there is an unexpected error during the save.
     */
    public void saveFile(Weblog weblog, 
                         String path, 
                         String contentType, 
                         long size,
                         InputStream is,
                         boolean checkCanSave)
        throws FileNotFoundException, FilePathException, FileIOException;
    
    
    /**
     * Create an empty subdirectory in the weblog's uploads area.
     *
     * @param weblog The weblog we are working on.
     * @param path The relative path to the desired location within 
     * the weblog's uploads area where the directory should be created.
     *
     * @throws FileNotFoundException If path to create location does not exist.
     * @throws FilePathException If path is invalid, is not a directory, or can't be read.
     * @throws FileIOException If there is an unexpected error during the create.
     */
    public void createDirectory(Weblog weblog, String path) 
        throws FileNotFoundException, FilePathException, FileIOException;
    
    
    /**
     * Delete file or directory from weblog's uploads area.
     * 
     * @param weblog The weblog we are working on.
     * @param path The relative path to the file within the weblog's uploads 
     * area that should be deleted.
     *
     * @throws FileNotFoundException If path does not exist.
     * @throws FilePathException If path is invalid, or can't be read.
     * @throws FileIOException If there is an unexpected error during the delete.
     */
    public void deleteFile(Weblog weblog, String path) 
        throws FileNotFoundException, FilePathException, FileIOException;
    
    
    /**
     * Delete all files associated with a given weblog, including the root folder.
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
     *
     * @return True if weblog is over set quota, False otherwise.
     */
    public boolean overQuota(Weblog weblog);
    
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
    
}
