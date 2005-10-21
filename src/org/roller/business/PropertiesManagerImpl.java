/*
 * PropertiesManagerImpl.java
 *
 * Created on April 21, 2005, 10:43 AM
 */

package org.roller.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.PropertiesManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.pojos.RollerConfigData;
import org.roller.pojos.RollerPropertyData;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import org.roller.config.RollerRuntimeConfig;
import org.roller.config.runtime.ConfigDef;
import org.roller.config.runtime.DisplayGroup;
import org.roller.config.runtime.PropertyDef;
import org.roller.config.runtime.RuntimeConfigDefs;

/**
 * Abstract PropertiesManager implementation.
 *
 * @author Allen Gilliland
 */
public abstract class PropertiesManagerImpl implements PropertiesManager
{

    protected PersistenceStrategy mStrategy;

    private static Log mLogger =
        LogFactory.getFactory().getInstance(PropertiesManagerImpl.class);


    /**
     * Creates a new instance of PropertiesManagerImpl
     */
    public PropertiesManagerImpl(PersistenceStrategy strategy)
    {
        this.mStrategy = strategy;
        init();
    }

    private void init()
    {
        Map props = null;
        try
        {
            props = this.getProperties();
            
            if(props.size() < 1) {
                // empty props table ... try migrating, then load defaults
                props = migrateOldRollerConfig(props);
                props = initializeMissingProps(props);
            } else {
                // found existing props ... check for new props
                props = initializeMissingProps(props);
            }
            
            // save our changes
            this.store(props);
        }
        catch (Exception e)
        {
            mLogger.fatal("Failed to initialize runtime configuration properties."+
                    "Please check that the database has been upgraded!", e);
            throw new RuntimeException(e);
        }

    }

    /**
     * Save a single property
     */
    public void store(RollerPropertyData property) throws RollerException
    {
        this.mStrategy.store(property);
    }

    /**
     * Save all properties
     */
    public void store(Map properties) throws RollerException
    {
        // just go through the list and store each property
        Iterator props = properties.values().iterator();
        while (props.hasNext())
        {
            try
            {
                this.mStrategy.store((RollerPropertyData) props.next());
            }
            catch (RollerException re)
            {
                mLogger.error("Couldn't store Roller property", re);
                throw re;
            }
        }
    }

    public void release()
    {
    }


    /**
     * Migrate data from the old roller config.
     * This is called only if the existing runtime properties are empty.
     */
    private Map migrateOldRollerConfig(Map props)
    {
        // try to get the old config
        Roller roller = RollerFactory.getRoller();
        RollerConfigData rollerConfig = null;

        try
        {
            rollerConfig = roller.getConfigManager().getRollerConfig();
        }
        catch (Exception e)
        {
            // We currently treat any exception obtaining the roller config
            // as if we had not found it.
            mLogger.error(e);
        }

        if (rollerConfig != null)
        {
            mLogger.info("Found old roller config ... doing migration to new runtime properties.");
            // copy over data
            props.put("site.name",
                new RollerPropertyData("site.name", rollerConfig.getSiteName()));
            props.put("site.description",
                new RollerPropertyData("site.description", rollerConfig.getSiteDescription()));
            props.put("site.adminemail",
                new RollerPropertyData("site.adminemail", rollerConfig.getEmailAddress()));
            props.put("site.absoluteurl",
                new RollerPropertyData("site.absoluteurl", rollerConfig.getAbsoluteURL()));
            props.put("site.linkbacks.enabled",
                new RollerPropertyData("site.linkbacks.enabled", rollerConfig.getEnableLinkback().toString()));
            props.put("users.registration.enabled",
                new RollerPropertyData("users.registration.enabled", rollerConfig.getNewUserAllowed().toString()));
            props.put("users.themes.path",
                new RollerPropertyData("users.themes.path", rollerConfig.getUserThemes()));
            props.put("users.editor.pages",
                new RollerPropertyData("users.editor.pages", rollerConfig.getEditorPages()));
            props.put("users.comments.enabled",
                new RollerPropertyData("users.comments.enabled", "true"));
            props.put("users.comments.autoformat",
                new RollerPropertyData("users.comments.autoformat", rollerConfig.getAutoformatComments().toString()));
            props.put("users.comments.escapehtml",
                new RollerPropertyData("users.comments.escapehtml", rollerConfig.getEscapeCommentHtml().toString()));
            props.put("users.comments.emailnotify",
                new RollerPropertyData("users.comments.emailnotify", rollerConfig.getEmailComments().toString()));
            props.put("uploads.enabled",
                new RollerPropertyData("uploads.enabled", rollerConfig.getUploadEnabled().toString()));
            props.put("uploads.types.allowed",
                new RollerPropertyData("uploads.types.allowed", rollerConfig.getUploadAllow()));
            props.put("uploads.types.forbid",
                new RollerPropertyData("uploads.types.forbid", rollerConfig.getUploadForbid()));
            props.put("uploads.file.maxsize",
                new RollerPropertyData("uploads.file.maxsize", rollerConfig.getUploadMaxFileMB().toString()));
            props.put("uploads.dir.maxsize",
                new RollerPropertyData("uploads.dir.maxsize", rollerConfig.getUploadMaxDirMB().toString()));
            /* no longer part of runtime config
            props.put("aggregator.enabled",
                new RollerPropertyData("aggregator.enabled", rollerConfig.getEnableAggregator().toString()));
            props.put("aggregator.cache.enabled",
                new RollerPropertyData("aggregator.cache.enabled", rollerConfig.getRssUseCache().toString()));
            props.put("aggregator.cache.timeout",
                new RollerPropertyData("aggregator.cache.timeout", rollerConfig.getRssCacheTime().toString()));
            props.put("debug.memory.enabled",
                new RollerPropertyData("debug.memory.enabled", rollerConfig.getMemDebug().toString()));
            */
            props.put("spam.referers.ignorewords",
                new RollerPropertyData("spam.referers.ignorewords", rollerConfig.getRefererSpamWords()));
        }
        else
        {
            mLogger.info("Old roller config not found ... default values will be loaded");
        }
        
        return props;
    }

    
    /**
     * This method compares the property definitions in the RuntimeConfigDefs
     * file with the properties in the given Map and initializes any properties
     * that were not found in the Map.
     *
     * If the Map of props is empty/null then we will initialize all properties.
     **/
    private Map initializeMissingProps(Map props) {
        
	if(props == null)
		props = new HashMap();

        // start by getting our runtimeConfigDefs
        RuntimeConfigDefs runtimeConfigDefs = 
                RollerRuntimeConfig.getRuntimeConfigDefs();
        
        // can't do initialization without our config defs
        if(runtimeConfigDefs == null)
            return props;
        
        // iterator through all the definitions and add properties
        // that are not already in our props map
        ConfigDef configDef = null;
        DisplayGroup dGroup = null;
        PropertyDef propDef = null;
        Iterator defs = runtimeConfigDefs.getConfigDefs().iterator();
        while(defs.hasNext()) {
            configDef = (ConfigDef) defs.next();
            
            Iterator groups = configDef.getDisplayGroups().iterator();
            while(groups.hasNext()) {
                dGroup = (DisplayGroup) groups.next();
                
                Iterator propdefs = dGroup.getPropertyDefs().iterator();
                while(propdefs.hasNext()) {
                    propDef = (PropertyDef) propdefs.next();
                    
                    // do we already have this prop?  if not then add it
                    if(!props.containsKey(propDef.getName())) {
                        RollerPropertyData newprop =
                                new RollerPropertyData(propDef.getName(), propDef.getDefaultValue());
                        
                        props.put(propDef.getName(), newprop);
                                
                        mLogger.info("Found uninitialized property "+propDef.getName()+
                                " ... setting value to ["+propDef.getDefaultValue()+"]");
                    }
                }
            }
        }
        
        return props;
    }
    
}
