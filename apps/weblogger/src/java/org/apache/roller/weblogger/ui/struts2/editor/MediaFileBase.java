package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.MediaFileDirectoryComparator;
import org.apache.roller.weblogger.pojos.MediaFileDirectoryComparator.DirectoryComparatorType;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

@SuppressWarnings("serial")
public class MediaFileBase extends UIAction {
    private static Log log = LogFactory.getLog(MediaFileBase.class);

    private String[] selectedMediaFiles;
    private String selectedDirectory;
    private String mediaFileId;

    private List<MediaFileDirectory> allDirectories;
    
    private boolean overlayMode;
    
    protected void doDeleteMediaFile() {
        
    	try {
			log.debug("Processing delete of file id - " + this.mediaFileId);
			MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
			MediaFile mediaFile = manager.getMediaFile(this.mediaFileId);
			manager.removeMediaFile(getActionWeblog(), mediaFile);
			// flush changes
			WebloggerFactory.getWeblogger().flush();
			addMessage("mediaFile.delete.success");
		} catch (WebloggerException e) {
            log.error("Error deleting media file", e);
            // TODO: i18n
            addError("Error deleting media file - " + this.mediaFileId);
		}
    }
    
    protected void doIncludeMediaFileInGallery() {
        
    	try {
			log.debug("Processing include-in-gallery of file id - " + this.mediaFileId);
			MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
			MediaFile mediaFile = manager.getMediaFile(this.mediaFileId);
			mediaFile.setSharedForGallery(true);
			manager.updateMediaFile(getActionWeblog(), mediaFile);
			// flush changes
			WebloggerFactory.getWeblogger().flush();
			addMessage("mediaFile.includeInGallery.success");
		} catch (WebloggerException e) {
            log.error("Error including media file in gallery", e);
            // TODO: i18n
            addError("Error including media file in gallery - " + this.mediaFileId);
		}
    }
    

    protected void doDeleteSelected() {
        String[] fileIds = getSelectedMediaFiles();
        if (fileIds != null && fileIds.length > 0) {
        	try {
				log.debug("Processing delete of " + fileIds.length + " media files.");
				MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
				for (int index = 0; index < fileIds.length; index++) {
				    log.debug("Deleting media file - " + fileIds[index]);
				    MediaFile mediaFile = manager.getMediaFile(fileIds[index]);
				    manager.removeMediaFile(getActionWeblog(), mediaFile);
				}
				// flush changes
				WebloggerFactory.getWeblogger().flush();
				addMessage("mediaFile.delete.success");
			} catch (WebloggerException e) {
	            log.error("Error deleting selected media files", e);
                // TODO: i18n
                addError("Error deleting selected media files");
			}
        }
        else {
        	addError("mediaFile.error.noneSelectedForDelete");
        }
    }
    
    protected void doMoveSelected() {
        String[] fileIds = getSelectedMediaFiles();
        if (fileIds != null && fileIds.length > 0) {
        	try {
				log.debug("Processing move of " + fileIds.length + " media files.");
				MediaFileManager manager = WebloggerFactory.getWeblogger().getMediaFileManager();
				MediaFileDirectory targetDirectory = manager.getMediaFileDirectory(this.selectedDirectory);
				for (int index = 0; index < fileIds.length; index++) {
				    log.debug("Moving media file - " + fileIds[index] + " to directory - " + this.selectedDirectory);
				    MediaFile mediaFile = manager.getMediaFile(fileIds[index]);
				    manager.moveMediaFile(mediaFile, targetDirectory);
				}
				// flush changes
				WebloggerFactory.getWeblogger().flush();
				addMessage("mediaFile.move.success");
			} catch (WebloggerException e) {
	            log.error("Error moving selected media files", e);
                // TODO: i18n
                addError("Error moving selected media files");
			}
        }
        else {
        	addError("mediaFile.error.noneSelectedForMove");
        }
    }

    protected void refreshAllDirectories() {
        try {
            MediaFileManager mgr = WebloggerFactory.getWeblogger().getMediaFileManager();
            List<MediaFileDirectory> directories = mgr.getMediaFileDirectories(getActionWeblog());
            List<MediaFileDirectory> sortedDirList = new ArrayList<MediaFileDirectory>();
            sortedDirList.addAll(directories);
            Collections.sort(sortedDirList, new MediaFileDirectoryComparator(DirectoryComparatorType.PATH));
            setAllDirectories(sortedDirList);
        } catch (WebloggerException ex) {
            log.error("Error looking up media file directories", ex);
        }
    }
    
    /**
     * Constructs the external URL for a given media file
     * @param mediaFile
     * @return
     */
    protected String getMediaFileURL(MediaFile mediaFile) {
    	return getSiteURL() + "/roller-ui/rendering/media-resources/" + mediaFile.getId();
    }

	public String[] getSelectedMediaFiles() {
		return selectedMediaFiles;
	}

	public void setSelectedMediaFiles(String[] selectedMediaFiles) {
		this.selectedMediaFiles = selectedMediaFiles;
	}

	public String getSelectedDirectory() {
		return selectedDirectory;
	}

	public void setSelectedDirectory(String selectedDirectory) {
		this.selectedDirectory = selectedDirectory;
	}


	public List<MediaFileDirectory> getAllDirectories() {
		return allDirectories;
	}

	public void setAllDirectories(List<MediaFileDirectory> allDirectories) {
		this.allDirectories = allDirectories;
	}

	public String getMediaFileId() {
		return mediaFileId;
	}

	public void setMediaFileId(String mediaFileId) {
		this.mediaFileId = mediaFileId;
	}
	
	public boolean isOverlayMode() {
		return overlayMode;
		
	}

	public void setOverlayMode(boolean mode) {
	    this.overlayMode = mode; 
	}
}
