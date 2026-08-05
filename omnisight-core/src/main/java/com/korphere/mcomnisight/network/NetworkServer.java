package com.korphere.mcomnisight.network;

public interface NetworkServer {
    void start();
    void stop(int timeoutMs) throws InterruptedException;
    void broadcast(String text);
    void broadcast(byte[] bytes);
    int getConnectedClientsCount();
    void updateSettings();
}