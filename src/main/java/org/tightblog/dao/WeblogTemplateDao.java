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
package org.tightblog.dao;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.tightblog.domain.Template;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogTemplate;

import java.util.List;

@Repository
public interface WeblogTemplateDao extends JpaRepository<WeblogTemplate, String> {

    @Cacheable(value = "weblogTemplatesByName")
    WeblogTemplate findByWeblogAndName(Weblog weblog, String name);

    @Cacheable(value = "weblogTemplatesByRole")
    WeblogTemplate findByWeblogAndRole(Weblog weblog, Template.Role role);

    @Cacheable(value = "weblogTemplates", key = "#weblog.handle")
    // Select all but the template source (latter obtainable individually by methods above)
    // https://stackoverflow.com/a/47471486/1207540
    @Query("SELECT new org.tightblog.domain.WeblogTemplate(w.id, w.role, w.name, w.description, " +
            "w.lastModified) FROM WeblogTemplate w WHERE w.weblog = ?1")
    List<WeblogTemplate> getWeblogTemplateMetadata(Weblog weblog);

    @Transactional("transactionManager")
    void deleteByWeblog(Weblog weblog);

    @CacheEvict(value = "weblogTemplates", key = "#weblog.handle")
    default void evictWeblogTemplates(Weblog weblog) {
        // ignored
    }

    @CacheEvict(value = "weblogTemplatesByName")
    default void evictWeblogTemplateByName(Weblog weblog, String name) {
    }

    @CacheEvict(value = "weblogTemplatesByRole")
    default void evictWeblogTemplateByRole(Weblog weblog, Template.Role role) {
    }
}
