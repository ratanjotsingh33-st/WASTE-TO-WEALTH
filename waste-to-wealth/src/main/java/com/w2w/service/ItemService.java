package com.w2w.service;

import com.w2w.db.DatabaseConnection;
import com.w2w.model.*;
import com.w2w.util.NotificationService;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ItemService {
    private static ItemService instance;
    private final Connection conn;

    private ItemService() {
        conn = DatabaseConnection.getInstance().getConnection();
    }

    public static ItemService getInstance() {
        if (instance == null) instance = new ItemService();
        return instance;
    }

    // ─── ADD ITEM ──────────────────────────────────────────────────────────────
    public Item addItem(Item item) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO items (name, description, category, item_type, owner_id, status, is_exchangeable, expiry_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, item.getName());
            ps.setString(2, item.getDescription());
            ps.setString(3, item.getCategory().name());
            ps.setString(4, item.getItemType());
            ps.setInt(5, item.getOwnerId());
            ps.setString(6, item.getStatus().name());
            ps.setInt(7, item.isExchangeable() ? 1 : 0);
            if (item instanceof PerishableItem pi) {
                ps.setString(8, pi.getExpiryDate());
            } else {
                ps.setNull(8, Types.VARCHAR);
            }
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                item.setId(keys.getInt(1));
                // Fetch owner name for notification
                AuthService auth = AuthService.getInstance();
                User owner = auth.getUserById(item.getOwnerId());
                String ownerName = owner != null ? owner.getName() : "Unknown";
                NotificationService.notifyItemListed(ownerName, item.getName());
                // Warn if perishable expiring soon
                if (item instanceof PerishableItem pi && pi.daysUntilExpiry() <= 3 && !pi.isExpired()) {
                    NotificationService.notifyExpiringItem(item.getName(), pi.daysUntilExpiry());
                }
                return item;
            }
        } catch (SQLException e) {
            System.out.println("❌ Failed to add item: " + e.getMessage());
        }
        return null;
    }

    // ─── GET ALL AVAILABLE ITEMS ───────────────────────────────────────────────
    public List<Item> getAllAvailableItems() {
        return fetchItems("SELECT i.*, u.name AS owner_name FROM items i JOIN users u ON i.owner_id = u.id WHERE i.status = 'AVAILABLE' ORDER BY i.created_at DESC");
    }

    // ─── GET ITEMS BY CATEGORY ─────────────────────────────────────────────────
    public List<Item> getItemsByCategory(Item.Category category) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT i.*, u.name AS owner_name FROM items i JOIN users u ON i.owner_id = u.id " +
                "WHERE i.status = 'AVAILABLE' AND i.category = ? ORDER BY i.created_at DESC"
            );
            ps.setString(1, category.name());
            return extractItems(ps.executeQuery());
        } catch (SQLException e) {
            System.out.println("❌ Filter error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ─── GET MY ITEMS ──────────────────────────────────────────────────────────
    public List<Item> getItemsByOwner(int ownerId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT i.*, u.name AS owner_name FROM items i JOIN users u ON i.owner_id = u.id " +
                "WHERE i.owner_id = ? ORDER BY i.created_at DESC"
            );
            ps.setInt(1, ownerId);
            return extractItems(ps.executeQuery());
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ─── SEARCH ────────────────────────────────────────────────────────────────
    public List<Item> searchItems(String keyword) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT i.*, u.name AS owner_name FROM items i JOIN users u ON i.owner_id = u.id " +
                "WHERE i.status = 'AVAILABLE' AND (LOWER(i.name) LIKE ? OR LOWER(i.description) LIKE ?) ORDER BY i.created_at DESC"
            );
            String kw = "%" + keyword.toLowerCase() + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            return extractItems(ps.executeQuery());
        } catch (SQLException e) {
            System.out.println("❌ Search error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ─── GET ITEM BY ID ────────────────────────────────────────────────────────
    public Item getItemById(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT i.*, u.name AS owner_name FROM items i JOIN users u ON i.owner_id = u.id WHERE i.id = ?"
            );
            ps.setInt(1, id);
            List<Item> items = extractItems(ps.executeQuery());
            return items.isEmpty() ? null : items.get(0);
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
            return null;
        }
    }

    // ─── UPDATE ITEM ───────────────────────────────────────────────────────────
    public boolean updateItem(int itemId, int ownerId, String newName, String newDesc, boolean exchangeable) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE items SET name = ?, description = ?, is_exchangeable = ? WHERE id = ? AND owner_id = ?"
            );
            ps.setString(1, newName);
            ps.setString(2, newDesc);
            ps.setInt(3, exchangeable ? 1 : 0);
            ps.setInt(4, itemId);
            ps.setInt(5, ownerId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Item updated successfully.");
                return true;
            } else {
                System.out.println("❌ Item not found or you are not the owner.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Update failed: " + e.getMessage());
        }
        return false;
    }

    // ─── DELETE ITEM ───────────────────────────────────────────────────────────
    public boolean deleteItem(int itemId, int ownerId) {
        try {
            // Check if there's a pending transaction
            PreparedStatement check = conn.prepareStatement(
                "SELECT id FROM transactions WHERE item_id = ? AND status = 'PENDING'"
            );
            check.setInt(1, itemId);
            if (check.executeQuery().next()) {
                System.out.println("⚠️  Cannot delete: there is a pending transaction on this item.");
                return false;
            }
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM items WHERE id = ? AND owner_id = ? AND status = 'AVAILABLE'"
            );
            ps.setInt(1, itemId);
            ps.setInt(2, ownerId);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                System.out.println("✅ Item deleted successfully.");
                return true;
            } else {
                System.out.println("❌ Item not found, not yours, or not available.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Delete failed: " + e.getMessage());
        }
        return false;
    }

    // ─── CLAIM ITEM ────────────────────────────────────────────────────────────
    public boolean claimItem(int itemId, int requesterId) {
        try {
            conn.setAutoCommit(false);

            // Lock check: get current status
            PreparedStatement checkItem = conn.prepareStatement(
                "SELECT status, owner_id, name FROM items WHERE id = ?"
            );
            checkItem.setInt(1, itemId);
            ResultSet rs = checkItem.executeQuery();
            if (!rs.next()) {
                System.out.println("❌ Item not found.");
                conn.rollback(); return false;
            }
            if (!rs.getString("status").equals("AVAILABLE")) {
                System.out.println("❌ Item is no longer available.");
                conn.rollback(); return false;
            }
            int ownerId = rs.getInt("owner_id");
            String itemName = rs.getString("name");

            if (ownerId == requesterId) {
                System.out.println("❌ You cannot claim your own item.");
                conn.rollback(); return false;
            }

            // Update item status to PENDING
            PreparedStatement updateItem = conn.prepareStatement(
                "UPDATE items SET status = 'PENDING' WHERE id = ? AND status = 'AVAILABLE'"
            );
            updateItem.setInt(1, itemId);
            if (updateItem.executeUpdate() == 0) {
                System.out.println("❌ Item was just claimed by someone else.");
                conn.rollback(); return false;
            }

            // Create transaction
            PreparedStatement txn = conn.prepareStatement(
                "INSERT INTO transactions (item_id, requester_id, owner_id, type, status) VALUES (?, ?, ?, 'CLAIM', 'PENDING')"
            );
            txn.setInt(1, itemId);
            txn.setInt(2, requesterId);
            txn.setInt(3, ownerId);
            txn.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);

            // Notifications
            User requester = AuthService.getInstance().getUserById(requesterId);
            User owner = AuthService.getInstance().getUserById(ownerId);
            NotificationService.notifyItemClaimed(
                requester != null ? requester.getName() : "User#" + requesterId,
                itemName,
                owner != null ? owner.getName() : "User#" + ownerId
            );
            System.out.println("✅ Item claimed successfully! Status: PENDING → awaiting owner confirmation.");
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            System.out.println("❌ Claim failed: " + e.getMessage());
            return false;
        }
    }

    // ─── REQUEST EXCHANGE ──────────────────────────────────────────────────────
    public boolean requestExchange(int itemId, int requesterId, String offeredDesc) {
        try {
            conn.setAutoCommit(false);

            PreparedStatement checkItem = conn.prepareStatement(
                "SELECT status, owner_id, name, is_exchangeable FROM items WHERE id = ?"
            );
            checkItem.setInt(1, itemId);
            ResultSet rs = checkItem.executeQuery();
            if (!rs.next()) {
                System.out.println("❌ Item not found."); conn.rollback(); return false;
            }
            if (!rs.getString("status").equals("AVAILABLE")) {
                System.out.println("❌ Item is not available."); conn.rollback(); return false;
            }
            if (rs.getInt("is_exchangeable") == 0) {
                System.out.println("❌ This item is not open for exchange."); conn.rollback(); return false;
            }
            int ownerId = rs.getInt("owner_id");
            String itemName = rs.getString("name");

            if (ownerId == requesterId) {
                System.out.println("❌ You cannot exchange with yourself."); conn.rollback(); return false;
            }

            PreparedStatement updateItem = conn.prepareStatement(
                "UPDATE items SET status = 'PENDING' WHERE id = ? AND status = 'AVAILABLE'"
            );
            updateItem.setInt(1, itemId);
            if (updateItem.executeUpdate() == 0) {
                System.out.println("❌ Item was just taken by someone else."); conn.rollback(); return false;
            }

            PreparedStatement txn = conn.prepareStatement(
                "INSERT INTO transactions (item_id, requester_id, owner_id, type, status) VALUES (?, ?, ?, 'EXCHANGE', 'PENDING')"
            );
            txn.setInt(1, itemId);
            txn.setInt(2, requesterId);
            txn.setInt(3, ownerId);
            txn.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);

            User requester = AuthService.getInstance().getUserById(requesterId);
            NotificationService.notifyExchangeRequested(
                requester != null ? requester.getName() : "User#" + requesterId,
                itemName
            );
            System.out.println("✅ Exchange request sent! Offered: " + offeredDesc);
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            System.out.println("❌ Exchange request failed: " + e.getMessage());
            return false;
        }
    }

    // ─── RESOLVE TRANSACTION ──────────────────────────────────────────────────
    public boolean resolveTransaction(int transactionId, int ownerId, boolean complete) {
        try {
            conn.setAutoCommit(false);

            PreparedStatement getTxn = conn.prepareStatement(
                "SELECT t.*, i.name AS item_name FROM transactions t JOIN items i ON t.item_id = i.id " +
                "WHERE t.id = ? AND t.owner_id = ? AND t.status = 'PENDING'"
            );
            getTxn.setInt(1, transactionId);
            getTxn.setInt(2, ownerId);
            ResultSet rs = getTxn.executeQuery();
            if (!rs.next()) {
                System.out.println("❌ Transaction not found or already resolved.");
                conn.rollback(); return false;
            }

            int itemId = rs.getInt("item_id");
            String itemName = rs.getString("item_name");
            String txnType = rs.getString("type");
            String newTxnStatus = complete ? "COMPLETED" : "CANCELLED";
            String newItemStatus = complete ? (txnType.equals("EXCHANGE") ? "EXCHANGED" : "CLAIMED") : "AVAILABLE";

            PreparedStatement updateTxn = conn.prepareStatement(
                "UPDATE transactions SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?"
            );
            updateTxn.setString(1, newTxnStatus);
            updateTxn.setInt(2, transactionId);
            updateTxn.executeUpdate();

            PreparedStatement updateItem = conn.prepareStatement(
                "UPDATE items SET status = ? WHERE id = ?"
            );
            updateItem.setString(1, newItemStatus);
            updateItem.setInt(2, itemId);
            updateItem.executeUpdate();

            conn.commit();
            conn.setAutoCommit(true);

            if (complete) {
                NotificationService.notifyTransactionCompleted(itemName);
            } else {
                NotificationService.notifyTransactionCancelled(itemName);
            }
            return true;

        } catch (SQLException e) {
            try { conn.rollback(); conn.setAutoCommit(true); } catch (SQLException ignored) {}
            System.out.println("❌ Error: " + e.getMessage());
            return false;
        }
    }

    // ─── GET MY TRANSACTIONS ──────────────────────────────────────────────────
    public List<Transaction> getMyTransactions(int userId) {
        List<Transaction> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT t.*, i.name AS item_name, r.name AS req_name, o.name AS own_name " +
                "FROM transactions t " +
                "JOIN items i ON t.item_id = i.id " +
                "JOIN users r ON t.requester_id = r.id " +
                "JOIN users o ON t.owner_id = o.id " +
                "WHERE t.requester_id = ? OR t.owner_id = ? " +
                "ORDER BY t.created_at DESC"
            );
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setItemId(rs.getInt("item_id"));
                t.setItemName(rs.getString("item_name"));
                t.setRequesterId(rs.getInt("requester_id"));
                t.setRequesterName(rs.getString("req_name"));
                t.setOwnerId(rs.getInt("owner_id"));
                t.setOwnerName(rs.getString("own_name"));
                t.setType(Transaction.Type.valueOf(rs.getString("type")));
                t.setStatus(Transaction.Status.valueOf(rs.getString("status")));
                t.setCreatedAt(rs.getString("created_at"));
                list.add(t);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error fetching transactions: " + e.getMessage());
        }
        return list;
    }

    // ─── PENDING TRANSACTIONS FOR OWNER ───────────────────────────────────────
    public List<Transaction> getPendingTransactionsForOwner(int ownerId) {
        List<Transaction> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT t.*, i.name AS item_name, r.name AS req_name, o.name AS own_name " +
                "FROM transactions t " +
                "JOIN items i ON t.item_id = i.id " +
                "JOIN users r ON t.requester_id = r.id " +
                "JOIN users o ON t.owner_id = o.id " +
                "WHERE t.owner_id = ? AND t.status = 'PENDING' " +
                "ORDER BY t.created_at ASC"
            );
            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction();
                t.setId(rs.getInt("id"));
                t.setItemId(rs.getInt("item_id"));
                t.setItemName(rs.getString("item_name"));
                t.setRequesterId(rs.getInt("requester_id"));
                t.setRequesterName(rs.getString("req_name"));
                t.setOwnerId(rs.getInt("owner_id"));
                t.setOwnerName(rs.getString("own_name"));
                t.setType(Transaction.Type.valueOf(rs.getString("type")));
                t.setStatus(Transaction.Status.valueOf(rs.getString("status")));
                t.setCreatedAt(rs.getString("created_at"));
                list.add(t);
            }
        } catch (SQLException e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
        return list;
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private List<Item> fetchItems(String sql) {
        try {
            return extractItems(conn.createStatement().executeQuery(sql));
        } catch (SQLException e) {
            System.out.println("❌ Error fetching items: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private List<Item> extractItems(ResultSet rs) throws SQLException {
        List<Item> items = new ArrayList<>();
        while (rs.next()) {
            Item item = mapRowToItem(rs);
            if (item != null) items.add(item);
        }
        return items;
    }

    private Item mapRowToItem(ResultSet rs) throws SQLException {
        String type = rs.getString("item_type");
        String cat = rs.getString("category");
        Item item;

        switch (type) {
            case "PerishableItem" -> {
                PerishableItem pi = new PerishableItem();
                pi.setExpiryDate(rs.getString("expiry_date"));
                item = pi;
            }
            case "Furniture" -> item = new Furniture();
            case "Electronics" -> item = new Electronics();
            default -> item = new GenericItem();
        }

        item.setId(rs.getInt("id"));
        item.setName(rs.getString("name"));
        item.setDescription(rs.getString("description"));
        item.setCategory(Item.Category.valueOf(cat));
        item.setOwnerId(rs.getInt("owner_id"));
        item.setStatus(Item.Status.valueOf(rs.getString("status")));
        item.setExchangeable(rs.getInt("is_exchangeable") == 1);
        try { item.setOwnerName(rs.getString("owner_name")); } catch (SQLException ignored) {}
        return item;
    }
}
