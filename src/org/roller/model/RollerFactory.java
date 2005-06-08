package org.roller.model;
import org.apache.commons.logging.Log;import org.apache.commons.logging.LogFactory;import org.roller.RollerException;import org.roller.util.StringUtils;

/**
 * This is primarily a static holder for whatever Roller implementation is 
 * being used. RollerContext should call setRoller(ServletContext) during its 
 * initialization so that the Roller implementation can be instantiated. 
 * Likewise, RollerContext.getRoller() should be replaced with calls to 
 * RollerFactory.getRoller(). This should prevent us any direct ties to 
 * Castor and permit for experimentation* with other persistence frameworks.
 * 
 * @author llavandowska
 */
public abstract class RollerFactory
{
    private static final String DEFAULT_IMPL = 
        "org.roller.business.hibernate.RollerImpl";
    
    private static Roller rollerInstance = null;

    private static Log mLogger = 
        LogFactory.getFactory().getInstance(RollerFactory.class);
            
    /**
     * Let's just be doubling certain this class cannot be instantiated.
     * @see java.lang.Object#Object()
     */
    private RollerFactory()
    {
        // hello all you beautiful people
    }
    
    /**
     * Static accessor for the instance of Roller
     * held by this class.
     * 
     * @return Roller
     */
    public static Roller getRoller()
    {
		return rollerInstance;
    }
    
    /**
     * Looks up which implementation to use by the key ROLLER_IMPL_KEY and use
     * reflection to call the implementation's instantiate(ServletContext ctx)
     * method.  Should this fail (either no implementation is specified or
     * instantiate() throws an Exception) than the DEFAULT_IMPL will be tried.
     * If this should fail as well that is bad news!
     * 
     * @param rollerInstanceName The name of the Roller implementation class
     * to instantiate.
     * @param configData A string whose meaning is defined by the Roller
     * implementation class being instantiated.
     */
    public static void setRoller(
       String rollerInstanceName, String configData)
    {
        if (StringUtils.isEmpty( rollerInstanceName ))
        {
            rollerInstanceName = DEFAULT_IMPL;
        }

        try
        {
			Class factoryClass = Class.forName(rollerInstanceName);
            java.lang.reflect.Method instanceMethod = 
				factoryClass.getMethod("instantiate", 
                	new Class[]{String.class});

            rollerInstance = (Roller)instanceMethod.invoke( 
				factoryClass, new Object[] {configData} );
            
            if (mLogger.isDebugEnabled()) 
            {             
                mLogger.debug("Using Roller impl: " + rollerInstanceName);
            }
        }
        catch (Exception e)
        {
            mLogger.error("Error instantiating " + rollerInstanceName, e);
            try 
            {
                rollerInstance = 
                   org.roller.business.hibernate.RollerImpl.instantiate(configData);
                mLogger.info("** Instantiating Hibernate RollerImpl **");
            } 
            catch (RollerException re) 
            {
                mLogger.warn("Unable to instantiate Hibernate RollerImpl", e);
            }
        }
	}
	
	public static void setRoller(Roller roller)
	{
		if (roller != null) rollerInstance = roller;
	}
}
