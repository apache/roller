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
import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.User;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataRetrievalFailureException;

/**
 * Spring Security UserDetailsService implemented using Weblogger API.
 */
public class RollerUserDetailsService implements UserDetailsService {
    private static Log log = LogFactory.getLog(RollerUserDetailsService.class);
    
    /**
     * @throws UsernameNotFoundException, DataAccessException
     */
    @Override
    public UserDetails loadUserByUsername(String userName) {
        // hiding the login form is not an authentication control: refuse
        // password lookups server side when only OIDC login is configured
        if (WebloggerConfig.getAuthMethod() == AuthMethod.OIDC) {
            throw new UsernameNotFoundException(
                    "form login is disabled: authentication.method is oidc");
        }

        Weblogger roller;
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
            User userData;
            try {
                userData = umgr.getUserByUserName(userName);
            } catch (WebloggerException ex) {
                throw new DataRetrievalFailureException("ERROR in user lookup", ex);
            }
            if (userData == null) {
                throw new UsernameNotFoundException("ERROR no user: " + userName);
            }
            List<SimpleGrantedAuthority> authorities = getAuthorities(userData, umgr);
            return new org.springframework.security.core.userdetails.User(userData.getUserName(), userData.getPassword(),
                    true, true, true, true, authorities);
        } catch (WebloggerException ex) {
            throw new DataAccessResourceFailureException("ERROR: fetching roles", ex);
        }
        

    }
        
     private List<SimpleGrantedAuthority> getAuthorities(User userData, UserManager umgr) throws WebloggerException {
         List<String> roles = umgr.getRoles(userData);
         List<SimpleGrantedAuthority> authorities = new ArrayList<>(roles.size());
         for (String role : roles) {
             authorities.add(new SimpleGrantedAuthority(role));
         }
         return authorities;
     }
    
}
