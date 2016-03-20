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

import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.OutgoingPingQueue;
import org.apache.roller.weblogger.business.PingTargetManager;
import org.apache.roller.weblogger.business.PingResult;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class JPAPingTargetManagerImpl implements PingTargetManager {

    private final JPAPersistenceStrategy strategy;

    private PropertiesManager propertiesManager;

    private static final Log log = LogFactory.getLog(JPAPingTargetManagerImpl.class);

    // for debugging, will log but not send ping out.
    private boolean logPingsOnly = false;

    public void setLogPingsOnly(boolean boolVal) {
        logPingsOnly = boolVal;
    }

    protected JPAPingTargetManagerImpl(JPAPersistenceStrategy strategy, PropertiesManager propertiesManager) {
        this.strategy = strategy;
        this.propertiesManager = propertiesManager;
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
    public void initialize() throws WebloggerException {
        try {
            // Initialize common targets from the configuration
            initializeCommonTargets();

            // Remove all autoping configurations if ping usage has been disabled.
            if (WebloggerConfig.getBooleanProperty("pings.disablePingUsage", false)) {
                log.info("Ping usage has been disabled.  Removing any existing auto ping configurations.");
                removeAllAutoPings();
            }
        } catch (Exception e) {
            throw new WebloggerException("Error initializing ping systems", e);
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
    private void initializeCommonTargets() throws WebloggerException {
        // Pattern used to parse common ping targets.
        // Each initial commmon ping target is specified in the format {{name}{url}}
        Pattern NESTED_BRACE_PAIR = Pattern.compile("\\{\\{(.*?)\\}\\{(.*?)\\}\\}");
        String PINGS_INITIAL_COMMON_TARGETS_PROP = "pings.initialCommonTargets";

        String configuredVal = WebloggerConfig.getProperty(PINGS_INITIAL_COMMON_TARGETS_PROP);
        if (configuredVal == null || configuredVal.trim().length() == 0) {
            if (log.isDebugEnabled()) {
                log.debug("No (or empty) value of " + PINGS_INITIAL_COMMON_TARGETS_PROP + " present in the configuration.  Skipping initialization of commmon targets.");
            }
            return;
        }

        if (!getCommonPingTargets().isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Some common ping targets are present in the database already.  Skipping initialization.");
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
                log.info("Creating common ping target '" + name + "' from configuration properties.");
                PingTarget pingTarget = new PingTarget(name, url, false);
                savePingTarget(pingTarget);
            } else {
                log.error("Unable to parse configured initial ping target '" + thisTarget +
                        "'. Skipping this target. Check your setting of the property " + PINGS_INITIAL_COMMON_TARGETS_PROP);
            }
        }
    }

    public AutoPing getAutoPing(String id) throws WebloggerException {
        return strategy.load(AutoPing.class, id);
    }

    public void saveAutoPing(AutoPing autoPing) throws WebloggerException {
        strategy.store(autoPing);
    }

    public void removeAutoPing(AutoPing autoPing) throws WebloggerException {
        strategy.remove(autoPing);
    }

    public void removeAutoPing(PingTarget pingTarget, Weblog website) throws WebloggerException {
        Query q = strategy.getNamedUpdate("AutoPing.removeByPingTarget&Weblog");
        q.setParameter(1, pingTarget);
        q.setParameter(2, website);
        q.executeUpdate();
    }

    public void removeAllAutoPings() throws WebloggerException {
        TypedQuery<AutoPing> q = strategy.getNamedQueryCommitFirst("AutoPing.getAll", AutoPing.class);
        strategy.removeAll(q.getResultList());
    }

    public void queueApplicableAutoPings(Weblog changedWeblog) throws WebloggerException {
        if (propertiesManager.getBooleanProperty("pings.suspendPingProcessing")) {
            if (log.isDebugEnabled()) {
                log.debug("Ping processing is suspended." + " No auto pings will be queued.");
            }
            return;
        }

        List<AutoPing> applicableAutopings = getAutoPingsByWeblog(changedWeblog);
        OutgoingPingQueue queue = OutgoingPingQueue.getInstance();

        for (AutoPing autoPing : applicableAutopings) {
            queue.addPing(autoPing);
        }
    }

    public List<AutoPing> getAutoPingsByWeblog(Weblog weblog) throws WebloggerException {
        TypedQuery<AutoPing> q = strategy.getNamedQuery("AutoPing.getByWeblog", AutoPing.class);
        q.setParameter(1, weblog);
        return q.getResultList();
    }

    public List<AutoPing> getAutoPingsByTarget(PingTarget pingTarget) throws WebloggerException {
        TypedQuery<AutoPing> q = strategy.getNamedQuery("AutoPing.getByPingTarget", AutoPing.class);
        q.setParameter(1, pingTarget);
        return q.getResultList();
    }

    @Override
    public void sendPings() throws WebloggerException {
        log.debug("ping task started");

        OutgoingPingQueue opq = OutgoingPingQueue.getInstance();

        List<AutoPing> pings = opq.getPings();

        // reset queue for next execution
        opq.clearPings();

        if (propertiesManager.getBooleanProperty("pings.suspendPingProcessing")) {
            log.info("Ping processing suspended on admin settings page, no pings are being generated.");
            return;
        }

        String absoluteContextUrl = WebloggerConfig.getAbsoluteContextURL();
        if (absoluteContextUrl == null) {
            log.warn("WARNING: Skipping current ping queue processing round because we cannot yet determine the site's absolute context url.");
            return;
        }

        if (logPingsOnly) {
            log.info("pings.logOnly set to true in properties file so no actual pinging will occur." +
                    " To see logged pings, make sure logging at DEBUG for this class.");
        }

        Timestamp startOfDay = new Timestamp(DateUtils.truncate(new Date(), Calendar.DAY_OF_MONTH).getTime());
        Boolean hadDateUpdate = false;
        for (AutoPing ping : pings) {
            try {
                if (logPingsOnly) {
                    log.debug("Would have pinged:" + ping);
                } else {
                    PingTarget pingTarget = ping.getPingTarget();
                    PingResult pr = sendPing(pingTarget, ping.getWeblog());
                    // for performance reasons, limit updates to daily
                    if (!pr.isError() && pingTarget.getLastSuccess().before(startOfDay)) {
                        pingTarget.setLastSuccess(startOfDay);
                        savePingTarget(pingTarget);
                        hadDateUpdate = true;
                    }
                }
            } catch (IOException|XmlRpcException ex) {
                log.debug(ex);
            }
        }
        if (hadDateUpdate) {
            strategy.flush();
        }

        log.info("ping task completed, pings processed = " + pings.size());
    }

    public PingResult sendPing(PingTarget pingTarget, Weblog website) throws IOException, XmlRpcException {
        String websiteUrl = website.getAbsoluteURL();
        String pingTargetUrl = pingTarget.getPingUrl();

        // Set up the ping parameters.
        List<String> params = new ArrayList<>();
        params.add(website.getName());
        params.add(websiteUrl);
        if (log.isDebugEnabled()) {
            log.debug("Executing ping to '" + pingTargetUrl + "' for weblog '" + websiteUrl + "' (" + website.getName() + ")");
        }

        // Send the ping.
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(pingTargetUrl));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        PingResult pingResult = parseResult(client.execute("weblogUpdates.ping", params.toArray()));

        if (log.isDebugEnabled()) {
            log.debug("Ping result is: " + pingResult);
        }
        return pingResult;
    }

    private PingResult parseResult(Object obj) {
        // Deal with the fact that some buggy ping targets may not respond with the proper struct type.
        if (obj == null) {
            return new PingResult(null,null);
        }
        try {
            // normal case: response is a struct (represented as a Map) with Boolean flerror and String fields.
            Map result = (Map) obj;
            return new PingResult((Boolean) result.get("flerror"), (String) result.get("message"));
        } catch (Exception ex) {
            // exception case:  The caller responded with an unexpected type, though parsed at the basic XML RPC level.
            // This effectively assumes flerror = false, and sets message = obj.toString();
            if (log.isDebugEnabled()) {
                log.debug("Invalid ping result of type: " + obj.getClass().getName() + "; proceeding with stand-in representative.");
            }
            return new PingResult(null,obj.toString());
        }
    }

}
