
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business.jpa;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.InitializationException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.PingTarget;

public class JPAPingTargetManagerImpl implements PingTargetManager {

    private final JPAPersistenceStrategy strategy;
    private static final Log LOGGER = LogFactory.getLog(JPAPingTargetManagerImpl.class);

    protected JPAPingTargetManagerImpl(JPAPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    public void removePingTarget(PingTarget pingTarget) 
            throws WebloggerException {
        // remove contents and then target
        this.removePingTargetContents(pingTarget);
        this.strategy.remove(pingTarget);
    }

    /**
     * Convenience method which removes any queued pings or auto pings that
     * reference the given ping target.
     */
    private void removePingTargetContents(PingTarget ping) 
            throws WebloggerException {
        // Remove the website's auto ping configurations
        Query q = strategy.getNamedUpdate("AutoPing.removeByPingTarget");
        q.setParameter(1, ping);
        q.executeUpdate();
    }

    public void savePingTarget(PingTarget pingTarget)
            throws WebloggerException {
        strategy.store(pingTarget);
    }

    public PingTarget getPingTarget(String id) throws WebloggerException {
        return strategy.load(PingTarget.class, id);
    }

    public boolean targetNameExists(String pingTargetName)
            throws WebloggerException {

        // Within that set of targets, fail if there is a target
        // with the same name and that target doesn't
        // have the same id.
        for (PingTarget pt : getCommonPingTargets()) {
            if (pt.getName().equals(pingTargetName)) {
                return true;
            }
        }
        // No conflict found
        return false;
    }

    
    public boolean isUrlWellFormed(String url)
            throws WebloggerException {

        if (url == null || url.trim().length() == 0) {
            return false;
        }
        try {
            URL parsedUrl = new URL(url);
            // OK.  If we get here, it parses ok.  Now just check 
            // that the protocol is http and there is a host portion.
            boolean isHttp = parsedUrl.getProtocol().equals("http");
            boolean hasHost = (parsedUrl.getHost() != null) && 
                (parsedUrl.getHost().trim().length() > 0);
            return isHttp && hasHost;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public boolean isHostnameKnown(String url)
            throws WebloggerException {
        if (url == null || url.trim().length() == 0) {
            return false;
        }
        try {
            URL parsedUrl = new URL(url);
            String host = parsedUrl.getHost();
            if (host == null || host.trim().length() == 0) {
                return false;
            }
            InetAddress addr = InetAddress.getByName(host);
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public List<PingTarget> getCommonPingTargets()
            throws WebloggerException {
        TypedQuery<PingTarget> q = strategy.getNamedQuery(
                "PingTarget.getPingTargetsOrderByName", PingTarget.class);
        return q.getResultList();
    }

    @Override
    public void initialize() throws InitializationException {
        try {
            // Initialize common targets from the configuration
            initializeCommonTargets();

            // Remove all autoping configurations if ping usage has been disabled.
            if (WebloggerConfig.getBooleanProperty("pings.disablePingUsage", false)) {
                LOGGER.info("Ping usage has been disabled.  Removing any existing auto ping configurations.");
                WebloggerFactory.getWeblogger().getAutopingManager().removeAllAutoPings();
            }
        } catch (Exception e) {
            throw new InitializationException("Error initializing ping systems", e);
        }
    }

    /**
     * Initialize the common ping targets from the configuration properties. If the current list of common ping targets
     * is empty, and the <code>pings.initialCommonTargets</code> property is present in the configuration then,
     * this method will use that value to initialize the common targets.  This is called on each server startup.
     * <p/>
     * Note: this is expected to be called during initialization with transaction demarcation being handled by the
     * caller.
     *
     * @see org.apache.roller.weblogger.ui.core.RollerContext#contextInitialized(javax.servlet.ServletContextEvent)
     */
    private static void initializeCommonTargets() throws WebloggerException {
        // Pattern used to parse common ping targets.
        // Each initial commmon ping target is specified in the format {{name}{url}}
        Pattern NESTED_BRACE_PAIR = Pattern.compile("\\{\\{(.*?)\\}\\{(.*?)\\}\\}");
        String PINGS_INITIAL_COMMON_TARGETS_PROP = "pings.initialCommonTargets";

        String configuredVal = WebloggerConfig.getProperty(PINGS_INITIAL_COMMON_TARGETS_PROP);
        if (configuredVal == null || configuredVal.trim().length() == 0) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No (or empty) value of " + PINGS_INITIAL_COMMON_TARGETS_PROP + " present in the configuration.  Skipping initialization of commmon targets.");
            }
            return;
        }
        PingTargetManager pingTargetMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
        if (!pingTargetMgr.getCommonPingTargets().isEmpty()) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Some common ping targets are present in the database already.  Skipping initialization.");
            }
            return;
        }

        String[] configuredTargets = configuredVal.trim().split(",");
        for (String configuredTarget : configuredTargets) {
            // Trim space around the target spec
            String thisTarget = configuredTarget.trim();
            // skip empty ones
            if (thisTarget.length() == 0) {
                continue;
            }
            // parse the ith target and store it
            Matcher m = NESTED_BRACE_PAIR.matcher(thisTarget);
            if (m.matches() && m.groupCount() == 2) {
                String name = m.group(1).trim();
                String url = m.group(2).trim();
                LOGGER.info("Creating common ping target '" + name + "' from configuration properties.");
                PingTarget pingTarget = new PingTarget(name, url, false);
                pingTargetMgr.savePingTarget(pingTarget);
            } else {
                LOGGER.error("Unable to parse configured initial ping target '" + thisTarget +
                        "'. Skipping this target. Check your setting of the property " + PINGS_INITIAL_COMMON_TARGETS_PROP);
            }
        }
    }

    public void release() {}
    
}
