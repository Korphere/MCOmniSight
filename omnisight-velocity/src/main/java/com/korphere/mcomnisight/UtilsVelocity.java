package com.korphere.mcomnisight;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

import static com.korphere.mcomnisight.StatusData.plugin;
import static com.korphere.mcomnisight.Utils.handleValue;

public class UtilsVelocity {

    public static @NotNull JsonObject serializeFromMap(@NotNull Map<String, Supplier<Object>> entryMap, @NotNull String configPath) {
        JsonObject data = new JsonObject();
        ConfigManager config = plugin.getConfigManager();

        for (Map.Entry<String, Supplier<Object>> entry : entryMap.entrySet()) {
            String key = entry.getKey();
            String fullPath = configPath + key;

            boolean isEnabled = config.getBoolean(fullPath, true);

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
        ConfigManager config = plugin.getConfigManager();

        for (Map.Entry<String, Supplier<Object>> entry : entryMap.entrySet()) {
            String key = entry.getKey();

            if (config.getBoolean(configPath + key, true)) {
                Object value = entry.getValue().get();
                if (value != null) {
                    handleValue(target, key, value);
                }
            }
        }
    }
}