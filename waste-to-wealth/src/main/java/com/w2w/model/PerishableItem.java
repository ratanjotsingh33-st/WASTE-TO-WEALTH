package com.w2w.model;

import com.w2w.interfaces.Claimable;
import com.w2w.service.ItemService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class PerishableItem extends Item implements Claimable {

    private String expiryDate;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public PerishableItem() {}

    public PerishableItem(String name, String description, int ownerId, String expiryDate) {
        super(name, description, Category.PERISHABLE, ownerId, false);
        this.expiryDate = expiryDate;
    }

    @Override
    public String getItemType() { return "PerishableItem"; }

    public boolean isExpired() {
        if (expiryDate == null || expiryDate.isEmpty()) return false;
        try {
            return LocalDate.parse(expiryDate, FMT).isBefore(LocalDate.now());
        } catch (Exception e) {
            return false;
        }
    }

    public long daysUntilExpiry() {
        if (expiryDate == null || expiryDate.isEmpty()) return Long.MAX_VALUE;
        try {
            return ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(expiryDate, FMT));
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    @Override
    public boolean claimItem(int requesterId) {
        if (!isAvailable()) {
            System.out.println("⚠️  Item is not available for claiming.");
            return false;
        }
        if (isExpired()) {
            System.out.println("⚠️  This item has expired and cannot be claimed.");
            return false;
        }
        return ItemService.getInstance().claimItem(this.id, requesterId);
    }

    @Override
    public String getClaimStatus() {
        return "Status: " + status + (expiryDate != null ? " | Expires: " + expiryDate : "");
    }

    @Override
    public String getSummary() {
        String expiry = expiryDate != null ? " | Expires: " + expiryDate : "";
        String expired = isExpired() ? " ⚠️ EXPIRED" : (daysUntilExpiry() <= 3 ? " ⚡ Expiring Soon!" : "");
        return super.getSummary() + expiry + expired;
    }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
}
