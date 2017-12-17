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
