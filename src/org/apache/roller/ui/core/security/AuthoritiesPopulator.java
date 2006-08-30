/**
 * 
 */
package org.apache.roller.ui.core.security;

import java.util.List;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.ldap.LdapDataAccessException;
import org.acegisecurity.providers.ldap.LdapAuthoritiesPopulator;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.acegisecurity.userdetails.jdbc.JdbcDaoImpl;
import org.acegisecurity.userdetails.ldap.LdapUserDetails;
import org.springframework.util.Assert;

/**
 * @author Elias Torres (<a href="mailto:eliast@us.ibm.com">eliast@us.ibm.com</a>)
 *
 */
public class AuthoritiesPopulator extends JdbcDaoImpl implements LdapAuthoritiesPopulator {
  

  /** A default role which will be assigned to all authenticated users if set */
  private GrantedAuthority defaultRole = null;

  /* (non-Javadoc)
   * @see org.acegisecurity.providers.ldap.LdapAuthoritiesPopulator#getGrantedAuthorities(org.acegisecurity.userdetails.ldap.LdapUserDetails)
   */
  public GrantedAuthority[] getGrantedAuthorities(LdapUserDetails userDetails) throws LdapDataAccessException {
    
    List dbAuths = authoritiesByUsernameMapping.execute(userDetails.getUsername());

    addCustomAuthorities(userDetails.getUsername(), dbAuths);
    
    if (defaultRole != null) {
      dbAuths.add(defaultRole);
    }

    if (dbAuths.size() == 0) {
        throw new UsernameNotFoundException("User has no GrantedAuthority");
    }

    return (GrantedAuthority[]) dbAuths.toArray(new GrantedAuthority[dbAuths.size()]); 
  }
  
  /**
   * The default role which will be assigned to all users.
   *
   * @param defaultRole the role name, including any desired prefix.
   */
  public void setDefaultRole(String defaultRole) {
      Assert.notNull(defaultRole, "The defaultRole property cannot be set to null");
      this.defaultRole = new GrantedAuthorityImpl(defaultRole);
  }  

}
