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

import java.util.Collection;
import java.util.List;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.pojos.Weblog;

public interface MediaFileManager {

	public void createMediaFile(Weblog weblog, MediaFile mediaFile) throws WebloggerException ;
	public void updateMediaFile(Weblog weblog, MediaFile mediaFile) throws WebloggerException ;
	public MediaFile getMediaFile(String id) throws WebloggerException;
	public MediaFile getMediaFile(String id, boolean includeContent) throws WebloggerException;
	public void removeMediaFile(Weblog weblog, MediaFile mediaFile) throws WebloggerException;

	public List<MediaFile> searchMediaFiles(Weblog weblog, MediaFileFilter filter) throws WebloggerException;

	public MediaFileDirectory createRootMediaFileDirectory(Weblog weblog) throws WebloggerException;
	public MediaFileDirectory createMediaFileDirectory(MediaFileDirectory parentDirectory, String newDirName) throws WebloggerException;
	public void createMediaFileDirectory(MediaFileDirectory directory) throws WebloggerException;
	public MediaFileDirectory createMediaFileDirectoryByPath(Weblog weblog, String path) throws WebloggerException; 

	public MediaFileDirectory getMediaFileDirectory(String id) throws WebloggerException;
	public MediaFileDirectory getMediaFileDirectoryByPath(Weblog weblog, String path) throws WebloggerException; 
	public List<MediaFileDirectory> getMediaFileDirectories(Weblog weblog) throws WebloggerException;
    public MediaFileDirectory getMediaFileRootDirectory(Weblog weblog) throws WebloggerException;
    
    public void moveMediaFiles(Collection<MediaFile> mediaFiles, MediaFileDirectory directory) throws WebloggerException;
    public void moveMediaFile(MediaFile mediaFile, MediaFileDirectory directory) throws WebloggerException;

	public void release();
}
