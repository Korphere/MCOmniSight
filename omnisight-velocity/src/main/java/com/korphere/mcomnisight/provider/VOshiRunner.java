package com.korphere.mcomnisight.provider;

public class VOshiRunner implements VMetricsRunner {
    @Override
    public void update() {
        VOshiProvider.updateMetrics();
    }
}