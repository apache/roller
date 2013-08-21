package org.apache.roller.weblogger.ui.core.security;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    private static Log log = LogFactory.getLog(RollerUserDetailsService.class);
    
    public UserDetails loadUserByUsername(String userName) 
            throws UsernameNotFoundException, DataAccessException {
        Weblogger roller = null;
        try {
            roller = WebloggerFactory.getWeblogger();
        } catch (Exception e) {
            // Should only happen in case of 1st time startup, setup required
            log.debug("Ignorable error getting Roller instance", e);
            // Thowing a "soft" exception here allows setup to proceed
            throw new UsernameNotFoundException("User info not available yet.");
        }
        try {
            UserManager umgr = roller.getUserManager();
            User userData = null;  
            // OpenID user
            if (userName.startsWith("http://") || userName.startsWith("https://")) {
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
                ArrayList<SimpleGrantedAuthority> authorities;
                
                // We are not throwing UsernameNotFound exception in case of 
                // openid authentication in order to receive OpenID Simple Registration (SREG)
                // attributes from the authentication filter and save them
                if (userData == null) {
                     authorities = new ArrayList<SimpleGrantedAuthority>(1);
                     SimpleGrantedAuthority g = new SimpleGrantedAuthority("rollerOpenidLogin");
                     authorities.add(g);
                     name = "openid";
                     password = "openid";
                } else {
                     authorities = getAuthorities(userData, umgr);
                     name = userData.getUserName();
                     password = userData.getPassword();
                }
                UserDetails usr = new org.springframework.security.core.userdetails.User(name, password,
                        true, true, true, true, authorities);
                return  usr;
                
            } else {
                // standard username/password auth
                try {
                    userData = umgr.getUserByUserName(userName);
                } catch (WebloggerException ex) {
                    throw new DataRetrievalFailureException("ERROR in user lookup", ex);
                }
                if (userData == null) {
                    throw new UsernameNotFoundException("ERROR no user: " + userName);
                }
                ArrayList<SimpleGrantedAuthority> authorities =  getAuthorities(userData, umgr);
                return new org.springframework.security.core.userdetails.User(userData.getUserName(), userData.getPassword(),
                        true, true, true, true, authorities);
            }            
        } catch (WebloggerException ex) {
            throw new DataAccessResourceFailureException("ERROR: fetching roles", ex);
        }
        

    }
        
     private ArrayList<SimpleGrantedAuthority> getAuthorities(User userData, UserManager umgr) throws WebloggerException {
         List<String> roles = umgr.getRoles(userData);
         ArrayList<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>(roles.size());
         int i = 0;
         for (String role : roles) {
             authorities.add(new SimpleGrantedAuthority(role));
         }
         return authorities;
     }
    
}
