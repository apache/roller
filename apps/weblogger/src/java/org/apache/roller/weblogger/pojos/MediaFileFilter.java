package org.apache.roller.weblogger.pojos;

import java.util.List;

public class MediaFileFilter {
	
	public enum SizeFilterType {GT, GTE, EQ, LT, LTE};
	public enum MediaFileOrder {NAME, DATE_UPLOADED, TYPE};

	String name;
	MediaFileType type;
	long size;
	SizeFilterType sizeFilterType;
	List<String> tags;
	MediaFileOrder order;
	
	/**
	 * Indicates the starting index in the complete result set 
	 * from which results should be returned.  This is always applied
	 * along with the length attribute below.
	 * A value of -1 means that the complete result set should be
	 * returned. length will be ignored in this case.
	 */
	int startIndex = -1;
	/**
	 * Number of results to be returned starting from startIndex.
	 */
	int length;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MediaFileType getType() {
		return type;
	}

	public void setType(MediaFileType type) {
		this.type = type;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public SizeFilterType getSizeFilterType() {
		return sizeFilterType;
	}

	public void setSizeFilterType(SizeFilterType sizeFilterType) {
		this.sizeFilterType = sizeFilterType;
	}

	public int getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public MediaFileOrder getOrder() {
		return order;
	}

	public void setOrder(MediaFileOrder order) {
		this.order = order;
	}

}
