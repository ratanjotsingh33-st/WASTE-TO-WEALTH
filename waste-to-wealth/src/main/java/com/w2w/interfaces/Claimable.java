package com.w2w.interfaces;

public interface Claimable {
    boolean claimItem(int requesterId);
    String getClaimStatus();
}
