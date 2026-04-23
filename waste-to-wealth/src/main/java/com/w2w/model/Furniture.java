package com.w2w.model;

import com.w2w.interfaces.Claimable;
import com.w2w.interfaces.Exchangeable;
import com.w2w.service.ItemService;

public class Furniture extends Item implements Claimable, Exchangeable {

    public Furniture() {}

    public Furniture(String name, String description, int ownerId, boolean exchangeable) {
        super(name, description, Category.FURNITURE, ownerId, exchangeable);
    }

    @Override
    public String getItemType() { return "Furniture"; }

    @Override
    public boolean claimItem(int requesterId) {
        if (!isAvailable()) {
            System.out.println("⚠️  Item is not available for claiming.");
            return false;
        }
        return ItemService.getInstance().claimItem(this.id, requesterId);
    }

    @Override
    public String getClaimStatus() {
        return "Status: " + status + " | Type: Furniture";
    }

    @Override
    public boolean requestExchange(int requesterId, String offeredItemDescription) {
        if (!isAvailable()) {
            System.out.println("⚠️  Item is not available for exchange.");
            return false;
        }
        if (!exchangeable) {
            System.out.println("⚠️  Owner has not marked this item as exchangeable.");
            return false;
        }
        return ItemService.getInstance().requestExchange(this.id, requesterId, offeredItemDescription);
    }

    @Override
    public boolean isExchangeable() { return exchangeable; }
}
