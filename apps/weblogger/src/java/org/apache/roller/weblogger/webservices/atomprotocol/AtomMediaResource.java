/*
 * Copyright 2007 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.weblogger.webservices.atomprotocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Date;
import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import org.apache.roller.weblogger.pojos.ThemeResource;

/**
 * Represents a media link entry.
 */
public class AtomMediaResource {

    private String contentType = null;
    private int contentLength = 0;
    private InputStream inputStream = null;
    private Date lastModified = null;

    public AtomMediaResource(ThemeResource resource) throws FileNotFoundException {
        // TODO: figure out why PNG is missing from Java MIME types
        FileTypeMap map = FileTypeMap.getDefaultFileTypeMap();
        if (map instanceof MimetypesFileTypeMap) {
            try {
                ((MimetypesFileTypeMap) map).addMimeTypes("image/png png PNG");
            } catch (Exception ignored) {
            }
        }
        contentType = map.getContentType(resource.getName());
        contentLength = (int)resource.getLength();
        lastModified = new Date(resource.getLastModified());
        inputStream = resource.getInputStream();
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
}
