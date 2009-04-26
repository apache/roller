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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.OneToMany;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.FileContent;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.MediaFileFilter.MediaFileOrder;
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
	
	public void moveMediaFiles(Collection<MediaFile> mediaFiles, MediaFileDirectory targetDirectory) 
        throws WebloggerException {
    	for (MediaFile mediaFile: mediaFiles) {
    		mediaFile.setDirectory(targetDirectory);
    		this.persistenceStrategy.store(mediaFile);
    	}
        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWeblog(targetDirectory.getWeblog());
    }

	public void moveMediaFile(MediaFile mediaFile, MediaFileDirectory targetDirectory) 
        throws WebloggerException {
		moveMediaFiles(Arrays.asList(mediaFile), targetDirectory);
	}
	
	public MediaFileDirectory createMediaFileDirectory(MediaFileDirectory parentDirectory, String newDirName) 
	  throws WebloggerException {
		
		if (parentDirectory.hasDirectory(newDirName)) {
            throw new WebloggerException("Directory exists");
		}
		
		MediaFileDirectory newDirectory = parentDirectory.createNewDirectory(newDirName);

		// update weblog last modified date.  date updated by saveWeblog()
        roller.getWeblogManager().saveWeblog(newDirectory.getWeblog());
        
        return newDirectory;
	}
	
	public void createMediaFileDirectory(MediaFileDirectory directory) 
	  throws WebloggerException {
		this.persistenceStrategy.store(directory);
        
        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWeblog(directory.getWeblog());
	}
	
	public MediaFileDirectory createMediaFileDirectoryByPath(Weblog weblog, String path) 
	    throws WebloggerException {
		if (path.startsWith("/")) {
			path = path.substring(1);
		}
		
		if (path.endsWith("/")) {
			path = path.substring(0, path.length() - 1);
		}
		
		if (path.equals("")) {
			/**
			 * Root cannot be created using this method. Use createRootMediaFileDirectory instead
			 */
			throw new WebloggerException("Invalid path!");
		}
		
		int lastPathIndex = path.lastIndexOf("/");
		
		MediaFileDirectory parentDirectory;
		String newDirName;
		if (lastPathIndex == -1) {
			//Directory needs to be created under root
			newDirName = path;
			parentDirectory = getMediaFileRootDirectory(weblog);
		}
		else {
			String parentPath = path.substring(0, lastPathIndex);
			newDirName = path.substring(lastPathIndex + 1);
			parentDirectory = getMediaFileDirectoryByPath(weblog, "/" + parentPath);
			// Validate whether the parent directory exists
			if (parentDirectory == null) {
	            throw new WebloggerException("Parent directory does not exist");
			}
		}

		if (parentDirectory.hasDirectory(newDirName)) {
            throw new WebloggerException("Directory exists");
		}
		
		MediaFileDirectory newDirectory = parentDirectory.createNewDirectory(newDirName);

		// update weblog last modified date.  date updated by saveWeblog()
        roller.getWeblogManager().saveWeblog(weblog);
        
        return newDirectory;
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

	public MediaFileDirectory getMediaFileDirectoryByPath(Weblog weblog, String path) 
    throws WebloggerException {
        Query q = this.persistenceStrategy.getNamedQuery("MediaFileDirectory.getByWeblogAndPath");
        q.setParameter(1, weblog);
        q.setParameter(2, path);
        try {
            return (MediaFileDirectory)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
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
	
	public void removeMediaFile(Weblog weblog, MediaFile mediaFile) 
	    throws WebloggerException {
        FileContentManager cmgr = WebloggerFactory.getWeblogger().getFileContentManager();

        this.persistenceStrategy.remove(mediaFile);
        // update weblog last modified date.  date updated by saveWeblog()
        roller.getWeblogManager().saveWeblog(weblog);

        try {
            cmgr.deleteFile(weblog, mediaFile.getId());
        }
        catch (FileNotFoundException e) {
            log.debug("File to be deleted already unavailable in the file store");
        }
	}
	
	public List<MediaFile> searchMediaFiles(Weblog weblog, MediaFileFilter filter) 
	     throws WebloggerException {
        
		List<Object> params = new ArrayList<Object>();
        int size = 0;
        StringBuffer queryString = new StringBuffer();
        StringBuffer whereClause = new StringBuffer();
        StringBuffer orderBy = new StringBuffer();
        
        queryString.append("SELECT m FROM MediaFile m WHERE ");
        
        params.add(size ++, weblog);
        whereClause.append("m.directory.weblog = ?" + size);
        
        if (!StringUtils.isEmpty(filter.getName())) {
        	String nameFilter = filter.getName();
        	nameFilter = nameFilter.trim();
        	if (!nameFilter.endsWith("%")) {
        		nameFilter = nameFilter + "%";
        	}
        	params.add(size ++, nameFilter);
        	whereClause.append(" AND m.name like ?" + size);
        }

        if (filter.getSize() > 0) {
        	params.add(size ++, filter.getSize());
        	whereClause.append(" AND m.length ");
        	switch (filter.getSizeFilterType()) {
            	case GT: whereClause.append(">");break;
            	case GTE: whereClause.append(">=");break;
            	case EQ: whereClause.append("=");break;
        	    case LT: whereClause.append("<");break;
        	    case LTE: whereClause.append("<=");break;
        	    default: whereClause.append("=");break;
        	}
        	whereClause.append(" ?" + size);
        }
        
        if (filter.getTags() != null) {
        	whereClause.append(" AND EXISTS (SELECT t FROM MediaFileTag t WHERE t.mediaFile = m and t.name IN (");
        	for (String tag: filter.getTags()) {
        	    params.add(size ++, tag);
        	    whereClause.append("?").append(size).append(",");
        	}
        	whereClause.deleteCharAt(whereClause.lastIndexOf(","));
        	whereClause.append("))");
        }
        
        if (filter.getType() != null) {
        	whereClause.append(" AND m.contentType IN (");
    		for (String contentType: filter.getType().getContentTypes()) {
        	    params.add(size ++, contentType);
        	    whereClause.append("?").append(size).append(",");
    		}
        	whereClause.deleteCharAt(whereClause.lastIndexOf(","));
        	whereClause.append(")");
        }
        
        switch(filter.getOrder()) {
            case NAME: orderBy.append(" order by m.name");break;
            case DATE_UPLOADED: orderBy.append(" order by m.dateUploaded");break;
            case TYPE: orderBy.append(" order by m.contentType");break;
            default:
        }

        Query query = persistenceStrategy.getDynamicQuery(queryString.toString() + whereClause.toString());
        for (int i=0; i<params.size(); i++) {
            query.setParameter(i+1, params.get(i));
        }
        
        if (filter.getStartIndex() >= 0) {
            query.setFirstResult(filter.getStartIndex());
            query.setMaxResults(filter.getLength());
        }
        
        return query.getResultList();
	}
	
	public static void main(String[] args) {
		/**
		MediaFileManagerImpl impl = new MediaFileManagerImpl(null, null);
		
		MediaFileFilter filter = new MediaFileFilter();
		filter.setName("testname");

		filter.setSize(3);
		filter.setSizeFilterType(MediaFileFilter.SizeFilterType.EQ);
		
		List<String> tags = new ArrayList<String>();
		tags.add("test1");
		tags.add("test2");
		filter.setTags(tags);
		
		filter.setType(MediaFileType.IMAGE);

		System.out.println(impl.searchMediaFiles(null, filter));
		*/
	}
	
	
	
	
	
	/*
	public void searchMediaFiles(MediaFileSearchCriteria searchCriteria) {
		
	}
	*/

}
