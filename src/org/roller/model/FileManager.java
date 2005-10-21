package org.roller.model;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;

import org.roller.RollerException;
import org.roller.pojos.WebsiteData;
import org.roller.util.RollerMessages;

/**
 * Interface for managing files uploaded to Roller.
 *
 * NOTE: this should probably be renamed "ResourceManager" or similar
 * since the real jobe here is managing resources, not just files.  We should
 * then extend this a bit more to include the notion of not only user uploaded
 * resources, but also other resources the system stores, such as the blacklist.
 *
 * @author dave
 */
public interface FileManager extends Serializable 
{
    /** Determine if file can be saved in website's file space. */
    public boolean canSave(
        WebsiteData site, String name, long size, RollerMessages msgs) 
        throws RollerException;
    
    /** Get website's files */
    public File[] getFiles(WebsiteData site) 
        throws RollerException;
    
    /** Delete specified file from website's file space. */
    public void deleteFile(WebsiteData site, String name) 
        throws RollerException;

    /** Save file in website's file space or throw exception if rules violated. */
    public void saveFile(WebsiteData site, String name, long size, InputStream is) 
        throws RollerException;

    /**
     * Get directory in which uploaded files are stored
     */
    public String getUploadDir();
    /**
     * Get base URL where uploaded files are made available.
     */
    public String getUploadUrl();
    
    /**
     * Release all resources associated with Roller session.
     */
    public void release();
}
