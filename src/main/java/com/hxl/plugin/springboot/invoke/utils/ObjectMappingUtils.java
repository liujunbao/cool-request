package com.hxl.plugin.springboot.invoke.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ObjectMappingUtils {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper getInstance() {
        return objectMapper;
    }

    public static String format(String source) {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        Object jsonObject = null;
        try {
            jsonObject = objectMapper.readValue(source, Object.class);
            return objectMapper.writeValueAsString(jsonObject);
        } catch (JsonProcessingException ignored) {
        }
        return source;
    }
}