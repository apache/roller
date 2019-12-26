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
package org.tightblog.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.tightblog.rendering.cache.LazyExpiringCache;

@Configuration
@ImportResource({ "classpath:spring-beans.xml", "classpath*:tightblog-custom.xml" })
public class AppConfig {

    @Bean
    ThreadPoolTaskScheduler blogTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(6);
        return scheduler;
    }

    @Bean
    public CacheManager cacheManager() {
        return new CaffeineCacheManager();
    }

    @Bean
    public LazyExpiringCache weblogPageCache(
            @Value("${weblogPageCache.maxEntries:400}") int maxEntries,
            @Value("${weblogPageCache.timeoutHours:48}") int timeoutHours) {
        return new LazyExpiringCache("cache.weblogpage", maxEntries, timeoutHours);
    }

    @Bean
    public LazyExpiringCache weblogFeedCache(
        @Value("${weblogFeedCache.maxEntries:200}") int maxEntries,
        @Value("${weblogFeedCache.timeoutHours:48}") int timeoutHours) {
        return new LazyExpiringCache("cache.weblogfeed", maxEntries, timeoutHours);
    }

    @Bean
    public LazyExpiringCache weblogMediaCache() {
        // Media images uncached, cache used for 304 stats gathering only.
        return new LazyExpiringCache("cache.weblogmedia", 0, 1);
    }

    @Bean
    public LazyExpiringCache githubSourceCache(
            @Value("${githubSourceCache.maxEntries:500}") int maxEntries,
            @Value("${githubSourceCache.timeoutHours:6}") int timeoutHours) {
        return new LazyExpiringCache("cache.githubsource", maxEntries, timeoutHours);
    }
}
