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
package org.apache.roller.weblogger.pojos;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.WebloggerUtils;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name="media_directory")
@NamedQueries({
        @NamedQuery(name="MediaFileDirectory.getByWeblog",
                query="SELECT d FROM MediaFileDirectory d WHERE d.weblog = ?1 order by d.name"),
        @NamedQuery(name="MediaFileDirectory.getByWeblogAndName",
                query="SELECT d FROM MediaFileDirectory d WHERE d.weblog = ?1 AND d.name = ?2")
})
public class MediaFileDirectory {

    private String id;
    String name;
    String description;
    Weblog weblog;
    Set<MediaFile> mediaFiles = new HashSet<MediaFile>();

    public MediaFileDirectory() {
    }

    public MediaFileDirectory(Weblog weblog, String name,
            String desc) {

        this.id = WebloggerUtils.generateUUID();
        this.name = name;
        this.description = desc;

        this.weblog = weblog;
        weblog.getMediaFileDirectories().add(this);
    }

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic(optional=false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne
    @JoinColumn(name="weblogid",nullable=false)
    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    @OneToMany(targetEntity=org.apache.roller.weblogger.pojos.MediaFile.class,
            cascade={CascadeType.PERSIST, CascadeType.REMOVE}, mappedBy="directory")
    @OrderBy("name")
    public Set<MediaFile> getMediaFiles() {
        return mediaFiles;
    }

    public void setMediaFiles(Set<MediaFile> mediaFiles) {
        this.mediaFiles = mediaFiles;
    }

    @Transient
    public boolean isEmpty() {
        return getMediaFiles().isEmpty();
    }

    /**
     * Indicates whether this directory contains the specified file.
     * 
     * @param name
     *            file name
     * @return true if the file is present in the directory, false otherwise.
     */
    public boolean hasMediaFile(String name) {
        Set<MediaFile> fileSet = this.getMediaFiles();
        if (fileSet == null) {
            return false;
        }
        for (MediaFile mediaFile : fileSet) {
            if (mediaFile.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns file with the given name, if present in this directory
     * 
     * @param name
     *            file name
     * @return media file object
     */
    public MediaFile getMediaFile(String name) {
        Set<MediaFile> fileSet = this.getMediaFiles();
        if (fileSet == null) {
            return null;
        }
        for (MediaFile mediaFile : fileSet) {
            if (mediaFile.getName().equals(name)) {
                return mediaFile;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof MediaFileDirectory)) {
            return false;
        }
        MediaFileDirectory o = (MediaFileDirectory) other;
        return new EqualsBuilder().append(getId(), o.getId())
                .append(getName(), o.getName())
                .append(getDescription(), o.getDescription()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getId()).append(getName())
                .append(getDescription()).toHashCode();
    }

    public static Comparator<MediaFileDirectory> Comparator = new Comparator<MediaFileDirectory>() {
        public int compare(MediaFileDirectory dir1, MediaFileDirectory dir2) {
            return dir1.getName().compareTo(dir2.getName());
        }
    };

}
