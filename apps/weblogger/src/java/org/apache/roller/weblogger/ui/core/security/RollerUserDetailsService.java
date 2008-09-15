package org.apache.roller.weblogger.ui.core.security;

import java.util.List;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UserDetailsService;
import org.springframework.security.userdetails.UsernameNotFoundException;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.UserAttribute;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * Spring Security UserDetailsService implemented using Weblogger API.
 */
public class RollerUserDetailsService implements UserDetailsService {
      
    public UserDetails loadUserByUsername(String userName) 
            throws UsernameNotFoundException, DataAccessException {
        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            UserManager umgr = roller.getUserManager();
            User userData = null;  
            if (userName.startsWith("http://")) {
                if (userName.endsWith("/")) {
                    userName = userName.substring(0, userName.length() -1 );
                }
                try {
                    userData = umgr.getUserByAttribute(
                        UserAttribute.Attributes.OPENID_URL.toString(), 
                        userName);
                } catch (WebloggerException ex) {
                    throw new DataRetrievalFailureException("ERROR in user lookup", ex);
                }
                String name;
                String password;
                GrantedAuthority[] authorities;
                
                // We are not throwing UsernameNotFound exception in case of 
                // openid authentication in order to recieve user SREG attributes 
                // from the authentication filter and save them                
                if (userData == null) {
                     authorities = new GrantedAuthority[1];
                     GrantedAuthority g = new GrantedAuthorityImpl("openidLogin");
                     authorities[0] = g;
                     name = "openid";
                     password = "openid";
                } else {
                     authorities =  getAuthorities(userData, umgr);
                     name = userData.getUserName();
                     password = userData.getPassword();
                }
                UserDetails usr = new org.springframework.security.userdetails.User(name, password, true, authorities);
                return  usr;
                
            } else {
                try {
                    userData = umgr.getUserByUserName(userName);
                } catch (WebloggerException ex) {
                    throw new DataRetrievalFailureException("ERROR in user lookup", ex);
                }
                if (userData == null) {
                    throw new UsernameNotFoundException("ERROR no user: " + userName);
                }
                GrantedAuthority[] authorities =  getAuthorities(userData, umgr);        
                return new org.springframework.security.userdetails.User(userData.getUserName(), userData.getPassword(), true, authorities);
            }            
        } catch (WebloggerException ex) {
            throw new DataAccessResourceFailureException("ERROR: fetching roles", ex);
        }
        

    }
        
     private GrantedAuthority[] getAuthorities(User userData, UserManager umgr) throws WebloggerException {
             List<String> roles = umgr.getRoles(userData);
            GrantedAuthority[] authorities = new GrantedAuthorityImpl[roles.size()];
            int i = 0;
            for (String role : roles) {
                authorities[i++] = new GrantedAuthorityImpl(role);
            }
            return authorities;
        }
    
}
