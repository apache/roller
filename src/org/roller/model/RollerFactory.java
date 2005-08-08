package org.roller.model;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.util.StringUtils;

/**
 * This is primarily a static holder for whatever Roller implementation is 
 * being used. RollerContext should call setRoller(ServletContext) during its 
 * initialization so that the Roller implementation can be instantiated. 
 * Likewise, RollerContext.getRoller() should be replaced with calls to 
 * RollerFactory.getRoller(). This should prevent us any direct ties to 
 * Castor and permit for experimentation* with other persistence frameworks.
 * 
 * @author llavandowska
 * @author Allen Gilliland
 */
public abstract class RollerFactory
{
    private static final String DEFAULT_IMPL = 
        "org.roller.business.hibernate.HibernateRollerImpl";
    
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
        // check to see if we need to instantiate
        if(rollerInstance == null) {
            // lookup value for the roller classname to use
            String roller_classname = 
                    RollerConfig.getProperty("persistence.roller.classname");
            
            // now call setRoller ourselves
            RollerFactory.setRoller(roller_classname);
        }
		
        return rollerInstance;
    }
    
    
    /**
     * Construct the actual Roller implemenation for this instance.
     * 
     * Use reflection to call the implementation's instantiate() method.
     * Should this fail (either no implementation is specified or
     * instantiate() throws an Exception) then the DEFAULT_IMPL will be tried.
     * If this should fail then we are in trouble :/
     *
     * @param roller_classname The name of the Roller implementation class
     * to instantiate.
     */
    public static void setRoller(String roller_classname)
    {
        
        if (StringUtils.isEmpty( roller_classname ))
            roller_classname = DEFAULT_IMPL;
        
        try
        {
            Class rollerClass = Class.forName(roller_classname);
            java.lang.reflect.Method instanceMethod =
                    rollerClass.getMethod("instantiate", (Class[])null);
            
            // do the invocation
            rollerInstance = (Roller)
                instanceMethod.invoke(rollerClass, (Class[])null);
            
            mLogger.info("Using Roller Impl: " + roller_classname);
        }
        catch (Exception e)
        {
            // uh oh
            mLogger.error("Error instantiating " + roller_classname, e);
            try
            {
                // if we didn't already try DEFAULT_IMPL then try it now
                if( ! DEFAULT_IMPL.equals(roller_classname))
                {
                    mLogger.info("** Trying DEFAULT_IMPL "+DEFAULT_IMPL+" **");
                    
                    Class rollerClass = Class.forName(DEFAULT_IMPL);
                    java.lang.reflect.Method instanceMethod =
                            rollerClass.getMethod("instantiate", (Class[])null);
                    
                    // do the invocation
                    rollerInstance = (Roller) 
                        instanceMethod.invoke(rollerClass, (Class[])null);
                }
                else
                {
                    // we just do this so that the logger gets the message
                    throw new Exception("Doh! Couldn't instantiate a roller class");
                }
                
            }
            catch (Exception re)
            {
                mLogger.fatal("Failed to instantiate fallback roller impl", re);
            }
        }
        
    }
    
	
	/**
	 * Set Roller to be returned by factory.
	 */
	public static void setRoller(Roller roller)
	{
		if (roller != null) rollerInstance = roller;
	}
}
