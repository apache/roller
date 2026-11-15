/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

package org.apache.roller.weblogger.ui.struts2.ajax;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.SharedTheme;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.ThemeResource;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * theme.xml can name a preview image that isn't there; the theme data must
 * still be served, without a preview path.
 */
public class ThemeDataServletTest {

    private static SharedTheme theme(String id, String previewPath) {
        SharedTheme theme = mock(SharedTheme.class);
        when(theme.getId()).thenReturn(id);
        when(theme.getName()).thenReturn(id + " \"name\"");
        when(theme.getDescription()).thenReturn("first line\nsecond line");
        if (previewPath != null) {
            ThemeResource preview = mock(ThemeResource.class);
            when(preview.getPath()).thenReturn(previewPath);
            when(theme.getPreviewImage()).thenReturn(preview);
        }
        return theme;
    }

    private static JsonNode themeData(String themeId) throws Exception {
        ThemeManager themes = mock(ThemeManager.class);
        SharedTheme withPreview = theme("basic", "sm-theme-basic.png");
        SharedTheme withoutPreview = theme("andreas08", null);
        when(themes.getEnabledThemesList()).thenReturn(List.of(withPreview, withoutPreview));
        when(themes.getTheme("basic")).thenReturn(withPreview);
        when(themes.getTheme("andreas08")).thenReturn(withoutPreview);
        Weblogger weblogger = mock(Weblogger.class);
        when(weblogger.getThemeManager()).thenReturn(themes);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getParameter("theme")).thenReturn(themeId);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter body = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        try (MockedStatic<WebloggerFactory> factory = mockStatic(WebloggerFactory.class)) {
            factory.when(WebloggerFactory::getWeblogger).thenReturn(weblogger);
            new ThemeDataServlet().doGet(request, response);
        }
        return new ObjectMapper().readTree(body.toString());
    }

    @Test
    public void aThemeWithoutAPreviewImageHasNoPreviewPath() throws Exception {
        JsonNode theme = themeData("andreas08");
        assertEquals("andreas08", theme.get("id").asText());
        assertTrue(theme.get("previewPath").isNull());
    }

    @Test
    public void aThemeWithAPreviewImageHasItsPath() throws Exception {
        assertEquals("/themes/basic/sm-theme-basic.png",
                themeData("basic").get("previewPath").asText());
    }

    @Test
    public void themeMetadataWithQuotesAndNewlinesIsValidJson() throws Exception {
        JsonNode theme = themeData("basic");
        assertEquals("basic \"name\"", theme.get("name").asText());
        assertEquals("first line\nsecond line", theme.get("description").asText());
    }

    @Test
    public void theThemeListIncludesThemesWithoutAPreviewImage() throws Exception {
        JsonNode list = themeData(null);
        assertEquals(2, list.size());
        assertEquals("/themes/basic/sm-theme-basic.png", list.get(0).get("previewPath").asText());
        assertTrue(list.get(1).get("previewPath").isNull());
    }
}
