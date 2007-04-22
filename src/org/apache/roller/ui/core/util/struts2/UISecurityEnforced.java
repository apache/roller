/*
 * UISecurityEnforced.java
 *
 * Created on April 17, 2007, 8:24 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.apache.roller.ui.core.util.struts2;


/**
 * Implemented by struts2 actions which want to enforce some level of security
 * protection on their action.
 * 
 * Available enforcements are ...
 *   - require a logged in user
 *   - reguire a valid weblog to work on
 *   - require a specific user role, such as "admin"
 *   - require a specific weblog permission
 *
 */
public interface UISecurityEnforced {
    
    /**
     * Does the action require an authenticated user?
     *
     * @return boolean True if authenticated user is required, false otherwise.
     */
    public boolean isUserRequired();
    
    
    /**
     * Does the action require a valid weblog to work on?
     *
     * This only takes effect if isUserRequired() is 'true'.
     *
     * @return boolean True if action weblog is required, false otherwise.
     */
    public boolean isWeblogRequired();
    
    
    /**
     * What is the required user role, if any?
     *
     * This method only takes effect if isUserRequired() is 'true'.
     *
     * @return String The required user role, or null if no role required.
     */
    public String requiredUserRole();
    
    
    /**
     * What are the required weblog permissions for this action, if any?
     *
     * This method only takes effect if both isUserRequired() and isWeblogRequired()
     * are 'true'.
     *
     * @return short The required weblog permissions, or -1 if no permissions required.
     */
    public short requiredWeblogPermissions();
    
}
