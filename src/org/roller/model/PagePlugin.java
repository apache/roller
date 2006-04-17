/*
 * Created on May 26, 2003
 */
package org.roller.model;

import org.apache.velocity.context.Context;
import org.roller.RollerException;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;

/**
 * Interface for Roller weblog entry page plugins, which can transform
 * entry summary or text fields.
 *
 * @author David M Johnson
 */
public interface PagePlugin {
    public String name = "PagePlugin";
    
    /**
     * Plugins can this as an opportunity to add any required objects
     * to the RollerRequest and the VelocityContext.
     * @param website     Website being processed
     * @param config      javax.servlet.ServletContext (or null if running outside webapp)
     * @param baseURL     Base URL of Roller site 
     * @param ctx         Plugins may place objects into the Velocity Context.
     */
    public void init(
            WebsiteData website,
            Object servletContext,
            String baseURL,
            Context ctx) throws RollerException;
    
    /**
     * Apply plugin to summary or text string associated with entry.
     * @param entry       Entry being rendered.
     * @param str         String to which plugin should be applied.
     * @param singleEntry Indicates rendering on single entry page.
     * @return            Results of applying plugin to entry.
     */
    public String render(WeblogEntryData entry, String str);
    
    /**
     * Apply plugin to summary or text specified in string.
     * @param str String to which plugin should be applied.
     * @param singleEntry Indicates rendering on single entry page.
     * @return Results of applying plugin to string.
     */
    public String render(String str);
    
    /**
     * Must implement toString(), returning the human-friendly
     * name of this Plugin.  This is what users will see.
     * @return The human-friendly name of this Plugin.
     */
    public String toString();
    
    /**
     * Returns the human-friendly name of this Plugin.
     * This is what users will see.
     * @return The human-friendly name of this Plugin.
     */
    public String getName();
    
    /**
     * Briefly describes the function of the Plugin. May
     * contain HTML.
     * @return A brief description of the Plugin.
     */
    public String getDescription();
    
    /** Returns true if this plugin should be skipped on single entry pages. */
    public boolean getSkipOnSingleEntry();
}
