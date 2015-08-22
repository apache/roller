/*
   Copyright 2015 Glen Mazza

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.apache.roller.weblogger.business.runnable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.OutgoingPingQueue;
import org.apache.roller.weblogger.business.pings.WeblogUpdatePinger;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A job which gathers the currently queued outgoing pings and sends them
 * to the external ping targets.
 */
public class PingProcessingJob implements Job {

    private static Log log = LogFactory.getLog(PingProcessingJob.class);

    public PingProcessingJob() {}

    /**
     * Execute the job.  Retrieve the currently queued pings from the
     * OutgoingPingQueue and send them to the ping targets.
     */
    public void execute() {
        log.debug("ping task started");

        OutgoingPingQueue opq = OutgoingPingQueue.getInstance();

        List<AutoPing> pings = opq.getPings();

        // reset queue for next execution
        opq.clearPings();

        if (WebloggerRuntimeConfig.getBooleanProperty("pings.suspendPingProcessing")) {
            log.info("Ping processing suspended on admin settings page, no pings are being generated.");
            return;
        }

        String absoluteContextUrl = WebloggerRuntimeConfig.getAbsoluteContextURL();
        if (absoluteContextUrl == null) {
            log.warn("WARNING: Skipping current ping queue processing round because we cannot yet determine the site's absolute context url.");
            return;
        }

        Boolean logOnly = WebloggerConfig.getBooleanProperty("pings.logOnly", false);

        if (logOnly) {
            log.info("pings.logOnly set to true in properties file to no actual pinging will occur." +
                    " To see logged pings, make sure logging at DEBUG for this class.");
        }

        for (AutoPing ping : pings) {
            try {
                if (logOnly) {
                    log.debug("Would have pinged:" + ping);
                } else {
                    WeblogUpdatePinger.sendPing(ping.getPingTarget(), ping.getWeblog());
                }
            } catch (IOException|XmlRpcException ex) {
                log.debug(ex);
            }
        }

        log.debug("ping task completed, pings processed = " + pings.size());
    }

    public void input(Map<String, Object> input) {
        // no-op
    }

    public Map<String, Object> output() {
        return null;
    }

}
