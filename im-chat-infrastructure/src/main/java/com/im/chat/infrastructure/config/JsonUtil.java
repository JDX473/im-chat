package com.im.chat.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Shared Jackson ObjectMapper for the infrastructure layer.
 */
public final class JsonUtil {

    private static final ObjectMapper om = new ObjectMapper();

    static {
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
    }

    private JsonUtil() {}

    public static ObjectMapper instance() {
        return om;
    }
}
