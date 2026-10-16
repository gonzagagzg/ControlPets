package com.itq.util;

import com.google.gson.*;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public final class JsonUtil {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, t, c) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, t, c) -> LocalDate.parse(json.getAsString()))
            .registerTypeAdapter(OffsetDateTime.class, (JsonSerializer<OffsetDateTime>) (src, t, c) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(OffsetDateTime.class, (JsonDeserializer<OffsetDateTime>) (json, t, c) -> OffsetDateTime.parse(json.getAsString()))
            .serializeNulls().create();
    private JsonUtil() {}
    public static Gson gson() { return GSON; }
}
