/*
 * Copyright 2010-2014 the original author or authors.
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
package org.apache.roller.weblogger.ui.rendering.util.mobile;

import javax.servlet.http.HttpServletRequest;

/**
 * The Class MobileDeviceRepository.
 */
public class MobileDeviceRepository {

	/**
	 * The Enum DeviceType.
	 * 
	 * eg use deviceType.equals(DeviceType.mobile) for camparison.
	 */
	public enum DeviceType {
		standard, mobile
	}

	/**
	 * Check if device in request is a mobile device.
	 * 
	 * @param request
	 *            the request
	 * 
	 * @return boolean
	 */
	public static boolean isMobileDevice(HttpServletRequest request) {

		// String userAgent = request.getHeader("User-Agent");
		// System.out.println(userAgent);

		Device device = DeviceUtils.getCurrentDevice(request);

		if (device == null) {
			// log.info("no device detected");
			return false;
		} else if (device.isNormal()) {
			// log.info("Device is normal");
			return false;
		} else if (device.isMobile()) {
			// log.info("Device is mobile");
			// System.out.println(userAgent);
			return true;
		} else if (device.isTablet()) {
			// log.info("Device is tablet");
			// System.out.println(userAgent);
			return true;
		} else {
			return false;
		}

	}

	/**
	 * Gets the request device type.
	 * 
	 * @param request
	 *            the request
	 * 
	 * @return the request type
	 */
	public static DeviceType getRequestType(HttpServletRequest request) {

		DeviceType type = DeviceType.standard;

		if (isMobileDevice(request)) {
			type = DeviceType.mobile;
		}
		return type;
	}

}
