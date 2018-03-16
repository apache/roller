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
package org.tightblog.rendering.processors;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.tightblog.rendering.model.Model;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.rendering.requests.WeblogPageRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

public class AbstractProcessorTest {

    @Test
    public void testGetModelMap() {
        ApplicationContext mockContext = mock(ApplicationContext.class);
        PageModel pageModel = new PageModel();
        Model mockModel = mock(Model.class);
        when(mockModel.getModelName()).thenReturn("mockModel");
        Set<Model> modelSet = new HashSet<>();
        modelSet.add(pageModel);
        modelSet.add(mockModel);
        when(mockContext.getBean(eq("testBean"), eq(Set.class))).thenReturn(modelSet);
        WeblogPageRequest req = new WeblogPageRequest();
        Map<String, Object> initData = new HashMap<>();
        initData.put("parsedRequest", req);
        PageProcessor processor = new PageProcessor();
        processor.setApplicationContext(mockContext);
        Map<String, Object> modelMap = processor.getModelMap("testBean", initData);
        assertEquals(pageModel, modelMap.get("model"));
        assertEquals(mockModel, modelMap.get("mockModel"));
        verify(mockModel).init(initData);
    }

    @Test
    public void testRespondIfNotModified() throws IOException {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        // test return date if valid
        Instant now = Instant.now();
        when(mockRequest.getDateHeader("If-Modified-Since")).thenReturn(now.toEpochMilli());
        long val = PageProcessor.getBrowserCacheExpireDate(mockRequest);
        assertEquals(now.toEpochMilli(), val);

        // test return -1 on invalid date
        when(mockRequest.getDateHeader("If-Modified-Since")).thenThrow(new IllegalArgumentException());
        val = PageProcessor.getBrowserCacheExpireDate(mockRequest);
        assertEquals(-1, val);
    }

}
