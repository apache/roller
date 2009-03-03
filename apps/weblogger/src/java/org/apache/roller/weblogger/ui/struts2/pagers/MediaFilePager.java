package org.apache.roller.weblogger.ui.struts2.pagers;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.MediaFile;

public class MediaFilePager {
    private static final Log log = LogFactory.getLog(MediaFilePager.class);
    
    // the collection for the pager
    private final List<MediaFile> items;
    
    // what page we are on
    private final int pageNum;
    
    // are there more items?
    private final boolean moreItems;
    
    public MediaFilePager(int page, List<MediaFile> mediaFiles, boolean hasMore) {
        this.pageNum = page;
        this.items = mediaFiles;
        this.moreItems = hasMore;
    }
    
    public List<MediaFile> getItems() {
        return items;
    }

    public boolean isMoreItems() {
        return moreItems;
    }

    public boolean isJustOnePage() {
    	return (pageNum == 0 && !moreItems);
    }
    
    public boolean hasPrevious() {
    	return (pageNum > 0);
    }
    
    public boolean hasNext() {
    	return this.moreItems;
    }
    
    

}
