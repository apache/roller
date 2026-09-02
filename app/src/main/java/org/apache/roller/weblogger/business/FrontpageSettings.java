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
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.  For additional
 * information regarding copyright in this work, please see the NOTICE
 * file in the top level directory of this distribution.
 */
package org.apache.roller.weblogger.business;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Reads and writes the site frontpage weblog settings.
 *
 * <p>Two screens change these values: the one-time setup screen used to choose
 * a frontpage while the site is being installed, and the global configuration
 * screen used afterwards. Both go through here so that the handle is resolved
 * and validated the same way and both properties move together.
 */
public final class FrontpageSettings {

    public static final String HANDLE_PROPERTY = "site.frontpage.weblog.handle";
    public static final String AGGREGATED_PROPERTY = "site.frontpage.weblog.aggregated";

    private FrontpageSettings() {
    }

    /**
     * Resolves a submitted handle to a weblog that actually exists and is
     * enabled.
     *
     * @return the weblog, or null when the handle is blank, unknown or refers
     *         to a disabled weblog
     */
    public static Weblog resolveWeblog(String handle) throws WebloggerException {
        if (StringUtils.isBlank(handle) || !isValidHandle(handle.trim())) {
            return null;
        }
        return WebloggerFactory.getWeblogger().getWeblogManager()
                .getWeblogByHandle(handle.trim(), Boolean.TRUE);
    }

    /** @return the configured frontpage handle, or null when none is set. */
    public static String getConfiguredHandle() throws WebloggerException {
        RuntimeConfigProperty prop = WebloggerFactory.getWeblogger()
                .getPropertiesManager().getProperty(HANDLE_PROPERTY);
        if (prop == null || StringUtils.isBlank(prop.getValue())) {
            return null;
        }
        return prop.getValue();
    }

    /** @return true when a frontpage weblog has already been chosen. */
    public static boolean isConfigured() throws WebloggerException {
        return resolveWeblog(getConfiguredHandle()) != null;
    }

    /**
     * Validates and stores the frontpage selection.
     *
     * <p>Both properties are written before the single flush so the pair cannot
     * be left half-applied, and the canonical handle from the resolved weblog is
     * stored rather than the submitted text. A missing aggregation value is
     * treated as false, which is what an unchecked checkbox means.
     *
     * @param handle     submitted weblog handle
     * @param aggregated submitted aggregation flag; null means false
     * @throws InvalidFrontpageWeblogException when the handle does not name an
     *         existing, enabled weblog
     */
    public static boolean applyInitial(String handle, Boolean aggregated)
            throws WebloggerException {

        Weblog weblog = resolveWeblog(handle);
        if (weblog == null) {
            throw new InvalidFrontpageWeblogException(handle);
        }

        PropertiesManager mgr = WebloggerFactory.getWeblogger().getPropertiesManager();

        RuntimeConfigProperty handleProp = mgr.getProperty(HANDLE_PROPERTY);
        String currentValue = handleProp == null ? null : handleProp.getValue();
        if (resolveWeblog(currentValue) != null
                || !mgr.compareAndSetProperty(HANDLE_PROPERTY, currentValue, weblog.getHandle())) {
            return false;
        }

        RuntimeConfigProperty aggregatedProp = mgr.getProperty(AGGREGATED_PROPERTY);
        aggregatedProp.setValue(Boolean.toString(Boolean.TRUE.equals(aggregated)));
        mgr.saveProperty(aggregatedProp);

        WebloggerFactory.getWeblogger().flush();

        return true;
    }

    private static boolean isValidHandle(String handle) {
        for (int i = 0; i < handle.length(); i++) {
            if (!Character.isLetterOrDigit(handle.charAt(i)) && handle.charAt(i) != '_') {
                return false;
            }
        }
        return true;
    }

    /** Raised when a submitted frontpage handle cannot be used. */
    public static class InvalidFrontpageWeblogException extends WebloggerException {
        private final String handle;

        public InvalidFrontpageWeblogException(String handle) {
            super("Not an existing, enabled weblog handle: " + handle);
            this.handle = handle;
        }

        public String getHandle() {
            return handle;
        }
    }
}
