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
package org.tightblog.rendering.controller;

import org.tightblog.rendering.model.Model;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class AbstractController implements ApplicationContextAware {

    private ApplicationContext appContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        appContext = applicationContext;
    }

    Map<String, Object> getModelMap(String modelBean, Map<String, Object> initData) {
        Map<String, Object> modelMap = new HashMap<>();
        Set modelSet = appContext.getBean(modelBean, Set.class);
        for (Object obj : modelSet) {
            Model m = (Model) obj;
            m.init(initData);
            modelMap.put(m.getModelName(), m);
        }
        return modelMap;
    }

    static long getBrowserCacheExpireDate(HttpServletRequest request) {
        long sinceDate = -1;
        try {
            sinceDate = request.getDateHeader("If-Modified-Since");
        } catch (IllegalArgumentException ex) {
            // this indicates there was some problem parsing the header value as a date
        }
        return sinceDate;
    }
}
