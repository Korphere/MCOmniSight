package com.korphere.mcomnisight;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.korphere.mcomnisight.StatusData.plugin;

public class MemoryFromRuntimeProvider {
    public static @Nullable JsonObject getMemoryFromRuntime() {
        if (!plugin.getConfig().getBoolean("features.performance.memory.enabled", true)) {
            return null;
        }
        Runtime runtime = Runtime.getRuntime();
        JsonObject memory = new JsonObject();
        String path = "features.performance.memory.";

        Map<String, Supplier<Object>> entryMap = new LinkedHashMap<>();

        entryMap.put("used", () -> createMemoryArray(runtime.totalMemory() - runtime.freeMemory()));
        entryMap.put("total", () -> createMemoryArray(runtime.totalMemory()));
        entryMap.put("free", () -> createMemoryArray(runtime.freeMemory()));
        entryMap.put("max", () -> createMemoryArray(runtime.maxMemory()));

        Utils.serializeFromMap(entryMap, path);

        return memory;
    }

    private static @NotNull JsonArray createMemoryArray(long bytes) {
        JsonArray array = new JsonArray();
        array.add(bytes);                               // B
        array.add(bytes / 1024);                        // KiB (Binary)
        array.add(bytes / 1000);                        // KB (Decimal)
        array.add(bytes / 1024 / 1024);                 // MiB
        array.add(bytes / 1000 / 1000);                 // MB
        array.add(bytes / 1024 / 1024 / 1024);          // GiB
        array.add(bytes / 1000 / 1000 / 1000);          // GB
        return array;
    }
}
