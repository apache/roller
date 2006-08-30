package org.apache.roller.ui.core.security;

import org.acegisecurity.userdetails.UserDetails;

/**
 * An interface to extract additional properties from a UserDetails instance. These extra
 * properties are needed in order to complete User object in RollerDB. 
 * 
 * @author Elias Torres (<a href="mailto:eliast@us.ibm.com">eliast@us.ibm.com</a>)
 *
 */
public interface RollerUserDetails extends UserDetails {

  public String getTimeZone();
  
  public String getLocale();
  
  public String getFullName();
  
  public String getEmailAddress();
  
}
