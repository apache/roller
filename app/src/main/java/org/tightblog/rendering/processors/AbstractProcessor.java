/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.rendering.processors;

import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mobile.device.DeviceType;
import org.tightblog.rendering.model.Model;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractProcessor implements ApplicationContextAware {

    private static Logger log = LoggerFactory.getLogger(AbstractProcessor.class);

    private ApplicationContext appContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        appContext = applicationContext;
    }

    Map<String, Object> getModelMap(String modelBean, Map<String, Object> initData) {
        HashMap<String, Object> modelMap = new HashMap<>();
        Set modelSet = appContext.getBean(modelBean, Set.class);
        for (Object obj : modelSet) {
            Model m = (Model) obj;
            m.init(initData);
            modelMap.put(m.getModelName(), m);
        }
        return modelMap;
    }

    /**
     * Sets the HTTP response status to 304 (NOT MODIFIED) if the request
     * contains an If-Modified-Since header that specifies a time that is at or
     * after the time specified by the value of lastModifiedTimeMillis
     * <em>truncated to second granularity</em>. Returns true if the response
     * status was set, false if not.
     *
     * @param request                - the request
     * @param response               - the response
     * @param lastModifiedTime       - last modified time of the requested data or null if never modified
     * @param deviceType             - standard or mobile, null to not check
     * @return true if a response status was sent, false otherwise.
     */
    boolean respondIfNotModified(HttpServletRequest request, HttpServletResponse response,
                                               Instant lastModifiedTime, DeviceType deviceType) {

        long sinceDate;
        try {
            sinceDate = request.getDateHeader("If-Modified-Since");
        } catch (IllegalArgumentException ex) {
            // this indicates there was some problem parsing the header value as a date
            return false;
        }

        if (log.isDebugEnabled()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd 'at' h:mm:ss a").withZone(ZoneId.systemDefault());
            log.debug("since date = {}", formatter.format(Instant.ofEpochMilli(sinceDate)));
            log.debug("last mod date = {}", lastModifiedTime == null ? "null" : formatter.format(lastModifiedTime));
        }

        // Set device type for device switching
        String eTag = (deviceType == null) ? null : deviceType.name();
        String previousToken = request.getHeader("If-None-Match");

        if ((eTag == null || previousToken == null || eTag.equals(previousToken)) &&
                (lastModifiedTime == null || lastModifiedTime.toEpochMilli() <= sinceDate)) {

            log.debug("NOT MODIFIED {}", request.getRequestURL());

            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            // use the same date we sent when we created the eTag the first time through
            response.setHeader("Last-Modified", request.getHeader("If-Modified-Since"));
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the Last-Modified header using the given time in milliseconds. Note
     * that because the header has the granularity of one second, the value will
     * get truncated to the nearest second that does not exceed the provided
     * value.  This will also set the Expires header to a date in the past. This forces
     * clients to revalidate the cache each time.
     */
    void setLastModifiedHeader(HttpServletResponse response,
                                             Instant lastModifiedTime, DeviceType deviceType) {

        // Save our device type for device switching. Must use caching on headers for this to work.
        response.setHeader("ETag", deviceType.name());

        if (lastModifiedTime != null) {
            response.setDateHeader("Last-Modified", lastModifiedTime.toEpochMilli());
        }
        // Force clients to revalidate each time
        // See RFC 2616 (HTTP 1.1 spec) secs 14.21, 13.2.1
        response.setDateHeader("Expires", 0);
        // We may also want this (See 13.2.1 and 14.9.4)
        // response.setHeader("Cache-Control","must-revalidate");
    }


}
