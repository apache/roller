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

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.tightblog.domain.MediaDirectory;
import org.tightblog.domain.Weblog;

import java.util.List;

@Repository
public interface MediaDirectoryDao extends JpaRepository<MediaDirectory, String> {

    MediaDirectory findByWeblogAndName(Weblog weblog, String directoryName);

    List<MediaDirectory> findByWeblog(Weblog weblog);

    default MediaDirectory findByIdOrNull(String id) {
        return findById(id).orElse(null);
    }
}
