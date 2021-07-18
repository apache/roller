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
 */

package org.apache.roller.weblogger.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;


/**
 * Represents a list of banned ip addresses.
 *
 * This base implementation gets its list from a file on the filesystem.  We
 * are also aware of when the file changes via some outside source and we will
 * automatically re-read the file and update the list when that happens.
 */
public final class IPBanList {

    private static final Log log = LogFactory.getLog(IPBanList.class);

    // set of ips that are banned, use a set to ensure uniqueness
    private volatile Set<String> bannedIps = newThreadSafeSet();

    // file listing the ips that are banned
    private ModifiedFile bannedIpsFile = null;

    // reference to our singleton instance
    private static IPBanList instance = null;


    static {
        instance = new IPBanList(() -> WebloggerConfig.getProperty("ipbanlist.file"));
    }


    // package-private for unit tests
    IPBanList(Supplier<String> banIpsFilePathSupplier) {

        log.debug("INIT");

        // load up set of denied ips
        String banIpsFilePath = banIpsFilePathSupplier.get();
        if(banIpsFilePath != null) {
            ModifiedFile banIpsFile = new ModifiedFile(banIpsFilePath);

            if(banIpsFile.exists() && banIpsFile.canRead()) {
                this.bannedIpsFile = banIpsFile;
                this.loadBannedIps();
            }
        }
    }


    // access to the singleton instance
    public static IPBanList getInstance() {
        return instance;
    }


    public boolean isBanned(String ip) {

        // update the banned ips list if needed
        this.loadBannedIpsIfNeeded();

        if(ip != null) {
            return this.bannedIps.contains(ip);
        } else {
            return false;
        }
    }


    public void addBannedIp(String ip) {

        if(ip == null) {
            return;
        }

        // update the banned ips list if needed
        this.loadBannedIpsIfNeeded();

        if(!this.bannedIps.contains(ip) &&
            (bannedIpsFile != null && bannedIpsFile.canWrite())) {

            try {
                synchronized(this) {
                    // add to file
                    PrintWriter out = new PrintWriter(new FileWriter(this.bannedIpsFile, true));
                    out.println(ip);
                    out.close();
                    this.bannedIpsFile.clearChanged();

                    // add to Set
                    this.bannedIps.add(ip);
                }

                log.debug("ADDED "+ip);
            } catch(Exception e) {
                log.error("Error adding banned ip to file", e);
            }
        }
    }


    /**
     * Check if the banned ips file has changed and needs to be reloaded.
     */
    private void loadBannedIpsIfNeeded() {

        if(bannedIpsFile != null &&
            (bannedIpsFile.hasChanged())) {

            // need to reload
            this.loadBannedIps();
        }
    }


    /**
     * Load the list of banned ips from a file.  This clears the old list and
     * loads exactly what is in the file.
     */
    private synchronized void loadBannedIps() {

        if(bannedIpsFile != null) {

            // TODO: optimize this
            try (BufferedReader in = new BufferedReader(new FileReader(this.bannedIpsFile))) {
                Set<String> newBannedIpList = newThreadSafeSet();

                String ip = null;
                while((ip = in.readLine()) != null) {
                    newBannedIpList.add(ip);
                }

                // list updated, reset modified file
                this.bannedIps = newBannedIpList;
                this.bannedIpsFile.clearChanged();

                log.info(this.bannedIps.size()+" banned ips loaded");
            } catch(Exception ex) {
                log.error("Error loading banned ips from file", ex);
            }

        }
    }


    // a simple extension to the File class which tracks if the file has
    // changed since the last time we checked
    private static class ModifiedFile extends java.io.File {

        private long myLastModified = 0;

        public ModifiedFile(String filePath) {
            super(filePath);

            this.myLastModified = lastModified();
        }

        public boolean hasChanged() {
            return lastModified() != myLastModified;
        }

        public void clearChanged() {
            myLastModified = lastModified();
        }
    }

    private static <T> Set<T> newThreadSafeSet() {
        return ConcurrentHashMap.newKeySet();
    }
}