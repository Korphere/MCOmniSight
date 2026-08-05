package com.korphere.mcomnisight.provider;

public class OshiRunner implements MetricsRunner {
    @Override
    public void update() {
        OshiProvider.updateMetrics();
    }
}