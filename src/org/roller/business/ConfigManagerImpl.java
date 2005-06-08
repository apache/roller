/*
 * Created on Feb 4, 2004
 */
package org.roller.business;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.ConfigManager;
import org.roller.model.Roller;
import org.roller.pojos.RollerConfig;
import org.roller.util.RollerConfigFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;

/**
 * Abstract base implementation using PersistenceStrategy.
 * @author Dave Johnson
 * @author Lance Lavandowska
 */
public abstract class ConfigManagerImpl implements ConfigManager {

    private static Log mLogger =
        LogFactory.getFactory().getInstance(UserManagerImpl.class);

    protected PersistenceStrategy mStrategy;
    protected Roller mRoller;

    public ConfigManagerImpl(PersistenceStrategy strategy, Roller roller)
    {
        mStrategy = strategy;
        mRoller = roller;
    }

    public void release()
    {
    }

	/**
	 * @see org.roller.model.ConfigManager#storeRollerConfig(org.roller.pojos.RollerConfig)
	 */
	public void storeRollerConfig(RollerConfig data) throws RollerException
    {
        mStrategy.store(data);
	}

	/**
     * Take the RollerConfigFile read from roller-config.xml
     * and set the values into a new RollerConfig instance.
     * Some values will need be converted from List to comma-delimited String.
	 */
	private RollerConfig newConfigFromFile(RollerConfigFile file) throws RollerException
    {
		RollerConfig config = new RollerConfig();
        config.setAbsoluteURL ( file.getAbsoluteURL() );
        config.setRssUseCache ( Boolean.valueOf(file.getRssUseCache()) );
        config.setRssCacheTime ( new Integer(file.getRssCacheTime()) );
        config.setNewUserAllowed ( Boolean.valueOf(file.getNewUserAllowed()) );
        config.setAdminUsers ( StringUtils.join( file.getAdminUsers().toArray(), ",") );
        config.setUserThemes ( file.getNewUserThemes() );
        config.setEditorPages ( StringUtils.join( file.getEditorPages().toArray(), ",") );
        config.setEnableAggregator ( Boolean.valueOf(file.getEnableAggregator()) );
        config.setUploadEnabled ( Boolean.valueOf(file.getUploadEnabled()) );
        config.setUploadMaxDirMB ( new BigDecimal(file.getUploadMaxDirMB().doubleValue()).setScale(2) );
        config.setUploadMaxFileMB ( new BigDecimal(file.getUploadMaxFileMB().doubleValue()).setScale(2) );
        config.setUploadAllow ( StringUtils.join( file.getUploadAllow().toArray(), ",") );
        config.setUploadForbid ( StringUtils.join( file.getUploadForbid().toArray(), ",") );
        config.setUploadDir ( file.getUploadDir() );
        config.setUploadPath ( file.getUploadPath() );
        config.setMemDebug ( Boolean.valueOf(file.getMemDebug()) );
        config.setAutoformatComments ( Boolean.valueOf(file.getAutoformatComments()) );
        config.setEscapeCommentHtml ( Boolean.valueOf(file.getEscapeCommentHtml()) );
        config.setEmailComments ( Boolean.valueOf(file.getEmailComments()) );
        config.setEnableLinkback ( Boolean.valueOf(file.isEnableLinkback()) );
        config.setSiteName ( file.getSiteName() );
        config.setSiteDescription ( file.getSiteDescription() );
        config.setEmailAddress ( file.getEmailAddress() );
        config.setIndexDir ( file.getIndexDir() );
        config.setEncryptPasswords( Boolean.valueOf(file.getEncryptPasswords()) );
        return config;
	}

    /**
     * Read in RollerConfig from the indicated filePath or, if the file cannot be found
     * returns a freshly initialized RollerConfig object.
     * @param filePath where roller-config.xml can be found.
     */
    public RollerConfig readFromFile(String filePath) throws RollerException
    {
        // Try to read roller-config.xml
        RollerConfigFile file = null;
        java.io.InputStream in = null;
        try
        {
            in = new FileInputStream(filePath);
            file = RollerConfigFile.readConfig(in);
        }
        catch (FileNotFoundException fnf)
        {
            mLogger.info("roller-config.xml not found, creating new: "+ filePath);
    		    return new RollerConfig();
        }
        catch (SecurityException se)
        {
            throw new RuntimeException(se);
        }
        finally
        {
            try
            {
                if (in != null)
                    in.close();
            }
            catch (java.io.IOException ioe)
            {
                mLogger.warn("ERROR closing InputStream", ioe);
            }
        }

        if (file == null)
        {
            file = new RollerConfigFile();
            mLogger.warn("Unable to read in roller-config.xml, creating 'blank' RollerConfigFile");
        }
        return newConfigFromFile(file);
    }

	/**
	 * This isn't part of the ConfigManager Interface, because really
	 * we shouldn't ever delete the RollerConfig.  This is mostly here
	 * to assist with unit testing.
	 */
    public void removeRollerConfig(String id) throws RollerException
    {
        mStrategy.remove(id,RollerConfig.class);
    }
}
