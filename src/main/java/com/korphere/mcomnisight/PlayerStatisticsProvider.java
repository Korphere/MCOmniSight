package com.korphere.mcomnisight;

import com.google.gson.JsonObject;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.korphere.mcomnisight.StatusData.plugin;

public class PlayerStatisticsProvider {
    public static @Nullable JsonObject getPlayerStatistics(@NotNull Player player) {
        if (!plugin.getConfig().getBoolean("features.players.players_list.player_data.statistics.enabled", true)) {
            return null;
        }
        String path = "features.players.players_list.player_data.statistics.";

        Map<String, Supplier<Object>> entryMap = new LinkedHashMap<>();
        
        entryMap.put("player_kills", () -> player.getStatistic(Statistic.PLAYER_KILLS));
        entryMap.put("animals_bred", () -> player.getStatistic(Statistic.ANIMALS_BRED));
        entryMap.put("armor_cleaned", () -> player.getStatistic(Statistic.ARMOR_CLEANED));
        entryMap.put("aviate_one_cm", () -> player.getStatistic(Statistic.AVIATE_ONE_CM));
        entryMap.put("banner_cleaned", () -> player.getStatistic(Statistic.BANNER_CLEANED));
        entryMap.put("beacon_interaction", () -> player.getStatistic(Statistic.BEACON_INTERACTION));
        entryMap.put("bell_ring", () -> player.getStatistic(Statistic.BELL_RING));
        entryMap.put("boat_one_cm", () -> player.getStatistic(Statistic.BOAT_ONE_CM));
        entryMap.put("break_item", () -> player.getStatistic(Statistic.BREAK_ITEM));
        entryMap.put("brewing_stand_interaction", () -> player.getStatistic(Statistic.BREWINGSTAND_INTERACTION));
        entryMap.put("cake_slices_eaten", () -> player.getStatistic(Statistic.CAKE_SLICES_EATEN));
        entryMap.put("cauldron_filled", () -> player.getStatistic(Statistic.CAULDRON_FILLED));
        entryMap.put("cauldron_used", () -> player.getStatistic(Statistic.CAULDRON_USED));
        entryMap.put("chest_opened", () -> player.getStatistic(Statistic.CHEST_OPENED));
        entryMap.put("clean_shulker_box", () -> player.getStatistic(Statistic.CLEAN_SHULKER_BOX));
        entryMap.put("climb_one_cm", () -> player.getStatistic(Statistic.CLIMB_ONE_CM));
        entryMap.put("craft_item", () -> player.getStatistic(Statistic.CRAFT_ITEM));
        entryMap.put("crafting_table_interaction", () -> player.getStatistic(Statistic.CRAFTING_TABLE_INTERACTION));
        entryMap.put("crouch_one_cm", () -> player.getStatistic(Statistic.CROUCH_ONE_CM));
        entryMap.put("damage_absorbed", () -> player.getStatistic(Statistic.DAMAGE_ABSORBED));
        entryMap.put("damage_blocked_by_shield", () -> player.getStatistic(Statistic.DAMAGE_BLOCKED_BY_SHIELD));
        entryMap.put("damage_dealt", () -> player.getStatistic(Statistic.DAMAGE_DEALT));
        entryMap.put("damage_dealt_absorbed", () -> player.getStatistic(Statistic.DAMAGE_DEALT_ABSORBED));
        entryMap.put("damage_dealt_resisted", () -> player.getStatistic(Statistic.DAMAGE_DEALT_RESISTED));
        entryMap.put("damage_resisted", () -> player.getStatistic(Statistic.DAMAGE_RESISTED));
        entryMap.put("damage_taken", () -> player.getStatistic(Statistic.DAMAGE_TAKEN));
        entryMap.put("deaths", () -> player.getStatistic(Statistic.DEATHS));
        entryMap.put("dispenser_inspected", () -> player.getStatistic(Statistic.DISPENSER_INSPECTED));
        entryMap.put("drop", () -> player.getStatistic(Statistic.DROP));
        entryMap.put("drop_count", () -> player.getStatistic(Statistic.DROP_COUNT));
        entryMap.put("dropper_inspected", () -> player.getStatistic(Statistic.DROPPER_INSPECTED));
        entryMap.put("ender_chest_opened", () -> player.getStatistic(Statistic.ENDERCHEST_OPENED));
        entryMap.put("entity_killed_by", () -> player.getStatistic(Statistic.ENTITY_KILLED_BY));
        entryMap.put("fall_one_cm", () -> player.getStatistic(Statistic.FALL_ONE_CM));
        entryMap.put("fish_caught", () -> player.getStatistic(Statistic.FISH_CAUGHT));
        entryMap.put("flower_potted", () -> player.getStatistic(Statistic.FLOWER_POTTED));
        entryMap.put("fly_one_cm", () -> player.getStatistic(Statistic.FLY_ONE_CM));
        entryMap.put("furnace_interaction", () -> player.getStatistic(Statistic.FURNACE_INTERACTION));
        entryMap.put("hopper_inspected", () -> player.getStatistic(Statistic.HOPPER_INSPECTED));
        entryMap.put("horse_one_cm", () -> player.getStatistic(Statistic.HORSE_ONE_CM));
        entryMap.put("interact_with_anvil", () -> player.getStatistic(Statistic.INTERACT_WITH_ANVIL));
        entryMap.put("interact_with_blast_furnace", () -> player.getStatistic(Statistic.INTERACT_WITH_BLAST_FURNACE));
        entryMap.put("interact_with_campfire", () -> player.getStatistic(Statistic.INTERACT_WITH_CAMPFIRE));
        entryMap.put("interact_with_cartography_table", () -> player.getStatistic(Statistic.INTERACT_WITH_CARTOGRAPHY_TABLE));
        entryMap.put("interact_with_grindstone", () -> player.getStatistic(Statistic.INTERACT_WITH_GRINDSTONE));
        entryMap.put("interact_with_lectern", () -> player.getStatistic(Statistic.INTERACT_WITH_LECTERN));
        entryMap.put("interact_with_loom", () -> player.getStatistic(Statistic.INTERACT_WITH_LOOM));
        entryMap.put("interact_with_smithing_table", () -> player.getStatistic(Statistic.INTERACT_WITH_SMITHING_TABLE));
        entryMap.put("interact_with_smoker", () -> player.getStatistic(Statistic.INTERACT_WITH_SMOKER));
        entryMap.put("interact_with_stonecutter", () -> player.getStatistic(Statistic.INTERACT_WITH_STONECUTTER));
        entryMap.put("item_enchanted", () -> player.getStatistic(Statistic.ITEM_ENCHANTED));
        entryMap.put("jump", () -> player.getStatistic(Statistic.JUMP));
        entryMap.put("kill_entity", () -> player.getStatistic(Statistic.KILL_ENTITY));
        entryMap.put("leave_game", () -> player.getStatistic(Statistic.LEAVE_GAME));
        entryMap.put("mine_block", () -> player.getStatistic(Statistic.MINE_BLOCK));
        entryMap.put("minecart_one_cm", () -> player.getStatistic(Statistic.MINECART_ONE_CM));
        entryMap.put("mob_kills", () -> player.getStatistic(Statistic.MOB_KILLS));
        entryMap.put("note_block_played", () -> player.getStatistic(Statistic.NOTEBLOCK_PLAYED));
        entryMap.put("note_block_tuned", () -> player.getStatistic(Statistic.NOTEBLOCK_TUNED));
        entryMap.put("open_barrel", () -> player.getStatistic(Statistic.OPEN_BARREL));
        entryMap.put("pickup", () -> player.getStatistic(Statistic.PICKUP));
        entryMap.put("pig_one_cm", () -> player.getStatistic(Statistic.PIG_ONE_CM));
        entryMap.put("play_one_minute", () -> player.getStatistic(Statistic.PLAY_ONE_MINUTE));
        entryMap.put("raid_trigger", () -> player.getStatistic(Statistic.RAID_TRIGGER));
        entryMap.put("raid_win", () -> player.getStatistic(Statistic.RAID_WIN));
        entryMap.put("record_played", () -> player.getStatistic(Statistic.RECORD_PLAYED));
        entryMap.put("shulker_box_opened", () -> player.getStatistic(Statistic.SHULKER_BOX_OPENED));
        entryMap.put("sleep_in_bed", () -> player.getStatistic(Statistic.SLEEP_IN_BED));
        entryMap.put("sneak_time", () -> player.getStatistic(Statistic.SNEAK_TIME));
        entryMap.put("sprint_one_cm", () -> player.getStatistic(Statistic.SPRINT_ONE_CM));
        entryMap.put("strider_one_cm", () -> player.getStatistic(Statistic.STRIDER_ONE_CM));
        entryMap.put("swim_one_cm", () -> player.getStatistic(Statistic.SWIM_ONE_CM));
        entryMap.put("talked_to_villager", () -> player.getStatistic(Statistic.TALKED_TO_VILLAGER));
        entryMap.put("target_hit", () -> player.getStatistic(Statistic.TARGET_HIT));
        entryMap.put("time_since_death", () -> player.getStatistic(Statistic.TIME_SINCE_DEATH));
        entryMap.put("time_since_rest", () -> player.getStatistic(Statistic.TIME_SINCE_REST));
        entryMap.put("total_world_time", () -> player.getStatistic(Statistic.TOTAL_WORLD_TIME));
        entryMap.put("traded_with_villager", () -> player.getStatistic(Statistic.TRADED_WITH_VILLAGER));
        entryMap.put("trapped_chest_triggered", () -> player.getStatistic(Statistic.TRAPPED_CHEST_TRIGGERED));
        entryMap.put("use_item", () -> player.getStatistic(Statistic.USE_ITEM));
        entryMap.put("walk_on_water_one_cm", () -> player.getStatistic(Statistic.WALK_ON_WATER_ONE_CM));
        entryMap.put("walk_one_cm", () -> player.getStatistic(Statistic.WALK_ONE_CM));

        return Utils.serializeFromMap(entryMap, path);
    }
}
