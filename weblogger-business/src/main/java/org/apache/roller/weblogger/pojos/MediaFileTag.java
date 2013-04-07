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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;

/**
 * Represents the tag for media files.
 * 
 */
public class MediaFileTag implements Serializable {

    private static final long serialVersionUID = -1349427373511141841L;

    private String id = UUIDGenerator.generateUUID();
    private String name;
    private MediaFile mediaFile;

    public MediaFileTag() {
    }

    public MediaFileTag(String name, MediaFile mediaFile) {
        this.name = name;
        this.mediaFile = mediaFile;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MediaFile getMediaFile() {
        return mediaFile;
    }

    public void setMediaFile(MediaFile mediaFile) {
        this.mediaFile = mediaFile;
    }

    // ------------------------------------------------------- Good citizenship

    public String toString() {
        return "MediaFileTag [id=" + id + ", name=" + name + ", mediaFile="
                + mediaFile + "]";
    }

    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (other instanceof MediaFileTag != true)
            return false;
        MediaFileTag o = (MediaFileTag) other;
        return new EqualsBuilder().append(getName(), o.getName())
                .append(getMediaFile(), o.getMediaFile()).isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder().append(getName()).append(getMediaFile())
                .toHashCode();
    }

}
