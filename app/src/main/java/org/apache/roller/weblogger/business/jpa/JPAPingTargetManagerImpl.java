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
import org.apache.roller.weblogger.business.OutgoingPingQueue;
import org.apache.roller.weblogger.business.PingTargetManager;
import org.apache.roller.weblogger.business.PingResult;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JPAPingTargetManagerImpl implements PingTargetManager {

    private static Logger log = LoggerFactory.getLogger(JPAPingTargetManagerImpl.class);

    private final JPAPersistenceStrategy strategy;

    private final PropertiesManager propertiesManager;

    private final URLStrategy urlStrategy;

    // for debugging, will log but not send ping out.
    private boolean logPingsOnly = false;

    public void setLogPingsOnly(boolean boolVal) {
        logPingsOnly = boolVal;
    }

    protected JPAPingTargetManagerImpl(JPAPersistenceStrategy strategy, URLStrategy urlStrategy,
                                       PropertiesManager propertiesManager) {
        this.strategy = strategy;
        this.urlStrategy = urlStrategy;
        this.propertiesManager = propertiesManager;
    }

    @Override
    public void removePingTarget(PingTarget pingTarget) {
        // remove contents and then target
        this.removePingTargetContents(pingTarget);
        this.strategy.remove(pingTarget);
    }

    /**
     * Convenience method which removes any queued pings or auto pings that
     * reference the given ping target.
     */
    private void removePingTargetContents(PingTarget ping) {
        // Remove the website's auto ping configurations
        Query q = strategy.getNamedUpdate("AutoPing.removeByPingTarget");
        q.setParameter(1, ping);
        q.executeUpdate();
    }

    @Override
    public void savePingTarget(PingTarget pingTarget) {
        strategy.store(pingTarget);
    }

    @Override
    public PingTarget getPingTarget(String id) {
        return strategy.load(PingTarget.class, id);
    }

    @Override
    public boolean targetNameExists(String pingTargetName) {

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

    @Override
    public boolean isUrlWellFormed(String url) {

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

    @Override
    public boolean isHostnameKnown(String url) {
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

    @Override
    public List<PingTarget> getCommonPingTargets() {
        TypedQuery<PingTarget> q = strategy.getNamedQuery(
                "PingTarget.getPingTargetsOrderByName", PingTarget.class);
        return q.getResultList();
    }

    @Override
    public void initialize() {
        initializeCommonTargets();

        // Remove all autoping configurations if ping usage has been disabled.
        if (WebloggerStaticConfig.getBooleanProperty("pings.disablePingUsage", false)) {
            log.info("Ping usage has been disabled.  Removing any existing auto ping configurations.");
            removeAllAutoPings();
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
    private void initializeCommonTargets() {
        // Pattern used to parse common ping targets.
        // Each initial commmon ping target is specified in the format {{name}{url}}
        Pattern NESTED_BRACE_PAIR = Pattern.compile("\\{\\{(.*?)\\}\\{(.*?)\\}\\}");
        String PINGS_INITIAL_COMMON_TARGETS_PROP = "pings.initialCommonTargets";

        String configuredVal = WebloggerStaticConfig.getProperty(PINGS_INITIAL_COMMON_TARGETS_PROP);
        if (configuredVal == null || configuredVal.trim().length() == 0) {
            log.debug("No (or empty) value of {} present in the configuration.  Skipping initialization of commmon targets.",
                    PINGS_INITIAL_COMMON_TARGETS_PROP);
            return;
        }

        if (!getCommonPingTargets().isEmpty()) {
            log.debug("Some common ping targets are present in the database already.  Skipping initialization.");
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
                log.info("Creating common ping target '{} from configuration properties.", name);
                PingTarget pingTarget = new PingTarget(name, url, false);
                savePingTarget(pingTarget);
            } else {
                log.error("Unable to parse configured initial ping target '{}'." +
                        ". Skipping this target. Check your setting of the property ", thisTarget, PINGS_INITIAL_COMMON_TARGETS_PROP);
            }
        }
    }

    @Override
    public AutoPing getAutoPing(String id) {
        return strategy.load(AutoPing.class, id);
    }

    @Override
    public void saveAutoPing(AutoPing autoPing) {
        strategy.store(autoPing);
    }

    @Override
    public void removeAutoPing(AutoPing autoPing) {
        strategy.remove(autoPing);
    }

    @Override
    public void removeAutoPing(PingTarget pingTarget, Weblog website) {
        Query q = strategy.getNamedUpdate("AutoPing.removeByPingTarget&Weblog");
        q.setParameter(1, pingTarget);
        q.setParameter(2, website);
        q.executeUpdate();
    }

    @Override
    public void removeAllAutoPings() {
        TypedQuery<AutoPing> q = strategy.getNamedQueryCommitFirst("AutoPing.getAll", AutoPing.class);
        strategy.removeAll(q.getResultList());
    }

    @Override
    public void queueApplicableAutoPings(Weblog changedWeblog) {
        if (propertiesManager.getBooleanProperty("pings.suspendPingProcessing")) {
            log.debug("Ping processing is suspended. No auto pings will be queued.");
            return;
        }

        List<AutoPing> applicableAutopings = getAutoPingsByWeblog(changedWeblog);
        OutgoingPingQueue queue = OutgoingPingQueue.getInstance();

        for (AutoPing autoPing : applicableAutopings) {
            queue.addPing(autoPing);
        }
    }

    @Override
    public List<AutoPing> getAutoPingsByWeblog(Weblog weblog) {
        TypedQuery<AutoPing> q = strategy.getNamedQuery("AutoPing.getByWeblog", AutoPing.class);
        q.setParameter(1, weblog);
        return q.getResultList();
    }

    @Override
    public List<AutoPing> getAutoPingsByTarget(PingTarget pingTarget) {
        TypedQuery<AutoPing> q = strategy.getNamedQuery("AutoPing.getByPingTarget", AutoPing.class);
        q.setParameter(1, pingTarget);
        return q.getResultList();
    }

    @Override
    public void sendPings() {
        log.debug("ping task started");

        OutgoingPingQueue opq = OutgoingPingQueue.getInstance();

        List<AutoPing> pings = opq.getPings();

        // reset queue for next execution
        opq.clearPings();

        if (propertiesManager.getBooleanProperty("pings.suspendPingProcessing")) {
            log.info("Ping processing suspended on admin settings page, no pings are being generated.");
            return;
        }

        String absoluteContextUrl = WebloggerStaticConfig.getAbsoluteContextURL();
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
                    log.debug("Would have pinged: {}", ping);
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
                log.debug("exception", ex);
            }
        }
        if (hadDateUpdate) {
            strategy.flush();
        }

        log.info("ping task completed, pings processed = {}", pings.size());
    }

    @Override
    public PingResult sendPing(PingTarget pingTarget, Weblog weblog) throws IOException, XmlRpcException {
        String websiteUrl = urlStrategy.getWeblogURL(weblog, true);
        String pingTargetUrl = pingTarget.getPingUrl();

        // Set up the ping parameters.
        List<String> params = new ArrayList<>();
        params.add(weblog.getName());
        params.add(websiteUrl);
        log.debug("Executing ping to '{}' for weblog '{}' ({})", pingTargetUrl, websiteUrl, weblog.getName());

        // Send the ping.
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(new URL(pingTargetUrl));
        XmlRpcClient client = new XmlRpcClient();
        client.setConfig(config);
        PingResult pingResult = parseResult(client.execute("weblogUpdates.ping", params.toArray()));
        log.debug("Ping result is: {}", pingResult);
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
            log.debug("Invalid ping result of type: {}, proceeding with stand-in representative.",
                    obj.getClass().getName());
            return new PingResult(null,obj.toString());
        }
    }

}
