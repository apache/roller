package org.apache.roller.weblogger.ui.core.security;

import java.util.Iterator;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.UserRole;
import org.apache.roller.weblogger.pojos.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * Acegi user details service implemented using Weblogger API.
 */
public class RollerUserDetailsService implements UserDetailsService {

    public UserDetails loadUserByUsername(String userName) 
        throws UsernameNotFoundException, DataAccessException {
        
        User userData = null;
        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            UserManager umgr = roller.getUserManager();
            userData = umgr.getUserByUserName(userName, Boolean.TRUE); 
        } catch (WebloggerException ex) {
            throw new DataRetrievalFailureException("ERROR in user lookup", ex);
        } 
        
        if (userData == null) {
            throw new UsernameNotFoundException("ERROR no user: " + userName);
        }
        
        GrantedAuthority[] authorities = 
            new GrantedAuthorityImpl[userData.getRoles().size()];
        int i = 0;
        for (Iterator it = userData.getRoles().iterator(); it.hasNext();) {
            UserRole role = (UserRole)it.next();
            authorities[i++] = new GrantedAuthorityImpl(role.getRole());
        }
        
        return new org.acegisecurity.userdetails.User(
            userData.getUserName(), userData.getPassword(), true, authorities);
    }
    
}
