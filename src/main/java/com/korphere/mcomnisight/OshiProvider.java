package com.korphere.mcomnisight;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import oshi.SystemInfo;
import oshi.hardware.*;
import oshi.software.os.*;

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
    private static final Sensors sensors = hal.getSensors();
    private static final OperatingSystem os = si.getOperatingSystem();
    private static final OSProcess cp = os.getCurrentProcess();
    private static final OSThread ct = os.getCurrentThread();
    private static final FileSystem fs = os.getFileSystem();
    private static final InternetProtocolStats ips = os.getInternetProtocolStats();
    private static final NetworkParams np = os.getNetworkParams();
    private static final OperatingSystem.OSVersionInfo vi = os.getVersionInfo();
    private static final int bn = os.getBitness();
    private static final int procCnt = os.getProcessCount();
    private static final int pid = os.getProcessId();
    private static final long sysUt = os.getSystemUptime();
    private static final long sysBt = os.getSystemBootTime();
    private static final boolean isElevated = os.isElevated();
    private static final int threadCount = os.getThreadCount();
    private static final int threadId = os.getThreadId();

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
        entryMap.put("cpu_temperature", sensors::getCpuTemperature);
        entryMap.put("cpu_voltage", sensors::getCpuVoltage);
        entryMap.put("fan_speeds", () -> {
            JsonArray fanSpeeds = new JsonArray();
            for (int fanSpeed : sensors.getFanSpeeds()) {
                fanSpeeds.add(fanSpeed);
            }
            return fanSpeeds;
        });
        entryMap.put("gpus", () -> {
            JsonArray gpuArray = new JsonArray();
            for (Map<String, Object> gpu : getGpuInfo()) {
                JsonObject gpuObj = new JsonObject();
                gpuObj.addProperty("name", (String) gpu.get("name"));
                gpuObj.add("vram", (JsonArray) gpu.get("vram"));
                gpuObj.addProperty("size", gpu.size());
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
        entryMap.put("os_bitness", os::getBitness);
        entryMap.put("os_family", os::getFamily);
        entryMap.put("os_manufacturer", os::getManufacturer);
        entryMap.put("cp_affinity_mask", cp::getAffinityMask);
        entryMap.put("cp_bitness", cp::getBitness);
        entryMap.put("cp_context_switches", cp::getContextSwitches);
        entryMap.put("cp_name", cp::getName);
        entryMap.put("cp_args", () -> {
            JsonArray cpArgs = new JsonArray();
            for (String arg : cp.getArguments()) {
                cpArgs.add(arg);
            }
            return cpArgs;
        });
        entryMap.put("cp_bytes_read", cp::getBytesRead);
        entryMap.put("cp_bytes_written", cp::getBytesWritten);
        entryMap.put("cp_cmd_line", cp::getCommandLine);
        entryMap.put("cp_current_working_dir", cp::getCurrentWorkingDirectory);
        entryMap.put("cp_env_vars", () -> {
            JsonObject envVars = new JsonObject();
            cp.getEnvironmentVariables().forEach(envVars::addProperty);
            return envVars;
        });
        entryMap.put("cp_group", cp::getGroup);
        entryMap.put("cp_group_id", cp::getGroupID);
        entryMap.put("cp_hard_open_file_limit", cp::getHardOpenFileLimit);
        entryMap.put("cp_kernel_time", cp::getKernelTime);
        entryMap.put("cp_major_faults", cp::getMajorFaults);
        entryMap.put("cp_minor_faults", cp::getMinorFaults);
        entryMap.put("cp_open_files", cp::getOpenFiles);
        entryMap.put("cp_parent_process_id", cp::getParentProcessID);
        entryMap.put("cp_path", cp::getPath);
        entryMap.put("cp_priority", cp::getPriority);
        entryMap.put("cp_process_cpu_load_cumulative", cp::getProcessCpuLoadCumulative);
        entryMap.put("cp_process_id", cp::getProcessID);
        entryMap.put("cp_resident_set_size", cp::getResidentSetSize);
        entryMap.put("cp_soft_open_file_limit", cp::getSoftOpenFileLimit);
        entryMap.put("cp_start_time", cp::getStartTime);
        entryMap.put("cp_state", () -> cp.getState().name());
        entryMap.put("cp_thread_count", cp::getThreadCount);
        entryMap.put("cp_thread_details", () -> {
            JsonArray threadDetails = new JsonArray();
            for (OSThread threadDetail : cp.getThreadDetails()) {
                JsonObject threadDetailObj = new JsonObject();
                threadDetailObj.addProperty("name", threadDetail.getName());
                threadDetailObj.addProperty("context_switches", threadDetail.getContextSwitches());
                threadDetailObj.addProperty("kernel_time", threadDetail.getKernelTime());
                threadDetailObj.addProperty("major_faults", threadDetail.getMajorFaults());
                threadDetailObj.addProperty("owning_process_id", threadDetail.getOwningProcessId());
                threadDetailObj.addProperty("minor_faults", threadDetail.getMinorFaults());
                threadDetailObj.addProperty("priority", threadDetail.getPriority());
                threadDetailObj.addProperty("start_mem_addr", threadDetail.getStartMemoryAddress());
                threadDetailObj.addProperty("start_time", threadDetail.getStartTime());
                threadDetailObj.addProperty("thread_cpu_load_cumulative", threadDetail.getThreadCpuLoadCumulative());
                threadDetailObj.addProperty("thread_id", threadDetail.getThreadId());
                threadDetailObj.addProperty("up_time", threadDetail.getUpTime());
                threadDetailObj.addProperty("user_time", threadDetail.getUserTime());
                threadDetails.add(threadDetailObj);
            }
            return threadDetails;
        });
        entryMap.put("cp_up_time", cp::getUpTime);
        entryMap.put("cp_user", cp::getUser);
        entryMap.put("cp_user_id", cp::getUserID);
        entryMap.put("cp_user_time", cp::getUserTime);
        entryMap.put("cp_virtual_size", cp::getVirtualSize);
        entryMap.put("ct_user_time", ct::getUserTime);
        entryMap.put("ct_up_time", ct::getUpTime);
        entryMap.put("ct_start_time", ct::getStartTime);
        entryMap.put("ct_thread_id", ct::getThreadId);
        entryMap.put("ct_name", ct::getName);
        entryMap.put("ct_thread_cpu_load_cumulative", ct::getThreadCpuLoadCumulative);
        entryMap.put("ct_start_mem_addr", ct::getStartMemoryAddress);
        entryMap.put("ct_priority", ct::getPriority);
        entryMap.put("ct_minor_faults", ct::getMinorFaults);
        entryMap.put("ct_owning_process_id", ct::getOwningProcessId);
        entryMap.put("ct_major_faults", ct::getMajorFaults);
        entryMap.put("ct_kernel_time", ct::getKernelTime);
        entryMap.put("ct_context_switches", ct::getContextSwitches);
        entryMap.put("ct_state", () -> ct.getState().name());
        entryMap.put("fs_file_stores", () -> {
            JsonArray fileStores = new JsonArray();
            for (OSFileStore fileStore : fs.getFileStores()) {
                JsonObject fileStoreObj = new JsonObject();
                fileStoreObj.addProperty("name", fileStore.getName());
                fileStoreObj.addProperty("description", fileStore.getDescription());
                fileStoreObj.addProperty("type", fileStore.getType());
                fileStoreObj.addProperty("free_inodes", fileStore.getFreeInodes());
                fileStoreObj.addProperty("free_space", fileStore.getFreeSpace());
                fileStoreObj.addProperty("label", fileStore.getLabel());
                fileStoreObj.addProperty("logical_volume", fileStore.getLogicalVolume());
                fileStoreObj.addProperty("mount", fileStore.getMount());
                fileStoreObj.addProperty("options", fileStore.getOptions());
                fileStoreObj.addProperty("total_inodes", fileStore.getTotalInodes());
                fileStoreObj.addProperty("total_space", fileStore.getTotalSpace());
                fileStoreObj.addProperty("usable_space", fileStore.getUsableSpace());
                fileStoreObj.addProperty("uuid", fileStore.getUUID());
                fileStoreObj.addProperty("volume", fileStore.getVolume());
                fileStores.add(fileStoreObj);
            }
            return fileStores;
        });
        entryMap.put("fs_max_file_descriptors", fs::getMaxFileDescriptors);
        entryMap.put("fs_open_file_descriptors", fs::getOpenFileDescriptors);
        entryMap.put("fs_max_file_descriptors_per_process", fs::getMaxFileDescriptorsPerProcess);
        entryMap.put("fss", () -> {
            JsonArray fileStores = new JsonArray();
            for (OSFileStore osfs : fs.getFileStores()) {
                JsonObject osFileStoreObj = new JsonObject();
                osFileStoreObj.addProperty("volume", osfs.getVolume());
                osFileStoreObj.addProperty("uuid", osfs.getUUID());
                osFileStoreObj.addProperty("usable", osfs.getUsableSpace());
                osFileStoreObj.addProperty("total", osfs.getTotalSpace());
                osFileStoreObj.addProperty("total_inodes", osfs.getTotalInodes());
                osFileStoreObj.addProperty("options", osfs.getOptions());
                osFileStoreObj.addProperty("mount", osfs.getMount());
                osFileStoreObj.addProperty("logical_volume", osfs.getLogicalVolume());
                osFileStoreObj.addProperty("label", osfs.getLabel());
                osFileStoreObj.addProperty("free", osfs.getFreeSpace());
                osFileStoreObj.addProperty("free_inodes", osfs.getFreeInodes());
                osFileStoreObj.addProperty("type", osfs.getType());
                osFileStoreObj.addProperty("description", osfs.getDescription());
                osFileStoreObj.addProperty("name", osfs.getName());
                fileStores.add(osFileStoreObj);
            }
            return fileStores;
        });
        entryMap.put("connections", () -> {
            JsonArray fileStores = new JsonArray();
            for (InternetProtocolStats.IPConnection connection : ips.getConnections()) {
                JsonObject connectionObj = new JsonObject();
                connectionObj.addProperty("foreign_port", connection.getForeignPort());
                connectionObj.addProperty("local_port", connection.getLocalPort());
                connectionObj.addProperty("type", connection.getType());
                connectionObj.addProperty("owning_process_id", connection.getowningProcessId());
                connectionObj.addProperty("receive_queue", connection.getReceiveQueue());
                connectionObj.addProperty("transmit_queue", connection.getTransmitQueue());
                connectionObj.addProperty("state", connection.getState().name());
                JsonArray fas = new JsonArray();
                for (byte fa : connection.getForeignAddress()) {
                    fas.add(fa);
                }
                connectionObj.add("foreign_address", fas);
                JsonArray las = new JsonArray();
                for (byte la : connection.getLocalAddress()) {
                    fas.add(la);
                }
                connectionObj.add("local_address", las);

                fileStores.add(connectionObj);
            }
            return fileStores;
        });
        entryMap.put("tcp_v4_stats", () -> {
            JsonObject s = new JsonObject();
            InternetProtocolStats.TcpStats v4s = ips.getTCPv4Stats();
            s.addProperty("connection_failures", v4s.getConnectionFailures());
            s.addProperty("connection_active", v4s.getConnectionsActive());
            s.addProperty("connection_established", v4s.getConnectionsEstablished());
            s.addProperty("in_errors", v4s.getInErrors());
            s.addProperty("connection_passive", v4s.getConnectionsPassive());
            s.addProperty("connection_reset", v4s.getConnectionsReset());
            s.addProperty("out_resets", v4s.getOutResets());
            s.addProperty("segments_received", v4s.getSegmentsReceived());
            s.addProperty("segments_established", v4s.getConnectionsEstablished());
            s.addProperty("segments_retransmitted", v4s.getSegmentsRetransmitted());
            s.addProperty("segments_sent", v4s.getSegmentsSent());
            return s;
        });
        entryMap.put("tcp_v6_stats", () -> {
            JsonObject s = new JsonObject();
            InternetProtocolStats.TcpStats v6s = ips.getTCPv6Stats();
            s.addProperty("connection_failures", v6s.getConnectionFailures());
            s.addProperty("connection_active", v6s.getConnectionsActive());
            s.addProperty("connection_established", v6s.getConnectionsEstablished());
            s.addProperty("in_errors", v6s.getInErrors());
            s.addProperty("connection_passive", v6s.getConnectionsPassive());
            s.addProperty("connection_reset", v6s.getConnectionsReset());
            s.addProperty("out_resets", v6s.getOutResets());
            s.addProperty("segments_received", v6s.getSegmentsReceived());
            s.addProperty("segments_established", v6s.getConnectionsEstablished());
            s.addProperty("segments_retransmitted", v6s.getSegmentsRetransmitted());
            s.addProperty("segments_sent", v6s.getSegmentsSent());
            return s;
        });
        entryMap.put("udp_v4_stats", () -> {
            JsonObject s = new JsonObject();
            InternetProtocolStats.UdpStats v4s = ips.getUDPv4Stats();
            s.addProperty("datagrams_no_port", v4s.getDatagramsNoPort());
            s.addProperty("datagrams_received", v4s.getDatagramsReceived());
            s.addProperty("datagrams_sent", v4s.getDatagramsSent());
            s.addProperty("datagrams_received_errors", v4s.getDatagramsReceivedErrors());
            return s;
        });
        entryMap.put("udp_v6_stats", () -> {
            JsonObject s = new JsonObject();
            InternetProtocolStats.UdpStats v6s = ips.getUDPv6Stats();
            s.addProperty("datagrams_no_port", v6s.getDatagramsNoPort());
            s.addProperty("datagrams_received", v6s.getDatagramsReceived());
            s.addProperty("datagrams_sent", v6s.getDatagramsSent());
            s.addProperty("datagrams_received_errors", v6s.getDatagramsReceivedErrors());
            return s;
        });

        entryMap.put("np_host_name", np::getHostName);
        entryMap.put("np_domain_name", np::getDomainName);
        entryMap.put("np_ipv4_default_gateway", np::getIpv4DefaultGateway);
        entryMap.put("np_ipv6_default_gateway", np::getIpv6DefaultGateway);
        entryMap.put("dns_servers", () -> {
            JsonArray dnsServers = new JsonArray();
            for (String dns: np.getDnsServers()) {
                dnsServers.add(dns);
            }
            return dnsServers;
        });

        entryMap.put("vi_build_number", vi::getBuildNumber);
        entryMap.put("vi_code_name", vi::getCodeName);
        entryMap.put("vi_version", vi::getVersion);

        entryMap.put("bitness", () -> bn);
        entryMap.put("process_count", () -> procCnt);
        entryMap.put("process_id", () -> pid);
        entryMap.put("sys_uptime", () -> sysUt);
        entryMap.put("sys_boot_time", () -> sysBt);
        entryMap.put("is_elevated", () -> isElevated);
        entryMap.put("thread_count", () -> threadCount);
        entryMap.put("thread_id", () -> threadId);

        entryMap.put("processes", () -> {
            JsonArray procs = new JsonArray();
            for (OSProcess proc : os.getProcesses()) {
                JsonObject procObj = new JsonObject();
                procObj.addProperty("cpu_load_between_ticks", proc.getProcessCpuLoadBetweenTicks(proc));
                procObj.addProperty("virtual_size", proc.getVirtualSize());
                procObj.addProperty("user_time", proc.getUserTime());
                procObj.addProperty("user_id", proc.getUserID());
                procObj.addProperty("user", proc.getUser());
                procObj.addProperty("up_time", proc.getUpTime());
                procObj.addProperty("thread_count", proc.getThreadCount());
                procObj.addProperty("state", proc.getState().name());
                procObj.addProperty("start_time", proc.getStartTime());
                procObj.addProperty("soft_open_file_limit", proc.getSoftOpenFileLimit());
                procObj.addProperty("resident_set_size", proc.getResidentSetSize());
                procObj.addProperty("id", proc.getProcessID());
                procObj.addProperty("cpu_load_cumulative", proc.getProcessCpuLoadCumulative());
                procObj.addProperty("priority", proc.getPriority());
                procObj.addProperty("path", proc.getPath());
                procObj.addProperty("parent_id", proc.getParentProcessID());
                procObj.addProperty("open_files", proc.getOpenFiles());
                procObj.addProperty("minor_faults", proc.getMinorFaults());
                procObj.addProperty("major_faults", proc.getMajorFaults());
                procObj.addProperty("kernel_time", proc.getKernelTime());
                procObj.addProperty("hard_open_file_limit", proc.getHardOpenFileLimit());
                procObj.addProperty("group_id", proc.getGroupID());
                procObj.addProperty("group", proc.getGroup());
                procObj.addProperty("current_working_dir", proc.getCurrentWorkingDirectory());
                procObj.addProperty("cmd_line", proc.getCommandLine());
                procObj.addProperty("bytes_written", proc.getBytesWritten());
                procObj.addProperty("bytes_read", proc.getBytesRead());
                procObj.addProperty("name", proc.getName());
                procObj.addProperty("context_switches", proc.getContextSwitches());
                procObj.addProperty("bitness", proc.getBitness());
                procObj.addProperty("affinity_mask", proc.getAffinityMask());
                JsonArray threadDetails = new JsonArray();
                for (OSThread t : proc.getThreadDetails()) {
                    JsonObject tObj = new JsonObject();
                    tObj.addProperty("user_time", t.getUserTime());
                    tObj.addProperty("kernel_time", t.getKernelTime());
                    tObj.addProperty("context_switches", t.getContextSwitches());
                    tObj.addProperty("major_faults", t.getMajorFaults());
                    tObj.addProperty("owning_process_id", t.getOwningProcessId());
                    tObj.addProperty("state", t.getState().name());
                    tObj.addProperty("minor_faults", t.getMinorFaults());
                    tObj.addProperty("priority", t.getPriority());
                    tObj.addProperty("start_mem_address", t.getStartMemoryAddress());
                    tObj.addProperty("cpu_load_cumulative", t.getThreadCpuLoadCumulative());
                    tObj.addProperty("name", t.getName());
                    tObj.addProperty("id", t.getThreadId());
                    tObj.addProperty("start_time", t.getStartTime());
                    tObj.addProperty("up_time", t.getUpTime());

                    threadDetails.add(tObj);
                }
                procObj.add("thread_details", threadDetails);

                procs.add(procObj);
            }
            return procs;
        });
        entryMap.put("services", () -> {
            JsonArray procs = new JsonArray();
            for (OSService service : os.getServices()) {
                JsonObject serviceObj = new JsonObject();
                serviceObj.addProperty("name", service.getName());
                serviceObj.addProperty("id", service.getProcessID());

                procs.add(serviceObj);
            }
            return procs;
        });
        entryMap.put("sessions", () -> {
            JsonArray procs = new JsonArray();
            for (OSSession session : os.getSessions()) {
                JsonObject sessionObj = new JsonObject();
                sessionObj.addProperty("host", session.getHost());
                sessionObj.addProperty("login_time", session.getLoginTime());
                sessionObj.addProperty("terminal_device", session.getTerminalDevice());
                sessionObj.addProperty("user_name", session.getUserName());

                procs.add(sessionObj);
            }
            return procs;
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
            gpuData.put("vram", Utils.createByteSizeArray(card.getVRam()));
            gpuData.put("version", card.getVersionInfo());

            gpuList.add(gpuData);
        }

        return gpuList;
    }
}