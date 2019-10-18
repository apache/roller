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
package org.tightblog.filters;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.support.MultipartFilter;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean charEncodingFilter() {
        FilterRegistrationBean<CharacterEncodingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new CharacterEncodingFilter());
        Map<String, String> filterMap = new HashMap<>();
        filterMap.put("encoding", "UTF-8");
        filterMap.put("forceEncoding", "true");
        bean.setInitParameters(filterMap);
        bean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);
        bean.setOrder(1);
        return bean;
    }

    @Bean
    public FilterRegistrationBean multipartFilter() {
        FilterRegistrationBean<MultipartFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new MultipartFilter());
        bean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);
        bean.setOrder(2);
        return bean;
    }

    @Bean
    public FilterRegistrationBean springSecurityFilter(@Qualifier("springSecurityFilterChain") Filter filter) {
        FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
        bean.setFilter(filter);
        bean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);
        bean.setOrder(3);
        return bean;
    }

    @Bean
    public FilterRegistrationBean bootstrapFilterBean(@Autowired BootstrapFilter bootstrapFilter) {
        FilterRegistrationBean<BootstrapFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(bootstrapFilter);
        bean.setDispatcherTypes(DispatcherType.REQUEST);
        bean.setOrder(4);
        return bean;
    }

    @Bean
    public FilterRegistrationBean requestMappingFilterBean(@Autowired RequestMappingFilter requestMappingFilter) {
        FilterRegistrationBean<RequestMappingFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(requestMappingFilter);
        bean.setDispatcherTypes(DispatcherType.REQUEST);
        bean.setOrder(5);
        return bean;
    }

}
