package com.korphere.mcomnisight;

import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.korphere.mcomnisight.StatusData.plugin;
import static com.korphere.mcomnisight.Utils.addNestedProperty;

public class WorldDataProvider {
    public static @Nullable JsonObject getWorldData() {
        if (!plugin.getConfig().getBoolean("features.world_data.enabled", true)) {
            return null;
        }

        JsonObject worldsJson = new JsonObject();
        String path = "features.world_data.per_world_settings.";

        for (World world : Bukkit.getWorlds()) {
            JsonObject worldJson = new JsonObject();
            Map<String, Supplier<Object>> dataDefinitions = new LinkedHashMap<>();

            dataDefinitions.put("environment", () -> world.getEnvironment().name());
            dataDefinitions.put("seed", world::getSeed);
            dataDefinitions.put("difficulty", () -> world.getDifficulty().name());
            dataDefinitions.put("difficulty_translation_key", () -> world.getDifficulty().translationKey());
            dataDefinitions.put("is_hardcore", world::isHardcore);
            dataDefinitions.put("allow_animals", world::getAllowAnimals);
            dataDefinitions.put("allow_monsters", world::getAllowMonsters);
            dataDefinitions.put("time.game_time", world::getGameTime);
            dataDefinitions.put("time.ticks", world::getTime);
            dataDefinitions.put("time.full_time", world::getFullTime);
            dataDefinitions.put("time.day_count", () -> world.getFullTime() / 24000);
            dataDefinitions.put("weather.has_storm", world::hasStorm);
            dataDefinitions.put("weather.has_thunder", world::isThundering);
            dataDefinitions.put("weather.clear_weather_duration", world::getClearWeatherDuration);
            dataDefinitions.put("chunks.loaded_count", () -> world.getLoadedChunks().length);
            dataDefinitions.put("chunks.chunk_count", world::getChunkCount);

            dataDefinitions.put("entities.total", world::getEntityCount);
            dataDefinitions.put("entities.living_count", () -> world.getLivingEntities().size());
            dataDefinitions.put("entities.tile_entity_count", world::getTileEntityCount);

            dataDefinitions.put("world_border.size", () -> world.getWorldBorder().getSize());
            dataDefinitions.put("world_border.center_x", () -> world.getWorldBorder().getCenter().getX());
            dataDefinitions.put("world_border.center_z", () -> world.getWorldBorder().getCenter().getZ());

            dataDefinitions.put("tile_entity_count", world::getTileEntityCount);

            dataDefinitions.put("can_generate_structures", world::canGenerateStructures);

            //これリストだからJsonArrayにして取り出すようにする。あとconfig.ymlに追記すること
            //dataDefinitions.put("biome.biomes", () -> world.getBiomeProvider() != null ? world.getBiomeProvider().getBiomes(world) : "NONE");

            dataDefinitions.put("is_day_time", world::isDayTime);
            dataDefinitions.put("is_auto_save", world::isAutoSave);

            for (Map.Entry<String, Supplier<Object>> entry : dataDefinitions.entrySet()) {
                if (plugin.getConfig().getBoolean(path + entry.getKey(), true)) {
                    addNestedProperty(worldJson, entry.getKey(), entry.getValue().get());
                }
            }

            worldsJson.add(world.getName(), worldJson);
        }

        return worldsJson;
    }
}