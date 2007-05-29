package org.apache.roller.ui.core.security;

import java.util.Iterator;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.userdetails.User;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.roller.RollerException;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.UserRole;
import org.apache.roller.pojos.UserData;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * Acegi user details service implemented using Roller API.
 */
public class RollerUserDetailsService implements UserDetailsService {

    public UserDetails loadUserByUsername(String userName) 
        throws UsernameNotFoundException, DataAccessException {
        
        UserData userData = null;
        try {
            Roller roller = RollerFactory.getRoller();
            UserManager umgr = roller.getUserManager();
            userData = umgr.getUserByUserName(userName, Boolean.TRUE); 
        } catch (RollerException ex) {
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
        
        return new User(
            userData.getUserName(), userData.getPassword(), true, authorities);
    }
    
}
