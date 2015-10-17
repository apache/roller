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
package org.apache.roller.weblogger.business.jpa;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.FileContentManager;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.FileContent;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.MediaFileFilter;
import org.apache.roller.weblogger.pojos.MediaFileType;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.RollerMessages;

import javax.imageio.ImageIO;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public class JPAMediaFileManagerImpl implements MediaFileManager {

    private final JPAPersistenceStrategy strategy;
    private static Log log = LogFactory.getFactory().getInstance(
            JPAMediaFileManagerImpl.class);

    /**
     * Creates a new instance of MediaFileManagerImpl
     */
    protected JPAMediaFileManagerImpl(JPAPersistenceStrategy persistenceStrategy) {
        this.strategy = persistenceStrategy;
    }

    /**
     * Initialize manager
     */
    public void initialize() {
    }

    /**
     * Release resources; currently a no-op.
     */
    public void release() {
    }

    /**
     * {@inheritDoc}
     */
    public void moveMediaFiles(Collection<MediaFile> mediaFiles,
            MediaFileDirectory targetDirectory) throws WebloggerException {

        List<MediaFile> moved = new ArrayList<>();
        moved.addAll(mediaFiles);

        for (MediaFile mediaFile : moved) {
            mediaFile.getDirectory().getMediaFiles().remove(mediaFile);

            mediaFile.setDirectory(targetDirectory);
            this.strategy.store(mediaFile);

            targetDirectory.getMediaFiles().add(mediaFile);
            this.strategy.store(targetDirectory);
        }

        // Refresh associated parent for changes
        strategy.flush();
        if (moved.size() > 0) {
            strategy.refresh(moved.get(0).getDirectory());
        }

        // Refresh associated parent for changes
        strategy.refresh(targetDirectory);
    }

    /**
     * {@inheritDoc}
     */
    public void moveMediaFile(MediaFile mediaFile,
            MediaFileDirectory targetDirectory) throws WebloggerException {
        moveMediaFiles(Arrays.asList(mediaFile), targetDirectory);
    }

    /**
     * {@inheritDoc}
     */
    public void createMediaFileDirectory(MediaFileDirectory directory)
            throws WebloggerException {
        this.strategy.store(directory);
    }

    /**
     * {@inheritDoc}
     */
    public MediaFileDirectory createMediaFileDirectory(Weblog weblog,
            String requestedName) throws WebloggerException {

        requestedName = requestedName.startsWith("/") ? requestedName.substring(1) : requestedName;

        if (requestedName.equals("") || requestedName.equals("default")) {
            // Default cannot be created using this method.
            // Use createDefaultMediaFileDirectory instead
            throw new WebloggerException("Invalid name!");
        }

        MediaFileDirectory newDirectory;

        if (weblog.hasMediaFileDirectory(requestedName)) {
            throw new WebloggerException("Directory exists");
        } else {
            newDirectory = new MediaFileDirectory(weblog, requestedName, null);
            log.debug("Created new Directory " + requestedName);
        }
        return newDirectory;
    }

    /**
     * {@inheritDoc}
     */
    public MediaFileDirectory createDefaultMediaFileDirectory(Weblog weblog)
            throws WebloggerException {
        MediaFileDirectory defaultDirectory = new MediaFileDirectory(weblog, "default",
                "default directory");
        createMediaFileDirectory(defaultDirectory);
        return defaultDirectory;
    }

    /**
     * {@inheritDoc}
     */
    public void createMediaFile(Weblog weblog, MediaFile mediaFile,
            RollerMessages errors) throws WebloggerException {

        try {
            FileContentManager cmgr = WebloggerFactory.getWeblogger()
                    .getFileContentManager();
            if (!cmgr.canSave(weblog, mediaFile.getName(),
                    mediaFile.getContentType(), mediaFile.getLength(), errors)) {
                return;
            }
            strategy.store(mediaFile);

            // Refresh associated parent for changes
            strategy.flush();
            strategy.refresh(mediaFile.getDirectory());

            cmgr.saveFileContent(weblog, mediaFile.getId(),
                    mediaFile.getInputStream());

            if (mediaFile.isImageFile()) {
                updateThumbnail(mediaFile);
            }
        } catch (IOException e) {
            throw new WebloggerException(e);
        }
    }

    private void updateThumbnail(MediaFile mediaFile) {
        try {
            FileContentManager cmgr = WebloggerFactory.getWeblogger()
                    .getFileContentManager();
            FileContent fc = cmgr.getFileContent(mediaFile.getDirectory().getWeblog(),
                    mediaFile.getId());
            BufferedImage img;

            img = ImageIO.read(fc.getInputStream());

            // determine and save width and height
            mediaFile.setWidth(img.getWidth());
            mediaFile.setHeight(img.getHeight());
            strategy.store(mediaFile);

            int newWidth = mediaFile.getThumbnailWidth();
            int newHeight = mediaFile.getThumbnailHeight();

            // create thumbnail image
            Image newImage = img.getScaledInstance(newWidth, newHeight,
                    Image.SCALE_SMOOTH);
            BufferedImage tmp = new BufferedImage(newWidth, newHeight,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = tmp.createGraphics();
            g2.drawImage(newImage, 0, 0, newWidth, newHeight, null);
            g2.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(tmp, "png", baos);

            cmgr.saveFileContent(mediaFile.getDirectory().getWeblog(), mediaFile.getId()
                    + "_sm", new ByteArrayInputStream(baos.toByteArray()));

            strategy.flush();
            // Refresh associated parent for changes
            strategy.refresh(mediaFile.getDirectory());

        } catch (Exception e) {
            log.debug("ERROR creating thumbnail", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void updateMediaFile(Weblog weblog, MediaFile mediaFile)
            throws WebloggerException {
        mediaFile.setLastUpdated(new Timestamp(System.currentTimeMillis()));
        strategy.store(mediaFile);

        strategy.flush();
        // Refresh associated parent for changes
        strategy.refresh(mediaFile.getDirectory());

        updateWeblogLastModifiedDate(weblog);
    }

    /**
     * {@inheritDoc}
     */
    public void updateMediaFile(Weblog weblog, MediaFile mediaFile,
            InputStream is) throws WebloggerException {
        try {
            mediaFile.setLastUpdated(new Timestamp(System.currentTimeMillis()));
            strategy.store(mediaFile);

            strategy.flush();
            // Refresh associated parent for changes
            strategy.refresh(mediaFile.getDirectory());

            updateWeblogLastModifiedDate(weblog);

            FileContentManager cmgr = WebloggerFactory.getWeblogger()
                    .getFileContentManager();
            RollerMessages msgs = new RollerMessages();
            if (!cmgr.canSave(weblog, mediaFile.getName(),
                    mediaFile.getContentType(), mediaFile.getLength(), msgs)) {
                throw new IOException(msgs.toString());
            }
            cmgr.saveFileContent(weblog, mediaFile.getId(), is);

            if (mediaFile.isImageFile()) {
                updateThumbnail(mediaFile);
            }
        } catch (IOException e) {
            throw new WebloggerException(e);
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
    public MediaFile getMediaFile(String id, boolean includeContent)
            throws WebloggerException {

        try {
            MediaFile mediaFile = this.strategy.load(MediaFile.class, id);
            if (includeContent) {
                FileContentManager cmgr = WebloggerFactory.getWeblogger()
                        .getFileContentManager();

                FileContent content = cmgr.getFileContent(mediaFile.getDirectory()
                        .getWeblog(), id);
                mediaFile.setContent(content);

                try {
                    FileContent thumbnail = cmgr.getFileContent(mediaFile
                            .getDirectory().getWeblog(), id + "_sm");
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
        } catch (IOException e) {
            throw new WebloggerException(e);
        }

    }

    /**
     * {@inheritDoc}
     */
    public MediaFileDirectory getMediaFileDirectoryByName(Weblog weblog,
            String name) throws WebloggerException {

        name = name.startsWith("/") ? name.substring(1) : name;

        log.debug("Looking up weblog|media file directory: " + weblog.getHandle() + "|" + name);

        TypedQuery<MediaFileDirectory> q = this.strategy
                .getNamedQuery("MediaFileDirectory.getByWeblogAndName", MediaFileDirectory.class);
        q.setParameter(1, weblog);
        q.setParameter(2, name);
        try {
            return q.getSingleResult();
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
        MediaFileDirectory mdir;
        int slash = path.lastIndexOf('/');
        if (slash > 0) {
            mdir = getMediaFileDirectoryByName(weblog, path.substring(0, slash));
        } else {
            mdir = getDefaultMediaFileDirectory(weblog);
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

        try {
            if (null == origpath) {
                return null;
            }

            if (!origpath.startsWith("/")) {
                origpath = "/" + origpath;
            }

            TypedQuery<MediaFile> q = this.strategy
                    .getNamedQuery("MediaFile.getByWeblogAndOrigpath", MediaFile.class);
            q.setParameter(1, weblog);
            q.setParameter(2, origpath);
            MediaFile mf;
            try {
                mf = q.getSingleResult();
            } catch (NoResultException e) {
                return null;
            }
            FileContentManager cmgr = WebloggerFactory.getWeblogger()
                    .getFileContentManager();
            FileContent content = cmgr.getFileContent(
                    mf.getDirectory().getWeblog(), mf.getId());
            mf.setContent(content);
            return mf;
        } catch (IOException e) {
            throw new WebloggerException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public MediaFileDirectory getMediaFileDirectory(String id)
            throws WebloggerException {
        return this.strategy.load(MediaFileDirectory.class, id);
    }

    /**
     * {@inheritDoc}
     */
    public MediaFileDirectory getDefaultMediaFileDirectory(Weblog weblog)
            throws WebloggerException {
        return getMediaFileDirectoryByName(weblog, "default");
    }

    /**
     * {@inheritDoc}
     */
    public List<MediaFileDirectory> getMediaFileDirectories(Weblog weblog)
            throws WebloggerException {

        TypedQuery<MediaFileDirectory> q = this.strategy.getNamedQuery("MediaFileDirectory.getByWeblog",
                MediaFileDirectory.class);
        q.setParameter(1, weblog);
        return q.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public void removeMediaFile(Weblog weblog, MediaFile mediaFile)
            throws WebloggerException {
        FileContentManager cmgr = WebloggerFactory.getWeblogger()
                .getFileContentManager();

        this.strategy.remove(mediaFile);

        // Refresh associated parent for changes
        strategy.refresh(mediaFile.getDirectory());

        try {
            cmgr.deleteFile(weblog, mediaFile.getId());
            // Now thumbnail
            cmgr.deleteFile(weblog, mediaFile.getId() + "_sm");
        } catch (Exception e) {
            log.debug("File to be deleted already unavailable in the file store");
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<MediaFile> fetchRecentPublicMediaFiles(int length)
            throws WebloggerException {

        String queryString = "SELECT m FROM MediaFile m WHERE m.sharedForGallery = true order by m.dateUploaded";
        TypedQuery<MediaFile> query = strategy.getDynamicQuery(queryString, MediaFile.class);
        query.setFirstResult(0);
        query.setMaxResults(length);
        return query.getResultList();
    }

    /**
     * {@inheritDoc}
     */
    public List<MediaFile> searchMediaFiles(Weblog weblog,
            MediaFileFilter filter) throws WebloggerException {

        List<Object> params = new ArrayList<>();
        int size = 0;
        String queryString = "SELECT m FROM MediaFile m WHERE ";
        StringBuilder whereClause = new StringBuilder();
        StringBuilder orderBy = new StringBuilder();

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

        if (filter.getType() != null) {
            if (filter.getType() == MediaFileType.OTHERS) {
                for (MediaFileType type : MediaFileType.values()) {
                    if (type != MediaFileType.OTHERS) {
                        params.add(size++, type.getContentTypePrefix() + "%");
                        whereClause.append(" AND m.contentType not like ?")
                                .append(size);
                    }
                }
            } else {
                params.add(size++, filter.getType().getContentTypePrefix()
                        + "%");
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

        TypedQuery<MediaFile> query = strategy.getDynamicQuery(queryString
                + whereClause.toString() + orderBy.toString(), MediaFile.class);
        for (int i = 0; i < params.size(); i++) {
            query.setParameter(i + 1, params.get(i));
        }

        if (filter.getStartIndex() >= 0) {
            query.setFirstResult(filter.getStartIndex());
            query.setMaxResults(filter.getLength());
        }
        return query.getResultList();
    }

    public void removeAllFiles(Weblog website) throws WebloggerException {
        removeMediaFileDirectory(getDefaultMediaFileDirectory(website));
    }

    public void removeMediaFileDirectory(MediaFileDirectory dir)
            throws WebloggerException {
        if (dir == null) {
            return;
        }
        FileContentManager cmgr = WebloggerFactory.getWeblogger()
                .getFileContentManager();
        Set<MediaFile> files = dir.getMediaFiles();
        for (MediaFile mf : files) {
            try {
                cmgr.deleteFile(dir.getWeblog(), mf.getId());
                // Now thumbnail
                cmgr.deleteFile(dir.getWeblog(), mf.getId() + "_sm");
            } catch (Exception e) {
                log.debug("File to be deleted already unavailable in the file store");
            }
            this.strategy.remove(mf);
        }

        dir.getWeblog().getMediaFileDirectories().remove(dir);

        // Contained media files
        strategy.flush();

        this.strategy.remove(dir);

        // Refresh associated parent
        strategy.flush();
    }

    private void updateWeblogLastModifiedDate(Weblog weblog) throws WebloggerException {
        weblog.setLastModified(new java.util.Date());
        strategy.store(weblog);
    }
}
