/*
   Copyright 2018 the original author or authors.

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.tightblog.rendering.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.tightblog.domain.Weblog;
import org.tightblog.dao.WeblogDao;
import org.tightblog.util.Utilities;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class WeblogListGenerator {

    private WeblogDao weblogDao;

    @Autowired
    public WeblogListGenerator(WeblogDao weblogDao) {
        this.weblogDao = weblogDao;
    }

    public List<WeblogData> getHotWeblogs(int length) {
        List<WeblogData> weblogDataList = new ArrayList<>();
        List<Weblog> weblogs = weblogDao.findByVisibleTrueAndHitsTodayGreaterThanOrderByHitsTodayDesc(0,
                PageRequest.of(0, length));
        for (Weblog weblog : weblogs) {
            weblogDataList.add(weblogToWeblogData(weblog));
        }
        return weblogDataList;
    }

    public WeblogListData getWeblogsByLetter(String baseUrl, Character letter, int pageNum, int maxBlogs) {
        WeblogListData weblogListData = new WeblogListData();

        List<Weblog> rawWeblogs;
        if (letter == null) {
            rawWeblogs = weblogDao.findByVisibleTrueOrderByHandle(PageRequest.of(pageNum, maxBlogs + 1));
        } else {
            rawWeblogs = weblogDao.findByLetterOrderByHandle(letter,
                    PageRequest.of(pageNum * maxBlogs, maxBlogs + 1));
        }

        List<WeblogData> weblogList = weblogListData.getWeblogs();
        for (Weblog weblog : rawWeblogs) {
            weblogList.add(weblogToWeblogData(weblog));
            if (weblogList.size() >= maxBlogs) {
                break;
            }
        }

        boolean needNextLink = rawWeblogs.size() > weblogListData.getWeblogs().size();

        if (pageNum > 0 || needNextLink) {
            Map<String, String> params = new HashMap<>();
            if (letter != null) {
                params.put("letter", String.valueOf(letter));
            }

            if (pageNum > 0) {
                params.put("page", "" + (pageNum - 1));
                weblogListData.prevLink = createURL(baseUrl, params);
            }

            if (needNextLink) {
                params.put("page", "" + (pageNum + 1));
                weblogListData.nextLink = createURL(baseUrl, params);
            }
        }

        return weblogListData;
    }

    private String createURL(String urlToCreate, Map<String, String> params) {
        return urlToCreate + Utilities.getQueryString(params);
    }

    private WeblogData weblogToWeblogData(Weblog weblog) {
        WeblogData wd = new WeblogData();
        wd.name = weblog.getName();
        wd.handle = weblog.getHandle();
        wd.about = weblog.getAbout();
        wd.creatorScreenName = weblog.getCreator().getScreenName();
        wd.lastModified = weblog.getLastModified();
        wd.hitsToday = weblog.getHitsToday();
        return wd;
    }

    public static class WeblogListData {
        private String nextLink;
        private String prevLink;
        private List<WeblogData> weblogs = new ArrayList<>();

        public String getNextLink() {
            return nextLink;
        }

        public String getPrevLink() {
            return prevLink;
        }

        public List<WeblogData> getWeblogs() {
            return weblogs;
        }
    }

    public static class WeblogData {
        private String name;
        private String handle;
        private String about;
        private String creatorScreenName;
        private Instant lastModified;
        private int hitsToday;

        public String getName() {
            return name;
        }

        public String getHandle() {
            return handle;
        }

        public String getAbout() {
            return about;
        }

        public String getCreatorScreenName() {
            return creatorScreenName;
        }

        public Instant getLastModified() {
            return lastModified;
        }

        public int getHitsToday() {
            return hitsToday;
        }
    }
}
