package org.apache.roller.weblogger.ui.core.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class RollerRememberMeServices extends TokenBasedRememberMeServices {
    private static final Log log = LogFactory.getLog(CustomUserRegistry.class);


    public RollerRememberMeServices() {
    }

    public RollerRememberMeServices(String key, UserDetailsService userDetailsService) {
        super(key, userDetailsService);
    }

    /**
     * Calculates the digital signature to be put in the cookie. Default value is
     * MD5 ("username:tokenExpiryTime:password:key")
     *
     * If LDAP is enabled then a configurable dummy password is used in the calculation.
     */
    protected String makeTokenSignature(long tokenExpiryTime, String username, String password) {

        boolean usingLDAP = WebloggerConfig.getAuthMethod() == AuthMethod.LDAP;
        if (usingLDAP) {
            log.debug("LDAP is enabled; using dummy password in remember me signature.");

            // for LDAP we don't store its password in the roller_users table,
            // just an string indicating external auth method being used.
            password = WebloggerConfig.getProperty("users.passwords.externalAuthValue","<externalAuth>");
        }

        String data = username + ":" + tokenExpiryTime + ":" + password + ":" + getKey();
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No MD5 algorithm available!");
        }

        return new String(Hex.encode(digest.digest(data.getBytes())));
    }


}
