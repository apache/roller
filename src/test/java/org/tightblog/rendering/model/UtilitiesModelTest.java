/*
 * Copyright 2018 the original author or authors.
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
package org.tightblog.rendering.model;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.MessageSource;
import org.tightblog.domain.Weblog;
import org.tightblog.rendering.requests.WeblogRequest;
import org.tightblog.dao.WebloggerPropertiesDao;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class UtilitiesModelTest {

    private WebloggerPropertiesDao webloggerPropertiesDao;
    private MessageSource messages;

    @Before
    public void setUp() {
        webloggerPropertiesDao = mock(WebloggerPropertiesDao.class);
        messages = mock(MessageSource.class);
    }

    @Test
    public void testFormatTemporal() {
        WeblogRequest weblogRequest = new WeblogRequest();
        Weblog weblog = new Weblog();
        weblog.setLocale("EN");
        weblog.setTimeZone("America/Los_Angeles");
        weblogRequest.setWeblog(weblog);
        Map<String, Object> initData = new HashMap<>();
        initData.put("parsedRequest", weblogRequest);
        UtilitiesModel um = new UtilitiesModel(webloggerPropertiesDao, messages, "2.0");
        um.init(initData);
        // three hours difference between NY and LA
        ZonedDateTime ldt = ZonedDateTime.of(2018, 6, 10, 12, 30, 45, 0,
                ZoneId.of("America/New_York"));
        String test = um.formatTemporal(ldt, "yyyy-MM-dd'T'HH:mm:ss");
        assertEquals("2018-06-10T09:30:45", test);
    }
}
