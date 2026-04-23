package com.w2w.model;

public class Transaction {

    public enum Type { CLAIM, EXCHANGE }
    public enum Status { PENDING, COMPLETED, CANCELLED }

    private int id;
    private int itemId;
    private String itemName;
    private int requesterId;
    private String requesterName;
    private int ownerId;
    private String ownerName;
    private Type type;
    private Status status;
    private String createdAt;

    public Transaction() {}

    public Transaction(int itemId, int requesterId, int ownerId, Type type) {
        this.itemId = itemId;
        this.requesterId = requesterId;
        this.ownerId = ownerId;
        this.type = type;
        this.status = Status.PENDING;
    }

    @Override
    public String toString() {
        return String.format(
            "Txn #%d | %s | Item: %s | Requester: %s | Owner: %s | Status: %s | Date: %s",
            id,
            type,
            itemName != null ? itemName : "ID:" + itemId,
            requesterName != null ? requesterName : "ID:" + requesterId,
            ownerName != null ? ownerName : "ID:" + ownerId,
            status,
            createdAt
        );
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public int getRequesterId() { return requesterId; }
    public void setRequesterId(int requesterId) { this.requesterId = requesterId; }

    public String getRequesterName() { return requesterName; }
    public void setRequesterName(String requesterName) { this.requesterName = requesterName; }

    public int getOwnerId() { return ownerId; }
    public void setOwnerId(int ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
