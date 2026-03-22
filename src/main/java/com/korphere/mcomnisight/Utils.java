package com.korphere.mcomnisight;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

import static com.korphere.mcomnisight.StatusData.plugin;

public class Utils {
    static void addNestedProperty(@NotNull JsonObject root, @NotNull String key, Object value) {
        String[] parts = key.split("\\.");
        JsonObject current = root;

        for (int i = 0; i < parts.length - 1; i++) {
            String part = parts[i];
            if (!current.has(part) || !current.get(part).isJsonObject()) {
                current.add(part, new JsonObject());
            }
            current = current.getAsJsonObject(part);
        }

        String lastKey = parts[parts.length - 1];

        handleValue(current, lastKey, value);
    }

    public static @NotNull JsonObject serializeFromMap(@NotNull Map<String, Supplier<Object>> entryMap, @NotNull String configPath) {
        JsonObject data = new JsonObject();

        for (Map.Entry<String, Supplier<Object>> entry : entryMap.entrySet()) {
            String key = entry.getKey();
            String fullPath = configPath + key;
            boolean isEnabled = plugin.getConfig().isBoolean(fullPath)
                    ? plugin.getConfig().getBoolean(fullPath)
                    : plugin.getConfig().getBoolean(fullPath + ".enabled", true);

            if (isEnabled) {
                Object value = entry.getValue().get();
                if (value != null) {
                    handleValue(data, key, value);
                }
            }
        }
        return data;
    }

    public static void addPropertiesFromMap(@NotNull Map<String, Supplier<Object>> entryMap, @NotNull String configPath, JsonObject target) {
        for (Map.Entry<String, Supplier<Object>> entry : entryMap.entrySet()) {
            String key = entry.getKey();
            if (plugin.getConfig().getBoolean(configPath + key, true)) {
                Object value = entry.getValue().get();

                handleValue(target, key, value);
            }
        }
    }

    private static void handleValue(JsonObject current, String lastKey, Object value) {
        switch (value) {
            case null -> current.add(lastKey, null);
            case JsonArray array -> current.add(lastKey, array);
            case JsonObject object -> current.add(lastKey, object);
            case JsonElement element -> current.add(lastKey, element);
            case Number number -> current.addProperty(lastKey, number);
            case Boolean b -> current.addProperty(lastKey, b);
            default -> current.addProperty(lastKey, value.toString());
        }
    }
}