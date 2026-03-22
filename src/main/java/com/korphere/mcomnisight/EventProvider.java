package com.korphere.mcomnisight;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class EventProvider {
    public static @NotNull JsonObject createEventPacket(String eventType) {
        JsonObject packet = new JsonObject();
        packet.addProperty("packet_type", "EVENT");
        packet.addProperty("event_type", eventType);
        packet.add("payload", new JsonObject());
        packet.addProperty("timestamp", System.currentTimeMillis());
        return packet;
    }
}
