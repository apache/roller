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

/**
 * Enumeration that defines all possible types of media files.
 *
 */
public enum MediaFileType {

	AUDIO("audio", "Audio", "audio/"),  // audio files 
	VIDEO("video", "Video", "video/"),  // video files
	IMAGE("image", "Image", "image/"),  // image files
	OTHERS("default", "Others", null);  // all other types

	// Content type prefix used by files of this type
	String contentTypePrefix;
	
	// A unique identifier for this file type.
	String id;
	
	// Description for this file type.
	String description;
	
	MediaFileType(String id, String desc, String prefix) {
		this.id = id;
		this.description = desc;
		this.contentTypePrefix = prefix;
	}

	public String getContentTypePrefix() {
		return this.contentTypePrefix;
	}

	public String getId() {
		return id;
	}

	public String getDesc() {
		return description;
	}
}
