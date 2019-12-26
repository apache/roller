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

import org.junit.Test;
import org.tightblog.rendering.service.WeblogEntryListGenerator;
import org.tightblog.service.URLService;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogTemplate;
import org.tightblog.rendering.service.WeblogListGenerator;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.service.WeblogManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SiteModelTest {

    @Test
    public void testGetWeblogListData() {
        WeblogListGenerator mockWeblogListGenerator = mock(WeblogListGenerator.class);
        WeblogTemplate testTemplate = new WeblogTemplate();
        testTemplate.setName("bar2");
        Weblog weblog = new Weblog();
        weblog.setHandle("testweblog");
        WeblogPageRequest wpr = new WeblogPageRequest(weblog.getHandle(), null, mock(PageModel.class));
        wpr.setTemplate(testTemplate);
        wpr.setPageNum(5);
        wpr.setWeblog(weblog);
        URLService mockUrlService = mock(URLService.class);
        when(mockUrlService.getCustomPageURL(weblog, "bar2", null))
                .thenReturn("https://foo.com");

        SiteModel siteModel = new SiteModel(wpr);
        siteModel.setUrlService(mockUrlService);
        siteModel.setWeblogEntryListGenerator(mock(WeblogEntryListGenerator.class));
        siteModel.setWeblogListGenerator(mockWeblogListGenerator);
        siteModel.setWeblogManager(mock(WeblogManager.class));

        siteModel.getWeblogListData('R', 24);
        verify(mockWeblogListGenerator).getWeblogsByLetter("https://foo.com", 'R', 5, 24);
    }
}
