/*
 * Copyright 2017 the original author or authors.
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
package org.tightblog.rendering.cache;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Caffeine's CacheStats not Jackson-friendly, as-is usage in AdminController returns this error:
 * org.springframework.http.converter.HttpMessageNotWritableException: Could not write JSON: No
 * serializer found for class com.github.benmanes.caffeine.cache.stats.CacheStats and no properties
 * discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS);
 * nested exception is com.fasterxml.jackson.databind.JsonMappingException: No serializer found for
 * class com.github.benmanes.caffeine.cache.stats.CacheStats and no properties discovered to create BeanSerializer.
 *
 * This custom serializer fixes this problem.
 */
@Component
public class CacheStatsSerializer extends JsonSerializer<CacheStats> {

    @Override
    public Class<CacheStats> handledType() {
        return CacheStats.class;
    }

    @Override
    public void serialize(CacheStats cs, JsonGenerator jgen, SerializerProvider sP) throws IOException {
        jgen.writeStartObject();
        jgen.writeNumberField("requestCount", cs.requestCount());
        jgen.writeNumberField("hits", cs.hitCount());
        jgen.writeNumberField("misses", cs.missCount());
        jgen.writeNumberField("puts", cs.loadSuccessCount());
        jgen.writeNumberField("removes", cs.evictionCount());
        jgen.writeNumberField("efficiency", cs.hitRate());
        jgen.writeEndObject();
    }
}
