package com.example.app;

import annotations.JsonField;
import annotations.JsonIgnore;
import java.lang.reflect.Field;

public class RuntimeJsonSerializer {

    public static String toJson(Object obj) throws IllegalAccessException {
        StringBuilder json = new StringBuilder();
        json.append("{");

        boolean first = true;
        for (Field field : obj.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(JsonIgnore.class)) {
                continue;
            }

            field.setAccessible(true);
            Object value = field.get(obj);

            if (!first) json.append(",");

            String fieldName = field.getName();
            if (field.isAnnotationPresent(JsonField.class)) {
                JsonField ann = field.getAnnotation(JsonField.class);
                if (!ann.name().isEmpty()) {
                    fieldName = ann.name();
                }
            }

            json.append("\"").append(fieldName).append("\":");
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else {
                json.append(value);
            }

            first = false;
        }

        json.append("}");
        return json.toString();
    }
}