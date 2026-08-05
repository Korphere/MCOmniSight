package com.korphere.mcomnisight.provider;

import com.google.gson.JsonObject;
import com.korphere.mcomnisight.Utils;
import com.korphere.mcomnisight.UtilsVelocity;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.korphere.mcomnisight.StatusData.plugin;

public class VMemoryFromRuntimeProvider {
    public static @Nullable JsonObject getMemoryFromRuntime() {
        if (!plugin.getConfigManager().getBoolean("features.performance_v.memory.enabled", true)) {
            return null;
        }
        Runtime runtime = Runtime.getRuntime();
        JsonObject memory = new JsonObject();
        String path = "features.performance_v.memory.";

        Map<String, Supplier<Object>> entryMap = new LinkedHashMap<>();

        entryMap.put("used", () -> Utils.createByteSizeArray(runtime.totalMemory() - runtime.freeMemory()));
        entryMap.put("total", () -> Utils.createByteSizeArray(runtime.totalMemory()));
        entryMap.put("free", () -> Utils.createByteSizeArray(runtime.freeMemory()));
        entryMap.put("max", () -> Utils.createByteSizeArray(runtime.maxMemory()));

        UtilsVelocity.serializeFromMap(entryMap, path);

        return memory;
    }
}
