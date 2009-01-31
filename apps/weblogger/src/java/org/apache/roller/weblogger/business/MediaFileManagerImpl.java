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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.FileContent;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.util.RollerMessages;

@com.google.inject.Singleton
public class MediaFileManagerImpl implements MediaFileManager {
	
    private final Weblogger roller;
    private final JPAPersistenceStrategy persistenceStrategy;
    
    /**
     * Creates a new instance of MediaFileManagerImpl
     */
   @com.google.inject.Inject
	protected MediaFileManagerImpl(Weblogger roller, JPAPersistenceStrategy persistenceStrategy) {
	   this.roller = roller;
	   this.persistenceStrategy = persistenceStrategy;
    }
	
	/**
     * The logger instance for this class.
     */
    private static Log log = LogFactory
            .getFactory().getInstance(MediaFileManagerImpl.class);

	public void release() {
		
	}
	
    @OneToMany
	public void moveMediaFiles(Collection<MediaFile> mediaFiles, MediaFileDirectory targetDirectory) 
        throws WebloggerException {
    	for (MediaFile mediaFile: mediaFiles) {
    		mediaFile.setDirectory(targetDirectory);
    		this.persistenceStrategy.store(mediaFile);
    	}
        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWeblog(targetDirectory.getWeblog());
    }

	
	public void createMediaFileDirectory(MediaFileDirectory directory) 
	  throws WebloggerException {
		this.persistenceStrategy.store(directory);
        
        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWeblog(directory.getWeblog());
	}
	
	public MediaFileDirectory createRootMediaFileDirectory(Weblog weblog) 
	    throws WebloggerException {
        MediaFileDirectory rootDirectory = new MediaFileDirectory(null, "root", "root directory", weblog);
		createMediaFileDirectory(rootDirectory);
		return rootDirectory;
	}

	

	public void createMediaFile(Weblog weblog, MediaFile mediaFile) throws WebloggerException {
		
        FileContentManager cmgr = WebloggerFactory.getWeblogger().getFileContentManager();
        RollerMessages msgs = new RollerMessages();
        if (!cmgr.canSave(weblog, mediaFile.getName(), mediaFile.getContentType(), mediaFile.getLength(), msgs)) {
            throw new FileIOException(msgs.toString());
        }

        mediaFile.setDateUploaded(new Timestamp(System.currentTimeMillis()));
        mediaFile.setLastUpdated(mediaFile.getDateUploaded());
		persistenceStrategy.store(mediaFile);
        // update weblog last modified date.  date updated by saveWeblog()
        roller.getWeblogManager().saveWeblog(weblog);
        
        cmgr.saveFileContent(weblog, mediaFile.getId(), mediaFile.getInputStream());
	}
	
	public void updateMediaFile(Weblog weblog, MediaFile mediaFile) throws WebloggerException {
		mediaFile.setLastUpdated(new Timestamp(System.currentTimeMillis()));
		persistenceStrategy.store(mediaFile);
        // update weblog last modified date.  date updated by saveWeblog()
        roller.getWeblogManager().saveWeblog(weblog);
	}

	public MediaFile getMediaFile(String id) throws WebloggerException {
        return getMediaFile(id, false);
	}

	public MediaFile getMediaFile(String id, boolean includeContent) throws WebloggerException {
		MediaFile mediaFile = (MediaFile) this.persistenceStrategy.load(MediaFile.class, id);
		if (includeContent) {
	        FileContentManager cmgr = WebloggerFactory.getWeblogger().getFileContentManager();
	        FileContent content = cmgr.getFileContent(mediaFile.getDirectory().getWeblog(), id);
	        mediaFile.setContent(content);
		}
        return mediaFile;
	}

	public MediaFileDirectory getMediaFileDirectory(String id) 
	    throws WebloggerException {
		return (MediaFileDirectory) this.persistenceStrategy.load(MediaFileDirectory.class, id);
	}

	public MediaFileDirectory getMediaFileRootDirectory(Weblog weblog) 
    throws WebloggerException {
        Query q = this.persistenceStrategy.getNamedQuery("MediaFileDirectory.getByWeblogAndNoParent");
        q.setParameter(1, weblog);
        try {
            return (MediaFileDirectory)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
	}

	public List<MediaFileDirectory> getMediaFileDirectories(Weblog weblog) 
	    throws WebloggerException {
        
        Query q = this.persistenceStrategy.getNamedQuery("MediaFileDirectory.getByWeblog");
        q.setParameter(1, weblog);
        return q.getResultList();
	}
	
	public void removeMediaFile(MediaFile mediaFile) 
	    throws WebloggerException {
		this.persistenceStrategy.remove(mediaFile);
	}
	
	/*
	public void searchMediaFiles(MediaFileSearchCriteria searchCriteria) {
		
	}
	*/

}
