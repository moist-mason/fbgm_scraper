package com.github.moistmason.scraper.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.function.Function;

public class JsonUtil {
    public static final int EMPTY_INT = -99999;

    public static <T> JsonArray toJsonArray(final List<T> list, final Function<T, JsonObject> function) {
        JsonArray jsonArray = new JsonArray();
        list.forEach(element -> jsonArray.add(function.apply(element)));
        return jsonArray;
    }
}
