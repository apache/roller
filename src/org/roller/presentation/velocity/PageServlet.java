package org.roller.presentation.velocity;


/**
 * The PageServlet handles all requests for weblog pages at /page/*
 *
 * All the real work is handled by the BasePageServlet though.
 *
 * @see org.roller.presentation.velocity.BasePageServlet
 * 
 * @web.servlet name="PageServlet" load-on-startup="0"
 * @web.servlet-init-param name="org.apache.velocity.properties" 
 * 		                  value="/WEB-INF/velocity.properties"
 *  
 * @web.servlet-mapping url-pattern="/page/*"
 */ 
public class PageServlet extends BasePageServlet {
    
    /**
     * This class used to have some special velocity init code that was
     * related to how previewing worked, but it became obsolete when we
     * redid the theme management stuff, so now this class does nothing.
     */
}

