package org.apache.roller.weblogger.pojos;

public enum MediaFileType {

	AUDIO("audio", "Audio", "audio/"), 
	VIDEO("video", "Video", "video/"),  
	IMAGE("image", "Image", "image/"),
	OTHERS("default", "Others", null);

	String contentTypePrefix;
	String id;
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
