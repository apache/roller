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
package org.apache.roller.weblogger.business.jpa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import org.apache.roller.weblogger.business.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import java.util.Properties;
import java.util.Set;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.FileContent;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.pojos.MediaFileType;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.Utilities;


@com.google.inject.Singleton
public class JPAMediaFileManagerImpl implements MediaFileManager {

    private final Weblogger roller;
    private final JPAPersistenceStrategy persistenceStrategy;
    private static Log log =
        LogFactory.getFactory().getInstance(JPAMediaFileManagerImpl.class);

    public static final String
        MIGRATIION_STATUS_FILENAME = "migration-status.properties";


    /**
     * Creates a new instance of MediaFileManagerImpl
     */
    @com.google.inject.Inject
    protected JPAMediaFileManagerImpl(Weblogger roller, JPAPersistenceStrategy persistenceStrategy) {
        this.roller = roller;
        this.persistenceStrategy = persistenceStrategy;
    }

    /**
     * Initialize manager; deal with upgrade/migration if 'uploads.migrate.auto' is true.
     */
    public void initialize() {
        boolean autoUpgrade = WebloggerConfig.getBooleanProperty("uploads.migrate.auto");
        if (autoUpgrade && this.isFileStorageUpgradeRequired()) {
            this.upgradeFileStorage();
        }
    }

    /**
     * Release resources; currently a no-op.
     */
    public void release() {
    }

    /**
     * {@inheritDoc}
     */
    public void moveMediaFiles(Collection<MediaFile> mediaFiles, MediaFileDirectory targetDirectory)
            throws WebloggerException {
        for (MediaFile mediaFile : mediaFiles) {
            mediaFile.setDirectory(targetDirectory);
            this.persistenceStrategy.store(mediaFile);
        }
        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWeblog(targetDirectory.getWeblog());
    }

    /**
     * {@inheritDoc}
     */
    public void moveMediaFile(MediaFile mediaFile, MediaFileDirectory targetDirectory)
            throws WebloggerException {
        moveMediaFiles(Arrays.asList(mediaFile), targetDirectory);
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public void createMediaFileDirectory(MediaFileDirectory directory)
            throws WebloggerException {
        this.persistenceStrategy.store(directory);

        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWeblog(directory.getWeblog());
    }

    /**
     * {@inheritDoc}
     */
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
        } else {
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

    /**
     * {@inheritDoc}
     */
    public MediaFileDirectory createRootMediaFileDirectory(Weblog weblog)
            throws WebloggerException {
        MediaFileDirectory rootDirectory = new MediaFileDirectory(null, "root", "root directory", weblog);
        createMediaFileDirectory(rootDirectory);
        return rootDirectory;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    public void updateMediaFile(Weblog weblog, MediaFile mediaFile) throws WebloggerException {
        mediaFile.setLastUpdated(new Timestamp(System.currentTimeMillis()));
        persistenceStrategy.store(mediaFile);
        // update weblog last modified date.  date updated by saveWeblog()
        roller.getWeblogManager().saveWeblog(weblog);
    }

    /**
     * {@inheritDoc}
     */
    public void updateMediaFile(Weblog weblog, MediaFile mediaFile, InputStream is) throws WebloggerException {
        mediaFile.setLastUpdated(new Timestamp(System.currentTimeMillis()));
        persistenceStrategy.store(mediaFile);
        // update weblog last modified date.  date updated by saveWeblog()
        roller.getWeblogManager().saveWeblog(weblog);

        FileContentManager cmgr = WebloggerFactory.getWeblogger().getFileContentManager();
        RollerMessages msgs = new RollerMessages();
        if (!cmgr.canSave(weblog, mediaFile.getName(), mediaFile.getContentType(), mediaFile.getLength(), msgs)) {
            throw new FileIOException(msgs.toString());
        }
        cmgr.saveFileContent(weblog, mediaFile.getId(), mediaFile.getInputStream());
    }

    /**
     * {@inheritDoc}
     */
    public MediaFile getMediaFile(String id) throws WebloggerException {
        return getMediaFile(id, false);
    }

    /**
     * {@inheritDoc}
     */
    public MediaFile getMediaFile(String id, boolean includeContent) throws WebloggerException {
        MediaFile mediaFile = (MediaFile) this.persistenceStrategy.load(MediaFile.class, id);
        if (includeContent) {
            FileContentManager cmgr = WebloggerFactory.getWeblogger().getFileContentManager();
            FileContent content = cmgr.getFileContent(mediaFile.getDirectory().getWeblog(), id);
            mediaFile.setContent(content);
        }
        return mediaFile;
    }

    /**
     * {@inheritDoc}
     */
    public MediaFileDirectory getMediaFileDirectoryByPath(Weblog weblog, String path)
            throws WebloggerException {
        Query q = this.persistenceStrategy.getNamedQuery(
                "MediaFileDirectory.getByWeblogAndPath");
        q.setParameter(1, weblog);
        q.setParameter(2, path);
        try {
            return (MediaFileDirectory) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public MediaFile getMediaFileByPath(Weblog weblog, String path)
            throws WebloggerException {

        // get directory
        MediaFileDirectory mdir = getMediaFileDirectoryByPath(
            weblog, path.substring(0, path.lastIndexOf("/")));
        if (mdir == null) return null; // no such path found

        return mdir.getMediaFile(path.substring(path.lastIndexOf("/")));
    }

    /**
     * {@inheritDoc}
     */
    public MediaFile getMediaFileByOriginalPath(Weblog weblog, String origpath) 
            throws WebloggerException {
        Query q = this.persistenceStrategy.getNamedQuery(
                "MediaFile.getByWeblogAndOrigpath");
        q.setParameter(1, weblog);
        q.setParameter(2, origpath);
        try {
            return (MediaFile) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public MediaFileDirectory getMediaFileDirectory(String id)
            throws WebloggerException {
        return (MediaFileDirectory) this.persistenceStrategy.load(MediaFileDirectory.class, id);
    }

    /**
     * {@inheritDoc}
     */
    public MediaFileDirectory getMediaFileRootDirectory(Weblog weblog)
            throws WebloggerException {
        Query q = this.persistenceStrategy.getNamedQuery("MediaFileDirectory.getByWeblogAndNoParent");
        q.setParameter(1, weblog);
        try {
            return (MediaFileDirectory) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<MediaFileDirectory> getMediaFileDirectories(Weblog weblog)
            throws WebloggerException {

        Query q = this.persistenceStrategy.getNamedQuery("MediaFileDirectory.getByWeblog");
        q.setParameter(1, weblog);
        return q.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public void removeMediaFile(Weblog weblog, MediaFile mediaFile)
            throws WebloggerException {
        FileContentManager cmgr = WebloggerFactory.getWeblogger().getFileContentManager();

        this.persistenceStrategy.remove(mediaFile);
        // update weblog last modified date.  date updated by saveWeblog()
        roller.getWeblogManager().saveWeblog(weblog);

        try {
            cmgr.deleteFile(weblog, mediaFile.getId());
        } catch (FileNotFoundException e) {
            log.debug("File to be deleted already unavailable in the file store");
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<MediaFile> fetchRecentPublicMediaFiles(int length)
            throws WebloggerException {

        List<Object> params = new ArrayList<Object>();
        int size = 0;
        StringBuffer queryString = new StringBuffer();

        queryString.append("SELECT m FROM MediaFile m WHERE m.sharedForGallery = true");
        queryString.append(" order by m.dateUploaded");
        Query query = persistenceStrategy.getDynamicQuery(queryString.toString());
        query.setFirstResult(0);
        query.setMaxResults(length);
        return query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public List<MediaFile> searchMediaFiles(Weblog weblog, MediaFileFilter filter)
            throws WebloggerException {

        List<Object> params = new ArrayList<Object>();
        int size = 0;
        StringBuffer queryString = new StringBuffer();
        StringBuffer whereClause = new StringBuffer();
        StringBuffer orderBy = new StringBuffer();

        queryString.append("SELECT m FROM MediaFile m WHERE ");

        params.add(size++, weblog);
        whereClause.append("m.directory.weblog = ?" + size);

        if (!StringUtils.isEmpty(filter.getName())) {
            String nameFilter = filter.getName();
            nameFilter = nameFilter.trim();
            if (!nameFilter.endsWith("%")) {
                nameFilter = nameFilter + "%";
            }
            params.add(size++, nameFilter);
            whereClause.append(" AND m.name like ?" + size);
        }

        if (filter.getSize() > 0) {
            params.add(size++, filter.getSize());
            whereClause.append(" AND m.length ");
            switch (filter.getSizeFilterType()) {
                case GT:
                    whereClause.append(">");
                    break;
                case GTE:
                    whereClause.append(">=");
                    break;
                case EQ:
                    whereClause.append("=");
                    break;
                case LT:
                    whereClause.append("<");
                    break;
                case LTE:
                    whereClause.append("<=");
                    break;
                default:
                    whereClause.append("=");
                    break;
            }
            whereClause.append(" ?" + size);
        }

        if (filter.getTags() != null) {
            whereClause.append(" AND EXISTS (SELECT t FROM MediaFileTag t WHERE t.mediaFile = m and t.name IN (");
            for (String tag : filter.getTags()) {
                params.add(size++, tag);
                whereClause.append("?").append(size).append(",");
            }
            whereClause.deleteCharAt(whereClause.lastIndexOf(","));
            whereClause.append("))");
        }

        if (filter.getType() != null) {
            if (filter.getType() == MediaFileType.OTHERS) {
                for (MediaFileType type : MediaFileType.values()) {
                    if (type != MediaFileType.OTHERS) {
                        params.add(size++, type.getContentTypePrefix() + "%");
                        whereClause.append(" AND m.contentType not like ?" + size);
                    }
                }
            } else {
                params.add(size++, filter.getType().getContentTypePrefix() + "%");
                whereClause.append(" AND m.contentType like ?" + size);
            }
        }

        if (filter.getOrder() != null) {
            switch (filter.getOrder()) {
                case NAME:
                    orderBy.append(" order by m.name");
                    break;
                case DATE_UPLOADED:
                    orderBy.append(" order by m.dateUploaded");
                    break;
                case TYPE:
                    orderBy.append(" order by m.contentType");
                    break;
                default:
            }
        }

        Query query = persistenceStrategy.getDynamicQuery(queryString.toString() + whereClause.toString() + orderBy.toString());
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }

        if (filter.getStartIndex() >= 0) {
            query.setFirstResult(filter.getStartIndex());
            query.setMaxResults(filter.getLength());
        }

        return query.getResultList();
    }

    /**
     * Does mediafile storage require any upgrading;
     * checks for existance of migration status file.
     */
    public boolean isFileStorageUpgradeRequired() {
        String uploadsDirName = WebloggerConfig.getProperty("uploads.dir");
        if (uploadsDirName != null) {
            File uploadsDir = new File(uploadsDirName);
            if (uploadsDir.exists() && uploadsDir.isDirectory()) {
                Properties props = new Properties();
                try {
                    props.load(new FileReader(uploadsDirName
                        + File.separator + MIGRATIION_STATUS_FILENAME));
                } catch (Exception ignored) {}
                if (props.getProperty("complete") != null) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Run mediafile storage upgrade, copying files to new storage system;
     * creates migration status file only when work is complete.
     */
    public List<String> upgradeFileStorage() {
        List<String> msgs = new ArrayList<String>();
        String oldDirName = WebloggerConfig.getProperty("uploads.dir");
        String FS = File.separator;

        if (oldDirName != null) {
            try {

                // loop through weblogs found in uploads directory
                File uploadsDir = new File(oldDirName);
                File[] dirs = uploadsDir.listFiles();
                for (int i=0; i<dirs.length; i++) {

                    if (dirs[i].isDirectory()) {
                        WeblogManager wmgr = this.roller.getWeblogManager(); 
                        Weblog weblog = wmgr.getWeblogByHandle(dirs[i].getName(), null);
                        if (weblog != null) {

                            log.info("Migrating weblog: " + weblog.getHandle());
                            
                            // use 1st admin user found as file creator
                            List<User> users = wmgr.getWeblogUsers(weblog, true);
                            User chosenUser = users.get(0);
                            for (User user: users) {
                                chosenUser = user;
                                if (user.hasGlobalPermission("admin")) {
                                    break;
                                }
                            }

                            try {
                                // create weblog's mediafile directory if needed
                                MediaFileDirectory root =
                                    this.getMediaFileRootDirectory(weblog);
                                if (root == null) {
                                    root = this.createRootMediaFileDirectory(weblog);
                                    roller.flush();
                                }

                                // upgrade!
                                upgradeUploadsDir(weblog, chosenUser,
                                  new File(oldDirName + FS + dirs[i].getName()),
                                  root);

                            } catch (Throwable t) {
                                log.error("ERROR upgading weblog", t);
                            }
                        }
                    }
                }

                Properties props = new Properties();
                props.setProperty("complete", "true");
                props.store(new FileOutputStream(oldDirName
                    + File.separator + MIGRATIION_STATUS_FILENAME),
                    "Migration is complete!");

            } catch (Exception ioex) {}
        }
        msgs.add("Migration complete!");
        return msgs;
    }

    private void upgradeUploadsDir(Weblog weblog, User user, File oldDir, MediaFileDirectory newDir) {
        log.debug("Upgrading dir: " + oldDir.getAbsolutePath());
        if (newDir == null) {
            log.error("newDir cannot be null");
            return;
        }

        // loop through files and directories
        int dirCount = 0;
        int fileCount = 0;
        File[] files = oldDir.listFiles();
        for (int i=0; i<files.length; i++) {

            // a directory: go recursive
            if (files[i].isDirectory()) {

                if (newDir.hasDirectory(files[i].getName())) {
                    // already have a mediafile directory for that
                    upgradeUploadsDir(weblog, user, files[i],
                        newDir.getChildDirectory(files[i].getName()));

                } else {
                    // need to create a new mediafile directory
                    MediaFileDirectory subDir = null;
                    try {
                        subDir = newDir.createNewDirectory(files[i].getName());
                        roller.getMediaFileManager().createMediaFileDirectory(subDir);
                        roller.flush();

                        dirCount++;

                    } catch (WebloggerException ex) {
                        log.error("ERROR creating directory: "
                            + newDir.getPath() + "/" + files[i].getName());
                    }
                    upgradeUploadsDir(weblog, user, files[i], subDir);
                }

            } else { // a file: create a database record for it

                // check to make sure that file does not already exist
                if (newDir.hasMediaFile(files[i].getName())) {
                    log.debug("    Skipping file that already exists: "
                        + files[i].getName());
                
                } else {

                    String originalPath = 
                        ("/".equals(newDir.getPath()) ? "" : newDir.getPath()) + files[i].getName();
                    log.debug("    Upgrade file with original path: " + originalPath);
                    MediaFile mf = new MediaFile();
                    try {
                        mf.setName(files[i].getName());
                        mf.setDescription(files[i].getName());
                        mf.setOriginalPath(originalPath);

                        mf.setDateUploaded(new Timestamp(files[i].lastModified()));
                        mf.setLastUpdated(new Timestamp(files[i].lastModified()));

                        mf.setDirectory(newDir);
                        mf.setCreatorUserName(user.getUserName());
                        mf.setSharedForGallery(Boolean.FALSE);

                        mf.setLength(files[i].length());
                        mf.setInputStream(new FileInputStream(files[i]));
                        mf.setContentType(Utilities.getContentTypeFromFileName(files[i].getName()));

                        this.roller.getMediaFileManager().createMediaFile(weblog, mf);

                        fileCount++;

                    } catch (WebloggerException ex) {
                        log.error("ERROR writing file to new storage system: "
                                + files[i].getAbsolutePath(), ex);

                    } catch (java.io.FileNotFoundException ex) {
                        log.error("ERROR reading file from old storage system: "
                                + files[i].getAbsolutePath(), ex);
                    }
                }
            }
        }

        try { // flush changes to this directory
            roller.flush();

            log.debug("Count of dirs  created: " + dirCount);
            log.debug("Count of files created: " + fileCount);

        } catch (WebloggerException ex) {
            log.error("ERROR flushing changes to dir: " + newDir.getPath(), ex);
        }
    }

    public void removeAllFiles(Weblog website) throws WebloggerException {
        removeMediaFileDirectory(getMediaFileRootDirectory(website));
    }

    private void removeMediaFileDirectory(MediaFileDirectory dir) 
            throws WebloggerException {

        if (dir == null) return;
        
        Set<MediaFile> files = dir.getMediaFiles();
        for (MediaFile mf : files) {
            this.removeMediaFile(dir.getWeblog(), mf);
        }
        Set<MediaFileDirectory> dirs = dir.getChildDirectories();
        for (MediaFileDirectory md : dirs) {
            removeMediaFileDirectory(md);
        }
        this.persistenceStrategy.remove(dir);
    }
}
