package org.roller.presentation.velocity;

import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

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

