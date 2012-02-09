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

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.roller.weblogger.business.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;

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
    private final JPAPersistenceStrategy strategy;
    private static Log log =
            LogFactory.getFactory().getInstance(JPAMediaFileManagerImpl.class);
    public static final String MIGRATION_STATUS_FILENAME = "migration-status.properties";

    /**
     * Creates a new instance of MediaFileManagerImpl
     */
    @com.google.inject.Inject
    protected JPAMediaFileManagerImpl(Weblogger roller, JPAPersistenceStrategy persistenceStrategy) {
        this.roller = roller;
        this.strategy = persistenceStrategy;
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
    public void moveMediaFileDirectories(Collection<MediaFileDirectory> mediaFileDirs, MediaFileDirectory targetDir)
            throws WebloggerException {

        for (MediaFileDirectory mediaFileDir : mediaFileDirs) {
            mediaFileDir.setParent(targetDir);
            this.strategy.store(mediaFileDir);
        }
        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWeblog(targetDir.getWeblog());
    }

    /**
     * {@inheritDoc}
     */
    public void moveMediaFiles(Collection<MediaFile> mediaFiles, MediaFileDirectory targetDirectory)
            throws WebloggerException {

        List<MediaFile> moved = new ArrayList<MediaFile>();
        moved.addAll(mediaFiles);
        
        for (MediaFile mediaFile : moved) {
            mediaFile.getDirectory().getMediaFiles().remove(mediaFile);

            mediaFile.setDirectory(targetDirectory);
            this.strategy.store(mediaFile);

            targetDirectory.getMediaFiles().add(mediaFile);
            this.strategy.store(targetDirectory);
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
    public void moveMediaFileDirectory(MediaFileDirectory mediaFileDir, MediaFileDirectory targetDirectory)
            throws WebloggerException {
        moveMediaFileDirectories(Arrays.asList(mediaFileDir), targetDirectory);
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
        this.strategy.store(directory);

        // update weblog last modified date.  date updated by saveWebsite()
        roller.getWeblogManager().saveWeblog(directory.getWeblog());
    }

    /**
     * {@inheritDoc}
     */
    public MediaFileDirectory createMediaFileDirectoryByPath(Weblog weblog, String requestedPath)
            throws WebloggerException {

        String path = requestedPath;
        log.debug("Creating dir: " + path);

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.equals("")) {
            // Root cannot be created using this method.
            // Use createRootMediaFileDirectory instead
            throw new WebloggerException("Invalid path!");
        }

        int lastPathIndex = path.lastIndexOf("/");

        MediaFileDirectory newDirectory = null;
        if (lastPathIndex == -1) {

            // Directory needs to be created under root
            MediaFileDirectory root = getMediaFileRootDirectory(weblog);

            if (root.hasDirectory(path)) {
                throw new WebloggerException("Directory exists");
            } else {
                log.debug("    Created dir under ROOT");
                newDirectory = root.createNewDirectory(path);
            }

        } else {

            boolean created = false;

            MediaFileDirectory base = getMediaFileRootDirectory(weblog);
            String token = null;
            String pathpart = "";
            StringTokenizer toker = new StringTokenizer(path, "/");
            while (toker.hasMoreTokens()) {
                token = toker.nextToken();
                if (!pathpart.endsWith("/")) {
                    pathpart += "/" + token;
                } else {
                    pathpart += token;
                }
                MediaFileDirectory possibleBase = getMediaFileDirectoryByPath(weblog, pathpart);
                if (possibleBase == null) {
                    base = base.createNewDirectory(token);
                    log.debug("   Created new directory: " + base.getPath());
                    created = true;
                    roller.flush();
                } else {
                    base = possibleBase;
                }
            }
            if (!created || !requestedPath.equals(base.getPath())) {
                throw new WebloggerException("ERROR directory not created");
            }
            newDirectory = base;
        }

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
    public void createMediaFile(Weblog weblog, MediaFile mediaFile, RollerMessages errors) throws WebloggerException {

        FileContentManager cmgr = WebloggerFactory.getWeblogger().getFileContentManager();
        if (!cmgr.canSave(weblog, mediaFile.getName(), mediaFile.getContentType(), mediaFile.getLength(), errors)) {
            return;
        }
        strategy.store(mediaFile);

        // update weblog last modified date.  date updated by saveWeblog()
        roller.getWeblogManager().saveWeblog(weblog);

        cmgr.saveFileContent(weblog, mediaFile.getId(), mediaFile.getInputStream());

        if (mediaFile.isImageFile()) {
            updateThumbnail(mediaFile);
        }
    }

    private void updateThumbnail(MediaFile mediaFile) {
        try {
            FileContentManager cmgr = WebloggerFactory.getWeblogger().getFileContentManager();
            FileContent fc = cmgr.getFileContent(mediaFile.getWeblog(), mediaFile.getId());
            BufferedImage img = null;

            img = ImageIO.read(fc.getInputStream());

            // determine and save width and height
            mediaFile.setWidth(img.getWidth());
            mediaFile.setHeight(img.getHeight());
            strategy.store(mediaFile);

            int newWidth = mediaFile.getThumbnailWidth();
            int newHeight = mediaFile.getThumbnailHeight();

            // create thumbnail image
            Image newImage = img.getScaledInstance(
                    newWidth, newHeight, Image.SCALE_SMOOTH);
            BufferedImage tmp = new BufferedImage(
                    newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = tmp.createGraphics();
            g2.drawImage(newImage, 0, 0, newWidth, newHeight, null);
            g2.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(tmp, "png", baos);

            cmgr.saveFileContent(mediaFile.getWeblog(), mediaFile.getId() + "_sm",
                    new ByteArrayInputStream(baos.toByteArray()));

        } catch (Exception e) {
            log.debug("ERROR creating thumbnail", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void updateMediaFile(Weblog weblog, MediaFile mediaFile) throws WebloggerException {
        mediaFile.setLastUpdated(new Timestamp(System.currentTimeMillis()));
        strategy.store(mediaFile);
        // update weblog last modified date.  date updated by saveWeblog()
        roller.getWeblogManager().saveWeblog(weblog);
    }

    /**
     * {@inheritDoc}
     */
    public void updateMediaFile(Weblog weblog, MediaFile mediaFile, InputStream is) throws WebloggerException {
        mediaFile.setLastUpdated(new Timestamp(System.currentTimeMillis()));
        strategy.store(mediaFile);

        // update weblog last modified date.  date updated by saveWeblog()
        roller.getWeblogManager().saveWeblog(weblog);

        FileContentManager cmgr = WebloggerFactory.getWeblogger().getFileContentManager();
        RollerMessages msgs = new RollerMessages();
        if (!cmgr.canSave(weblog, mediaFile.getName(), mediaFile.getContentType(), mediaFile.getLength(), msgs)) {
            throw new FileIOException(msgs.toString());
        }
        cmgr.saveFileContent(weblog, mediaFile.getId(), is);

        if (mediaFile.isImageFile()) {
            updateThumbnail(mediaFile);
        }
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
        MediaFile mediaFile = (MediaFile) this.strategy.load(MediaFile.class, id);
        if (includeContent) {
            FileContentManager cmgr = WebloggerFactory.getWeblogger().getFileContentManager();

            FileContent content = cmgr.getFileContent(mediaFile.getDirectory().getWeblog(), id);
            mediaFile.setContent(content);

            try {
                FileContent thumbnail = cmgr.getFileContent(mediaFile.getDirectory().getWeblog(), id + "_sm");
                mediaFile.setThumbnailContent(thumbnail);

            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Cannot load thumbnail for image " + id, e);
                } else {
                    log.warn("Cannot load thumbnail for image " + id);
                }
            }
        }
        return mediaFile;
    }

    /**
     * {@inheritDoc}
     */
    public MediaFileDirectory getMediaFileDirectoryByPath(Weblog weblog, String path)
            throws WebloggerException {

        path = !path.startsWith("/") ? "/" + path : path;

        log.debug("Looking up weblog|path: " + weblog.getHandle() + "|" + path);

        Query q = this.strategy.getNamedQuery(
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
        String fileName = path;
        MediaFileDirectory mdir = null;
        int slash = path.lastIndexOf("/");
        if (slash > 0) {
            mdir = getMediaFileDirectoryByPath(weblog, path.substring(0, slash));
        } else {
            mdir = getMediaFileRootDirectory(weblog);
        }
        if (slash != -1) {
            fileName = fileName.substring(slash + 1);
        }
        return mdir.getMediaFile(fileName);
    }

    /**
     * {@inheritDoc}
     */
    public MediaFile getMediaFileByOriginalPath(Weblog weblog, String origpath)
            throws WebloggerException {
                
        if (null == origpath) return null;

        if (!origpath.startsWith("/")) {
            origpath = "/" + origpath;
        }

        Query q = this.strategy.getNamedQuery(
                "MediaFile.getByWeblogAndOrigpath");
        q.setParameter(1, weblog);
        q.setParameter(2, origpath);
        MediaFile mf = null;
        try {
            mf = (MediaFile) q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
        FileContentManager cmgr = WebloggerFactory.getWeblogger().getFileContentManager();
        FileContent content = cmgr.getFileContent(mf.getDirectory().getWeblog(), mf.getId());
        mf.setContent(content);
        return mf;
    }

    /**
     * {@inheritDoc}
     */
    public MediaFileDirectory getMediaFileDirectory(String id)
            throws WebloggerException {
        return (MediaFileDirectory) this.strategy.load(MediaFileDirectory.class, id);
    }

    /**
     * {@inheritDoc}
     */
    public MediaFileDirectory getMediaFileRootDirectory(Weblog weblog)
            throws WebloggerException {
        Query q = this.strategy.getNamedQuery("MediaFileDirectory.getByWeblogAndNoParent");
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

        Query q = this.strategy.getNamedQuery("MediaFileDirectory.getByWeblog");
        q.setParameter(1, weblog);
        return q.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public void removeMediaFile(Weblog weblog, MediaFile mediaFile)
            throws WebloggerException {
        FileContentManager cmgr = WebloggerFactory.getWeblogger().getFileContentManager();

        this.strategy.remove(mediaFile);
        // update weblog last modified date.  date updated by saveWeblog()
        roller.getWeblogManager().saveWeblog(weblog);

        try {
            cmgr.deleteFile(weblog, mediaFile.getId());
            // Now thumbnail
            cmgr.deleteFile(weblog, mediaFile.getId() + "_sm");
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
        StringBuilder queryString = new StringBuilder();

        queryString.append("SELECT m FROM MediaFile m WHERE m.sharedForGallery = true");
        queryString.append(" order by m.dateUploaded");
        Query query = strategy.getDynamicQuery(queryString.toString());
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
        StringBuilder queryString = new StringBuilder();
        StringBuilder whereClause = new StringBuilder();
        StringBuilder orderBy = new StringBuilder();

        queryString.append("SELECT m FROM MediaFile m WHERE ");

        params.add(size++, weblog);
        whereClause.append("m.directory.weblog = ?").append(size);

        if (!StringUtils.isEmpty(filter.getName())) {
            String nameFilter = filter.getName();
            nameFilter = nameFilter.trim();
            if (!nameFilter.endsWith("%")) {
                nameFilter = nameFilter + "%";
            }
            params.add(size++, nameFilter);
            whereClause.append(" AND m.name like ?").append(size);
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
            whereClause.append(" ?").append(size);
        }

        if (filter.getTags() != null && filter.getTags().size() > 1) {
            whereClause.append(" AND EXISTS (SELECT t FROM MediaFileTag t WHERE t.mediaFile = m and t.name IN (");
            for (String tag : filter.getTags()) {
                params.add(size++, tag);
                whereClause.append("?").append(size).append(",");
            }
            whereClause.deleteCharAt(whereClause.lastIndexOf(","));
            whereClause.append("))");
        } else if (filter.getTags() != null && filter.getTags().size() == 1) {
            params.add(size++, filter.getTags().get(0));
            whereClause.append(" AND EXISTS (SELECT t FROM MediaFileTag t WHERE t.mediaFile = m and t.name = ?").append(size).append(")");
        }

        if (filter.getType() != null) {
            if (filter.getType() == MediaFileType.OTHERS) {
                for (MediaFileType type : MediaFileType.values()) {
                    if (type != MediaFileType.OTHERS) {
                        params.add(size++, type.getContentTypePrefix() + "%");
                        whereClause.append(" AND m.contentType not like ?").append(size);
                    }
                }
            } else {
                params.add(size++, filter.getType().getContentTypePrefix() + "%");
                whereClause.append(" AND m.contentType like ?").append(size);
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
        } else {
            orderBy.append(" order by m.name");
        }

        Query query = strategy.getDynamicQuery(queryString.toString() + whereClause.toString() + orderBy.toString());
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
     * checks for existence of migration status file.
     */
    public boolean isFileStorageUpgradeRequired() {
        String uploadsDirName = WebloggerConfig.getProperty("uploads.dir");
        if (uploadsDirName != null) {
            File uploadsDir = new File(uploadsDirName);
            if (uploadsDir.exists() && uploadsDir.isDirectory()) {
                Properties props = new Properties();
                try {
                    props.load(new FileInputStream(uploadsDirName
                            + File.separator + MIGRATION_STATUS_FILENAME));

                } catch (IOException ex) {
                    return true;
                }
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
                if (null != dirs) {
                    for (int i = 0; i < dirs.length; i++) {

                        if (dirs[i].isDirectory()) {
                            WeblogManager wmgr = this.roller.getWeblogManager();
                            Weblog weblog = wmgr.getWeblogByHandle(dirs[i].getName(), null);
                            if (weblog != null) {

                                log.info("Migrating weblog: " + weblog.getHandle());

                                // use 1st admin user found as file creator
                                List<User> users = wmgr.getWeblogUsers(weblog, true);
                                User chosenUser = users.get(0);
                                for (User user : users) {
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
                }

                Properties props = new Properties();
                props.setProperty("complete", "true");
                props.store(new FileOutputStream(oldDirName
                        + File.separator + MIGRATION_STATUS_FILENAME),
                        "Migration is complete!");

            } catch (Exception ioex) {
                log.error("ERROR upgrading", ioex);
            }
        }
        msgs.add("Migration complete!");
        return msgs;
    }

    private void upgradeUploadsDir(Weblog weblog, User user, File oldDir, MediaFileDirectory newDir) {
        RollerMessages messages = new RollerMessages();

        log.debug("Upgrading dir: " + oldDir.getAbsolutePath());
        if (newDir == null) {
            log.error("newDir cannot be null");
            return;
        }

        // loop through files and directories
        int dirCount = 0;
        int fileCount = 0;
        File[] files = oldDir.listFiles();
        for (int i = 0; i < files.length; i++) {

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
                        newDir.getChildDirectories().add(subDir);
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
                            ("/".equals(newDir.getPath()) ? "" : newDir.getPath()) + "/" + files[i].getName();
                    log.debug("    Upgrade file with original path: " + originalPath);
                    MediaFile mf = new MediaFile();
                    try {
                        mf.setName(files[i].getName());
                        mf.setDescription(files[i].getName());
                        mf.setOriginalPath(originalPath);

                        mf.setDateUploaded(new Timestamp(files[i].lastModified()));
                        mf.setLastUpdated(new Timestamp(files[i].lastModified()));

                        mf.setDirectory(newDir);
                        mf.setWeblog(weblog);
                        mf.setCreatorUserName(user.getUserName());
                        mf.setSharedForGallery(Boolean.FALSE);

                        mf.setLength(files[i].length());
                        mf.setInputStream(new FileInputStream(files[i]));
                        mf.setContentType(Utilities.getContentTypeFromFileName(files[i].getName()));
                        newDir.getMediaFiles().add(mf);

                        this.roller.getMediaFileManager().createMediaFile(weblog, mf, messages);
                        log.info(messages.toString());

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

    public void removeMediaFileDirectory(MediaFileDirectory dir)
            throws WebloggerException {
        if (dir == null) {
            return;
        }
        FileContentManager cmgr = WebloggerFactory.getWeblogger().getFileContentManager();
        Set<MediaFile> files = dir.getMediaFiles();
        for (MediaFile mf : files) {
            try {
                cmgr.deleteFile(dir.getWeblog(), mf.getId());
            } catch (FileNotFoundException e) {
                log.debug("File to be deleted already unavailable in the file store");
            }
            this.strategy.remove(mf);
        }
        Set<MediaFileDirectory> dirs = dir.getChildDirectories();
        for (MediaFileDirectory md : dirs) {
            removeMediaFileDirectory(md);
        }
        this.strategy.remove(dir);
    }
}
