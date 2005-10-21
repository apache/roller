package org.roller.business;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.model.FileManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.pojos.RollerPropertyData;
import org.roller.pojos.WebsiteData;
import org.roller.util.RollerMessages;

/**
 * Responsible for managing website resources.  This base implementation
 * writes resources to a filesystem.
 * 
 * @author David M Johnson
 * @author Allen Gilliland
 */
public class FileManagerImpl implements FileManager
{
    private String upload_dir = null;
    private String upload_url = null;
    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(FileManagerImpl.class);
    
    
    /**
     * Create file manager.
     */
    public FileManagerImpl()
    {
        String uploaddir = RollerConfig.getProperty("uploads.dir");
        String uploadurl = RollerConfig.getProperty("uploads.url");

        // Note: System property expansion is now handled by RollerConfig.

        if(uploaddir == null || uploaddir.trim().length() < 1)
            uploaddir = System.getProperty("user.home") + File.separator+"roller_data"+File.separator+"uploads";

        if( ! uploaddir.endsWith(File.separator))
            uploaddir += File.separator;
        
        if(uploadurl == null || uploadurl.trim().length() < 1)
            uploadurl = File.separator+"resources";
        
        this.upload_dir = uploaddir.replace('/',File.separatorChar);
        this.upload_url = uploadurl;
    }
    
    
    /**
     * Get the upload directory being used by this file manager
     **/
    public String getUploadDir() {
        return this.upload_dir;
    }
    
    /**
     * Get the upload path being used by this file manager
     **/
    public String getUploadUrl() {
        return this.upload_url;
    }
    
    
    /**
     * Determine if file can be saved given current RollerConfig settings.
     */
    public boolean canSave(
            WebsiteData site, String name, long size, RollerMessages messages)
            throws RollerException
    {
        Roller mRoller = RollerFactory.getRoller();
        Map config = mRoller.getPropertiesManager().getProperties();
        
        if (!((RollerPropertyData)config.get("uploads.enabled")).getValue().equalsIgnoreCase("true")) {
            messages.addError("error.upload.disabled");
            return false;
        }
        
        String allows = ((RollerPropertyData)config.get("uploads.types.allowed")).getValue();
        String forbids = ((RollerPropertyData)config.get("uploads.types.forbid")).getValue();
        String[] allowFiles = StringUtils.split(StringUtils.deleteWhitespace(allows), ",");
        String[] forbidFiles = StringUtils.split(StringUtils.deleteWhitespace(forbids), ",");
        if (!checkFileType(allowFiles, forbidFiles, name)) {
            messages.addError("error.upload.forbiddenFile", allows);
            return false;
        }
        
        BigDecimal maxDirMB = new BigDecimal(
                ((RollerPropertyData)config.get("uploads.dir.maxsize")).getValue());
        int maxDirBytes = (int)(1024000 * maxDirMB.doubleValue());
        int userDirSize = getWebsiteDirSize(site.getHandle(), this.upload_dir);
        if (userDirSize + size > maxDirBytes) {
            messages.addError("error.upload.dirmax", maxDirMB.toString());
            return false;
        }
        
        BigDecimal maxFileMB = new BigDecimal(
                ((RollerPropertyData)config.get("uploads.file.maxsize")).getValue());
        int maxFileBytes = (int)(1024000 * maxFileMB.doubleValue());
        mLogger.debug(""+maxFileBytes);
        mLogger.debug(""+size);
        if (size > maxFileBytes) {
            messages.addError("error.upload.filemax", maxFileMB.toString());
            return false;
        }
        
        return true;
    }

    /**
     * Get collection files in website's resource directory.
     * @param site Website
     * @return Collection of files in website's resource directory
     */
    public File[] getFiles(WebsiteData site) throws RollerException
    {
        String dir = this.upload_dir + site.getHandle();
        File uploadDir = new File(dir);
        return uploadDir.listFiles();
    }

    /**
     * Delete named file from website's resource area.
     */
    public void deleteFile(WebsiteData site, String name)
            throws RollerException
    {
        String dir = this.upload_dir + site.getHandle();
        File f = new File(dir + File.separator + name);
        f.delete();
    }

    /**
     * Save file to website's resource directory.
     * @param site Website to save to
     * @param name Name of file to save
     * @param size Size of file to be saved
     * @param is Read file from input stream
     */
    public void saveFile(WebsiteData site, String name, long size, InputStream is)
            throws RollerException
    {
        if (!canSave(site, name, size, new RollerMessages())) 
        {
            throw new RollerException("ERROR: upload denied");
        }
        
        byte[] buffer = new byte[8192];
        int bytesRead = 0;
        String dir = this.upload_dir;
        String handle = site.getHandle();

        File dirPath = new File(dir + File.separator + handle);
        if (!dirPath.exists())
        {
            dirPath.mkdirs();
        }
        OutputStream bos = null;
        try
        {
            bos = new FileOutputStream(
                    dirPath.getAbsolutePath() + File.separator + name);
            while ((bytesRead = is.read(buffer, 0, 8192)) != -1)
            {
                bos.write(buffer, 0, bytesRead);
            }
        }
        catch (Exception e)
        {
            throw new RollerException("ERROR uploading file", e);
        }
        finally 
        {
            try 
            {
                bos.flush();
                bos.close();
            } 
            catch (Exception ignored) {}
        }
        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("The file has been written to \"" + dir + handle + "\"");
        }
    }

    /**
     * Get upload directory path.
     * @return Upload directory path.
     * @throws RollerException
     */
    /* old method ... no longer used -- Allen G
    private String getUploadDir() throws RollerException
    {
        if(this.upload_dir != null)
        {
            return this.upload_dir;
        }
        else
        {
            Roller mRoller = RollerFactory.getRoller();
            RollerConfigData config = RollerFactory.getRoller().getConfigManager().getRollerConfig();
            String dir = config.getUploadDir();
            if (dir == null || dir.trim().length()==0)
            {
                dir = mRealPath + File.separator + USER_RESOURCES;
            }
            
            return dir;
        }
    }
    */
    
    /**
     * Returns current size of file uploads owned by specified user.
     * @param username User
     * @param dir      Upload directory
     * @return Size of user's uploaded files in bytes.
     */
    private int getWebsiteDirSize(String username, String dir)
    {
        int userDirSize = 0;
        File d = new File(dir + File.separator + username);
        if (d.mkdirs() || d.exists())
        {
            File[] files = d.listFiles();
            long dirSize = 0l;
            for (int i=0; i<files.length; i++)
            {
                if (!files[i].isDirectory())
                {
                    dirSize = dirSize + files[i].length();
                }
            }
            userDirSize = new Long(dirSize).intValue();
        }
        return userDirSize;
    }

    /**
     * Return true if file is allowed to be uplaoded given specified allowed and 
     * forbidden file types.
     * @param allowFiles  File types (i.e. extensions) that are allowed
     * @param forbidFiles File types that are forbidden
     * @param fileName    Name of file to be uploaded
     * @return True if file is allowed to be uploaded
     */
    private boolean checkFileType(
            String[] allowFiles, String[] forbidFiles, String fileName)
    {
        // default to false
        boolean allowFile = false;
        
        // if this person hasn't listed any allows, then assume they want
        // to allow *all* filetypes, except those listed under forbid
        if(allowFiles == null || allowFiles.length < 1)
            allowFile = true;
        
        // check for allowed types
        if (allowFiles != null && allowFiles.length > 0)
        {
            for (int y=0; y<allowFiles.length; y++)
            {
                if (fileName.toLowerCase().endsWith(
                        allowFiles[y].toLowerCase()))
                {
                    allowFile = true;
                    break;
                }
            }
        }
        
        // check for forbidden types ... this overrides any allows
        if (forbidFiles != null && forbidFiles.length > 0)
        {
            for (int x=0; x<forbidFiles.length; x++)
            {
                if (fileName.toLowerCase().endsWith(
                        forbidFiles[x].toLowerCase()))
                {
                    allowFile = false;
                    break;
                }
            }
        } 

        return allowFile;
    }

    /* (non-Javadoc)
     * @see org.roller.model.FileManager#release()
     */
    public void release()
    {
        // TODO Auto-generated method stub
        
    }
}
