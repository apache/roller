package org.roller.presentation.velocity;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.pojos.PageData;
import org.roller.presentation.RollerContext;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * This is a simple template file loader that loads templates
 * from the Roller instance instead of plain files.
 *
 * RollerResourceLoader makes use of RollerFactory.
 *
 * @author <a href="mailto:lance@brainopolis.com">Lance Lavandowska</a>
 * @version $Id: RollerResourceLoader.java,v 1.8 2004/06/19 02:15:54 lavandowska Exp $
*/
public class RollerResourceLoader extends ResourceLoader
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(RollerResourceLoader.class);
        
    public void init( ExtendedProperties configuration)
    {
        if (mLogger.isDebugEnabled())
        {
		    mLogger.debug(configuration);
        }
    }
    
    /**
     * The web-app startup timing may be tricky.  In the case that roller is
     * null (which means that RollerContext may not have had a chance to call
     * RollerFactory yet) ask RollerContext for Roller because it has the
     * information necessary for RollerFactory to do its job.
     * @return Roller
     */
    private Roller getRoller()
    {
    	return RollerContext.getRoller( null );
    }

    public boolean isSourceModified(Resource resource)
    {
        return (resource.getLastModified() !=
            readLastModified(resource, "checking timestamp"));
    }

    public long getLastModified(Resource resource)
    {
        return readLastModified(resource, "getting timestamp");
    }

    /**
     * Get an InputStream so that the Runtime can build a
     * template with it.
     *
     *  @param name name of template
     *  @return InputStream containing template
    */
    public InputStream getResourceStream( String name )
    throws ResourceNotFoundException
    {
        if (name == null || name.length() == 0)
        {
            throw new ResourceNotFoundException ("Need to specify a template name!");
        }

        try
        {
            PageData page = getPage( name );
            if (page == null)
            {
            	throw new ResourceNotFoundException(
					"RollerResourceLoader: page \"" + 
					name + "\" not found");
            }
            return new ByteArrayInputStream( page.getTemplate().getBytes() );
        }
        catch (RollerException re)
        {
             String msg = "RollerResourceLoader Error: " +
                "database problem trying to load resource " + name;
            mLogger.error( msg, re );
            throw new ResourceNotFoundException (msg);
        }
    }

    /**
     *  Fetches the last modification time of the resource
     *
     *  @param resource Resource object we are finding timestamp of
     *  @param i_operation string for logging, indicating caller's intention
     *
     *  @return timestamp as long
     */
    private long readLastModified(Resource resource, String i_operation)
    {
        /*
         *  get the template name from the resource
        */
        String name = resource.getName();
        try
        {
            PageData page = getPage( name );
            
            if (mLogger.isDebugEnabled())
            {
                mLogger.debug(name + ": resource=" + resource.getLastModified() + 
							    " vs. page=" + page.getUpdateTime().getTime());
            }
            return page.getUpdateTime().getTime();
        }
        catch (RollerException re)
        {
            mLogger.error( "Error " + i_operation, re );
        }
        return 0;
    }

    public PageData getPage(String id) throws RollerException
    {
    	if (getRoller() == null) throw new RollerException(
			"RollerResourceLoader.getRoller() returned NULL");
        return getRoller().getUserManager().retrievePage(id); //retrievePageReadOnly( id );
    }

}
