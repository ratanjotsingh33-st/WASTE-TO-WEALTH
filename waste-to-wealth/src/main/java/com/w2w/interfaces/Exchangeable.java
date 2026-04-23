package com.w2w.interfaces;

public interface Exchangeable {
    boolean requestExchange(int requesterId, String offeredItemDescription);
    boolean isExchangeable();
}
