package com.w2w.model;

public abstract class Item {

    public enum Status {
        AVAILABLE, PENDING, CLAIMED, EXCHANGED, CANCELLED
    }

    public enum Category {
        PERISHABLE, FURNITURE, ELECTRONICS, CLOTHING, BOOKS, TOYS, OTHER
    }

    protected int id;
    protected String name;
    protected String description;
    protected Category category;
    protected int ownerId;
    protected String ownerName;
    protected Status status;
    protected boolean exchangeable;

    public Item() {}

    public Item(String name, String description, Category category, int ownerId, boolean exchangeable) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.ownerId = ownerId;
        this.status = Status.AVAILABLE;
        this.exchangeable = exchangeable;
    }

    // Abstract method — each subclass describes itself
    public abstract String getItemType();

    // Common display method
    public String getSummary() {
        return String.format("[%s #%d] %s | Category: %s | Status: %s | Exchangeable: %s | Owner: %s",
                getItemType(), id, name, category, status, exchangeable ? "Yes" : "No", ownerName != null ? ownerName : "ID:" + ownerId);
    }

    public boolean isAvailable() {
        return status == Status.AVAILABLE;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public boolean isExchangeable() { return exchangeable; }
    public void setExchangeable(boolean exchangeable) { this.exchangeable = exchangeable; }
}
