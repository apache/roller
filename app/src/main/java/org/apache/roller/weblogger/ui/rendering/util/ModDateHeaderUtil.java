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

package org.apache.roller.weblogger.ui.rendering.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.DateUtil;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository;

/**
 * Utility class to localize the modification date header-related logic.
 */
public class ModDateHeaderUtil {

	private static Log log = LogFactory.getLog(ModDateHeaderUtil.class);

	/**
	 * Instantiates a new mod date header util.
	 */
	private ModDateHeaderUtil() {
	}

	/**
	 * Sets the HTTP response status to 304 (NOT MODIFIED) if the request
	 * contains an If-Modified-Since header that specifies a time that is at or
	 * after the time specified by the value of lastModifiedTimeMillis
	 * <em>truncated to second granularity</em>. Returns true if the response
	 * status was set, false if not.
	 * 
	 * @param request
	 *            the request
	 * @param response
	 *            the response
	 * @param lastModifiedTimeMillis
	 *            the last modified time millis
	 * @param deviceType
	 *            the device type. Null to ignore ie no theme device type
	 *            swithing check.
	 * 
	 * @return true if a response status was sent, false otherwise.
	 */
	public static boolean respondIfNotModified(HttpServletRequest request,
			HttpServletResponse response, long lastModifiedTimeMillis,
			MobileDeviceRepository.DeviceType deviceType) {

		long sinceDate = 0;
		try {
			sinceDate = request.getDateHeader("If-Modified-Since");
		} catch (IllegalArgumentException ex) {
			// this indicates there was some problem parsing the header value as
			// a date
			return false;
		}

		// truncate to seconds
		lastModifiedTimeMillis -= (lastModifiedTimeMillis % 1000);

		if (log.isDebugEnabled()) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"EEE MMM dd 'at' h:mm:ss a");
			log.debug("since date = "
					+ DateUtil.format(new Date(sinceDate), dateFormat));
			log.debug("last mod date (trucated to seconds) = "
					+ DateUtil.format(new Date(lastModifiedTimeMillis),
							dateFormat));
		}

		// Set device type for device switching
		String eTag = null;
		if (deviceType != null) {
			// int code = new HashCodeBuilder().append(deviceType.name())
			// .hashCode();
			// eTag = String.valueOf(code);
			eTag = deviceType.name();
		}

		String previousToken = request.getHeader("If-None-Match");
		if (eTag != null && previousToken != null && eTag.equals(previousToken)
				&& lastModifiedTimeMillis <= sinceDate
				|| (eTag == null || previousToken == null)
				&& lastModifiedTimeMillis <= sinceDate) {

			if (log.isDebugEnabled()) {
				log.debug("NOT MODIFIED " + request.getRequestURL());
            }

			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);

			// use the same date we sent when we created the ETag the
			// first time through
			response.setHeader("Last-Modified",
					request.getHeader("If-Modified-Since"));

			return true;
		} else {
			return false;
		}
	}

	/**
	 * Set the Last-Modified header using the given time in milliseconds. Note
	 * that because the header has the granularity of one second, the value will
	 * get truncated to the nearest second that does not exceed the provided
	 * value.
	 * <p/>
	 * This will also set the Expires header to a date in the past. This forces
	 * clients to revalidate the cache each time.
	 * 
	 * @param response
	 *            the response
	 * @param lastModifiedTimeMillis
	 *            the last modified time millis
	 * @param deviceType
	 *            the device type. Null to ignore ie no theme device type
	 *            swithing check.
	 */
	public static void setLastModifiedHeader(HttpServletResponse response,
			long lastModifiedTimeMillis,
			MobileDeviceRepository.DeviceType deviceType) {

		// Save our device type for device switching. Must use chaching on
		// headers for this to work.
		if (deviceType != null) {

			// int code = new HashCodeBuilder().append(deviceType.name())
			// .hashCode();
			// String eTag = String.valueOf(code);

			String eTag = deviceType.name();

			response.setHeader("ETag", eTag);
		}

		response.setDateHeader("Last-Modified", lastModifiedTimeMillis);
		// Force clients to revalidate each time
		// See RFC 2616 (HTTP 1.1 spec) secs 14.21, 13.2.1
		response.setDateHeader("Expires", 0);
		// We may also want this (See 13.2.1 and 14.9.4)
		// response.setHeader("Cache-Control","must-revalidate");

	}

}
