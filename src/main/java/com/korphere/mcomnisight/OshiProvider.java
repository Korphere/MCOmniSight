package com.korphere.mcomnisight;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.SystemInfo;
import oshi.hardware.*;

import java.util.*;
import java.util.function.Supplier;

import static com.korphere.mcomnisight.StatusData.plugin;

public class OshiProvider {
    private static final SystemInfo si = new SystemInfo();
    private static final HardwareAbstractionLayer hal = si.getHardware();
    private static final CentralProcessor cpu = hal.getProcessor();
    private static final CentralProcessor.ProcessorIdentifier identifier = cpu.getProcessorIdentifier();
    private static final GlobalMemory mem = hal.getMemory();
    private static final ComputerSystem cs = hal.getComputerSystem();

    private static volatile double lastCpuLoad = 0.0;
    private static long[] lastTicks;

    static {
        lastTicks = cpu.getSystemCpuLoadTicks();
    }

    public static void updateMetrics() {
        lastCpuLoad = cpu.getSystemCpuLoadBetweenTicks(lastTicks) * 100;
        lastTicks = cpu.getSystemCpuLoadTicks();
    }

    public static @Nullable JsonObject getHostData() {
        if (!plugin.getConfig().getBoolean("features.host_status.enabled", true)) {
            return null;
        }
        String path = "features.host_status.";

        Map<String, Supplier<Object>> entryMap = new LinkedHashMap<>();

        entryMap.put("cpu_context_switches", cpu::getContextSwitches);
        entryMap.put("cpu_load", () -> Math.round(lastCpuLoad));
        entryMap.put("cpu_interrupts", cpu::getInterrupts);
        entryMap.put("cpu_logical_processor_count", cpu::getLogicalProcessorCount);
        entryMap.put("cpu_max_freq", cpu::getMaxFreq);
        entryMap.put("cpu_physical_package_count", cpu::getPhysicalPackageCount);
        entryMap.put("cpu_current_freq", cpu::getCurrentFreq);
        //entryMap.put("cpu_feature_flags", cpu::getFeatureFlags);
        entryMap.put("cpu_feature_flags", () -> "Unsupported (Library Version Conflict)");
        entryMap.put("cpu_physical_processor_count", cpu::getPhysicalProcessorCount);
        entryMap.put("cpu_physical_processors", cpu::getPhysicalProcessors);
        entryMap.put("cpu_processor_caches", cpu::getProcessorCaches);
        entryMap.put("cpu_processor_cpu_load_ticks", cpu::getProcessorCpuLoadTicks);
        entryMap.put("cpu_logical_processors", cpu::getLogicalProcessors);
        entryMap.put("mem_available", mem::getAvailable);
        entryMap.put("mem_page_size", mem::getPageSize);
        entryMap.put("physical_memory", mem::getPhysicalMemory);
        entryMap.put("mem_total", mem::getTotal);
        entryMap.put("virtual_memory", mem::getVirtualMemory);
        entryMap.put("cs_baseboard", cs::getBaseboard);
        entryMap.put("cs_firmware", cs::getFirmware);
        entryMap.put("cs_hardware_uuid", cs::getHardwareUUID);
        entryMap.put("cs_manufacturer", cs::getManufacturer);
        entryMap.put("cs_model", cs::getModel);
        entryMap.put("cs_serial_number", cs::getSerialNumber);
        entryMap.put("identifier", identifier::getIdentifier);
        entryMap.put("identifier_model", identifier::getModel);
        entryMap.put("identifier_family", identifier::getFamily);
        entryMap.put("identifier_name", identifier::getName);
        entryMap.put("identifier_arch", identifier::getMicroarchitecture);
        entryMap.put("identifier_id", identifier::getProcessorID);
        entryMap.put("identifier_stepping", identifier::getStepping);
        entryMap.put("identifier_vendor", identifier::getVendor);
        entryMap.put("identifier_vendor_freq", identifier::getVendorFreq);
        entryMap.put("identifier_is_cpu_64bit", identifier::isCpu64bit);
        entryMap.put("gpus", () -> {
            JsonArray gpuArray = new JsonArray();
            for (Map<String, Object> gpu : getGpuInfo()) {
                JsonObject gpuObj = new JsonObject();
                gpuObj.addProperty("name", (String) gpu.get("name"));
                gpuObj.add("vram", (JsonArray) gpu.get("vram"));
                gpuArray.add(gpuObj);
            }
            return gpuArray;
        });
        entryMap.put("net_interfaces", () -> {
            JsonArray netArray = new JsonArray();
            for (NetworkIF net : hal.getNetworkIFs()) {
                JsonObject netObj = new JsonObject();
                netObj.addProperty("name", net.getName());
                netObj.addProperty("rx_bytes", net.getBytesRecv());
                netObj.addProperty("tx_bytes", net.getBytesSent());
                netObj.addProperty("display_name", net.getDisplayName());
                netObj.addProperty("collisions", net.getCollisions());
                netObj.addProperty("if_alias", net.getIfAlias());
                netObj.addProperty("if_type", net.getIfType());
                netObj.addProperty("index", net.getIndex());
                netObj.addProperty("in_drops", net.getInDrops());
                netObj.addProperty("in_errors", net.getInErrors());
                netObj.addProperty("mac_addr", net.getMacaddr());
                netObj.addProperty("mtu", net.getMTU());
                netObj.addProperty("ndis_physical_medium_type", net.getNdisPhysicalMediumType());
                netObj.addProperty("out_errors", net.getOutErrors());
                netObj.addProperty("rx_packets", net.getPacketsRecv());
                netObj.addProperty("tx_packets", net.getPacketsSent());
                netObj.addProperty("speed", net.getSpeed());
                netObj.addProperty("timestamp", net.getTimeStamp());
                netObj.addProperty("is_connector_present", net.isConnectorPresent());
                netObj.addProperty("is_known_vm_mac_addr", net.isKnownVmMacAddr());
                netObj.addProperty("if_oper_status", net.getIfOperStatus().name());
                netObj.addProperty("query_network_interface", net.queryNetworkInterface().getName());
                netArray.add(netObj);
            }
            return netArray;
        });
        entryMap.put("disks", () -> {
            JsonArray diskArray = new JsonArray();
            for (HWDiskStore disk : hal.getDiskStores()) {
                JsonObject diskObj = new JsonObject();
                diskObj.addProperty("model", disk.getModel());
                diskObj.addProperty("size", disk.getSize());
                diskObj.addProperty("name", disk.getName());
                diskObj.addProperty("current_queue_length", disk.getCurrentQueueLength());
                diskObj.addProperty("timestamp", disk.getTimeStamp());
                diskObj.addProperty("read_bytes", disk.getReadBytes());
                diskObj.addProperty("reads", disk.getReads());
                diskObj.addProperty("serial", disk.getSerial());
                diskObj.addProperty("transfer_time", disk.getTransferTime());
                diskObj.addProperty("write_bytes", disk.getWriteBytes());
                diskObj.addProperty("writes", disk.getWrites());
                diskArray.add(diskObj);
            }
            return diskArray;
        });

        return Utils.serializeFromMap(entryMap, path);
    }

    public static @NotNull List<Map<String, Object>> getGpuInfo() {
        List<Map<String, Object>> gpuList = new ArrayList<>();

        List<GraphicsCard> cards = hal.getGraphicsCards();

        for (GraphicsCard card : cards) {
            Map<String, Object> gpuData = new HashMap<>();

            gpuData.put("name", card.getName());
            gpuData.put("vendor", card.getVendor());
            gpuData.put("id", card.getDeviceId());
            gpuData.put("vram", createGpuVramArray(card.getVRam()));
            gpuData.put("version", card.getVersionInfo());

            gpuList.add(gpuData);
        }

        return gpuList;
    }

    private static @NotNull JsonArray createGpuVramArray(long bytes) {
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