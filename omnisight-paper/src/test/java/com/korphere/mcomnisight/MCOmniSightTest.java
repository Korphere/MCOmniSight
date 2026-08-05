package com.korphere.mcomnisight;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import static org.junit.jupiter.api.Assertions.*;

class MCOmniSightTest {
    private ServerMock server;
    private MCOmniSightPaper plugin;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(MCOmniSightPaper.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void testConfigDefaults() {
        assertFalse(plugin.getConfig().getBoolean("oshi_details.processes"));
    }
}