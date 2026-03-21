package com.korphere.mcomnisight;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.korphere.mcomnisight.StatusData.plugin;

public class PlayerInventoryProvider {

    public static @Nullable JsonObject getInventoryData(@NotNull Player player) {
        if (!plugin.getConfig().getBoolean("features.players.players_list.player_data.states.enabled", true)) {
            return null;
        }

        String path = "features.players.players_list.player_data.inventory.";
        PlayerInventory inv = player.getInventory();

        Map<String, Supplier<Object>> entryMap = new LinkedHashMap<>();

        entryMap.put("main", () -> serializeContents(inv.getContents()));
        entryMap.put("armor", () -> serializeContents(inv.getArmorContents()));
        entryMap.put("off_hand", () -> serializeItem(inv.getItemInOffHand()));
        entryMap.put("main_hand", () -> serializeItem(inv.getItemInMainHand()));
        entryMap.put("held_item_slot", inv::getHeldItemSlot);
        entryMap.put("extra", () -> serializeContents(inv.getExtraContents()));
        entryMap.put("size", inv::getSize);
        entryMap.put("max_stack_size", inv::getMaxStackSize);

        return Utils.serializeFromMap(entryMap, path);
    }

    private static @NotNull JsonArray serializeContents(ItemStack @NotNull [] items) {
        JsonArray array = new JsonArray();
        for (int i = 0; i < items.length; i++) {
            ItemStack item = items[i];
            if (item != null && item.getType() != Material.AIR) {
                JsonObject itemObj = serializeItem(item);
                itemObj.addProperty("slot", i);
                array.add(itemObj);
            }
        }
        return array;
    }

    private static @NotNull JsonObject serializeItem(ItemStack item) {
        JsonObject obj = new JsonObject();
        if (item == null || item.getType() == Material.AIR) {
            obj.addProperty("type", "AIR");
            return obj;
        }

        obj.addProperty("type", item.getType().toString());
        obj.addProperty("amount", item.getAmount());
        obj.addProperty("max_stack_size", item.getMaxStackSize());

        String name = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName())
                : item.getType().toString();
        obj.addProperty("name", name);

        return obj;
    }
}