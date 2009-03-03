package org.apache.roller.weblogger.pojos;

public enum MediaFileType {

	AUDIO("audio", "Audio", "audio/mpeg", "audio/x-ms-wma", "audio/x-wav", "audio/vnd.rn-realaudio"), 
	VIDEO("video", "Video", "video/mpeg", "video/mp4", "video/quicktime", "video/x-ms-wmv"),  
	IMAGE("image", "Image", "image/jpeg", "image/gif", "image/png", "image/tiff");

	String[] contentTypes;
	String id;
	String description;
	
	MediaFileType(String id, String desc, String... contentTypes) {
		this.id = id;
		this.description = desc;
		this.contentTypes = contentTypes;
	}

	public String[] getContentTypes() {
		return contentTypes;
	}

	public String getId() {
		return id;
	}

	public String getDesc() {
		return description;
	}
}
