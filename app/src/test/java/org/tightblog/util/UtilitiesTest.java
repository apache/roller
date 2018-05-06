/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.tightblog.util;

import org.junit.Test;
import org.springframework.mobile.device.Device;
import org.springframework.mobile.device.DevicePlatform;
import org.springframework.mobile.device.DeviceType;
import org.springframework.mobile.device.DeviceUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UtilitiesTest {

    @Test
    public void testRemoveHTML() {
        String test = "<br><br><p>a <b>bold</b> sentence with a <a href=\"http://example.com\">link</a></p>";
        String expect = "a bold sentence with a link";
        String result = Utilities.removeHTML(test);
        assertEquals(expect, result);
    }

    @Test
    public void testInsertLineBreaksIfMissing() {
        String convertLinesStart = "paragraph1\n\nparagraph2\nline2\nline3\n\nparagraph3";
        String convertLinesFormatted = "<p>paragraph1</p><p>paragraph2 line2 line3</p><p>paragraph3</p>";

        // reformat
        String output = Utilities.insertLineBreaksIfMissing(convertLinesStart);

        // make sure it turned out how we planned
        assertEquals(convertLinesFormatted, output);
    }

    @Test
    public void testReplaceNonAlphanumeric() {
        assertEquals("A bc  345", Utilities.replaceNonAlphanumeric("A?bc!#345", ' '));
        assertEquals("A-bc--345", Utilities.replaceNonAlphanumeric("A?bc!#345", '-'));
        assertEquals("A bc345", Utilities.replaceNonAlphanumeric("A?bc''345", ' '));
    }

    @Test
    public void testGetQueryString() {
        Map<String, String> params = new LinkedHashMap<>();
        assertEquals("", Utilities.getQueryString(params));
        params.put("foo", "12");
        assertEquals("?foo=12", Utilities.getQueryString(params));
        params.put("bar", "yes");
        assertEquals("?foo=12&bar=yes", Utilities.getQueryString(params));
        params.put("bar2", "open");
        assertEquals("?foo=12&bar=yes&bar2=open", Utilities.getQueryString(params));
    }

    private abstract static class TestDevice implements Device {
        @Override
        public boolean isNormal() {
            return false;
        }

        @Override
        public boolean isMobile() {
            return false;
        }

        @Override
        public boolean isTablet() {
            return false;
        }

        @Override
        public DevicePlatform getDevicePlatform() {
            return null;
        }
    }

    @Test
    public void testGetDeviceType() {
        Device normalDevice = new TestDevice() {
            @Override
            public boolean isNormal() {
                return true;
            }
        };

        Device mobileDevice = new TestDevice() {
            @Override
            public boolean isMobile() {
                return true;
            }
        };

        Device tabletDevice = new TestDevice() {
            @Override
            public boolean isTablet() {
                return true;
            }
        };

        // normal device
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getAttribute(DeviceUtils.CURRENT_DEVICE_ATTRIBUTE)).thenReturn(normalDevice);
        assertEquals(DeviceType.NORMAL, Utilities.getDeviceType(mockRequest));

        // tablet device
        when(mockRequest.getAttribute(DeviceUtils.CURRENT_DEVICE_ATTRIBUTE)).thenReturn(tabletDevice);
        assertEquals(DeviceType.TABLET, Utilities.getDeviceType(mockRequest));

        // mobile device
        when(mockRequest.getAttribute(DeviceUtils.CURRENT_DEVICE_ATTRIBUTE)).thenReturn(mobileDevice);
        assertEquals(DeviceType.MOBILE, Utilities.getDeviceType(mockRequest));

        // null = normal device
        when(mockRequest.getAttribute(DeviceUtils.CURRENT_DEVICE_ATTRIBUTE)).thenReturn(null);
        assertEquals(DeviceType.NORMAL, Utilities.getDeviceType(mockRequest));
    }

    @Test
    public void testParseURLDate() {

        // parse 6 digit YYYYMM
        assertEquals(LocalDate.of(1858, 10, 1), Utilities.parseURLDate("185810"));
        assertEquals(LocalDate.of(1858, 4, 1), Utilities.parseURLDate("185804"));

        // parse 8 digit YYYYMMDD
        assertEquals(LocalDate.of(1858, 10, 4), Utilities.parseURLDate("18581004"));
        assertEquals(LocalDate.of(1858, 4, 12), Utilities.parseURLDate("18580412"));
        assertEquals(LocalDate.of(1858, 4, 5), Utilities.parseURLDate("18580405"));

        // invalid cases return today's date, skip tests around midnight as value of "now" will change
        ZonedDateTime zdt = ZonedDateTime.now();
        long seconds = Duration.between(zdt, zdt.plusDays(1).truncatedTo(ChronoUnit.DAYS)).getSeconds();
        if (seconds > 60) {
            LocalDate now = LocalDate.now();
            assertEquals(now, Utilities.parseURLDate(null));
            assertEquals(now, Utilities.parseURLDate(""));
            assertEquals(now, Utilities.parseURLDate("abcd"));
            assertEquals(now, Utilities.parseURLDate("20181"));
            assertEquals(now, Utilities.parseURLDate("201815"));
            assertEquals(now, Utilities.parseURLDate("2018041"));
            assertEquals(now, Utilities.parseURLDate("201804247"));
            assertEquals(now, Utilities.parseURLDate("2018a0424"));
        }
    }
}
