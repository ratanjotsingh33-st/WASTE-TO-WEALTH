package com.w2w.ui;

import com.w2w.model.*;
import com.w2w.service.AuthService;
import com.w2w.service.ItemService;
import com.w2w.util.NotificationService;

import java.util.List;
import java.util.Scanner;

public class ConsoleUI {

    private static final String BANNER = """
            ╔══════════════════════════════════════════════════════════════╗
            ║      ♻️   WASTE-TO-WEALTH COMMUNITY EXCHANGE   ♻️             ║
            ║         Give • Claim • Exchange — Not Discard               ║
            ╚══════════════════════════════════════════════════════════════╝
            """;

    private final Scanner sc = new Scanner(System.in);
    private final AuthService authService = AuthService.getInstance();
    private final ItemService itemService = ItemService.getInstance();

    public void start() {
        System.out.println(BANNER);
        boolean running = true;
        while (running) {
            if (!authService.isLoggedIn()) {
                running = showAuthMenu();
            } else {
                running = showMainMenu();
            }
        }
        System.out.println("\n🌍 Thank you for reducing waste! Goodbye!\n");
    }

    // ─── AUTH MENU ────────────────────────────────────────────────────────────
    private boolean showAuthMenu() {
        System.out.println("""
                \n┌─── MAIN MENU ─────────────────┐
                │  1. Register                  │
                │  2. Login                     │
                │  0. Exit                      │
                └───────────────────────────────┘""");
        System.out.print("Choice: ");
        switch (input()) {
            case "1" -> doRegister();
            case "2" -> doLogin();
            case "0" -> { return false; }
            default -> System.out.println("⚠️  Invalid choice.");
        }
        return true;
    }

    // ─── MAIN MENU ────────────────────────────────────────────────────────────
    private boolean showMainMenu() {
        User u = authService.getCurrentUser();
        System.out.printf("""
                \n┌─── LOGGED IN AS: %-22s ─┐
                │  1. Browse Available Items            │
                │  2. Search Items                      │
                │  3. Filter by Category                │
                │  4. List a New Item                   │
                │  5. My Listings                       │
                │  6. My Transactions                   │
                │  7. Manage Incoming Requests          │
                │  8. Rate a User                       │
                │  9. Logout                            │
                │  0. Exit                              │
                └───────────────────────────────────────┘%n""", u.getName());
        System.out.print("Choice: ");
        switch (input()) {
            case "1" -> browsItems();
            case "2" -> searchItems();
            case "3" -> filterByCategory();
            case "4" -> listNewItem();
            case "5" -> myListings();
            case "6" -> myTransactions();
            case "7" -> manageRequests();
            case "8" -> rateUser();
            case "9" -> authService.logout();
            case "0" -> { return false; }
            default -> System.out.println("⚠️  Invalid choice.");
        }
        return true;
    }

    // ─── REGISTER ─────────────────────────────────────────────────────────────
    private void doRegister() {
        System.out.println("\n── Register ──────────────────");
        System.out.print("Full Name    : "); String name = input();
        System.out.print("Email        : "); String email = input();
        System.out.print("Password     : "); String pass = input();
        authService.register(name, email, pass);
    }

    // ─── LOGIN ────────────────────────────────────────────────────────────────
    private void doLogin() {
        System.out.println("\n── Login ─────────────────────");
        System.out.print("Email    : "); String email = input();
        System.out.print("Password : "); String pass = input();
        authService.login(email, pass);
    }

    // ─── BROWSE ITEMS ─────────────────────────────────────────────────────────
    private void browsItems() {
        System.out.println("\n── Available Items ───────────────────────────────");
        List<Item> items = itemService.getAllAvailableItems();
        if (items.isEmpty()) {
            System.out.println("📭 No items available right now.");
            return;
        }
        printItems(items);
        interactWithItem(items);
    }

    // ─── SEARCH ───────────────────────────────────────────────────────────────
    private void searchItems() {
        System.out.print("\nSearch keyword: ");
        String kw = input();
        List<Item> items = itemService.searchItems(kw);
        if (items.isEmpty()) {
            System.out.println("🔍 No items found for: " + kw);
            return;
        }
        System.out.println("🔍 Found " + items.size() + " result(s):");
        printItems(items);
        interactWithItem(items);
    }

    // ─── FILTER ───────────────────────────────────────────────────────────────
    private void filterByCategory() {
        System.out.println("""
                \nCategories:
                  1. PERISHABLE  2. FURNITURE  3. ELECTRONICS
                  4. CLOTHING    5. BOOKS      6. TOYS  7. OTHER""");
        System.out.print("Choose: ");
        Item.Category cat = switch (input()) {
            case "1" -> Item.Category.PERISHABLE;
            case "2" -> Item.Category.FURNITURE;
            case "3" -> Item.Category.ELECTRONICS;
            case "4" -> Item.Category.CLOTHING;
            case "5" -> Item.Category.BOOKS;
            case "6" -> Item.Category.TOYS;
            default -> Item.Category.OTHER;
        };
        List<Item> items = itemService.getItemsByCategory(cat);
        if (items.isEmpty()) {
            System.out.println("📭 No items in category: " + cat);
            return;
        }
        printItems(items);
        interactWithItem(items);
    }

    // ─── LIST NEW ITEM ────────────────────────────────────────────────────────
    private void listNewItem() {
        System.out.println("\n── List a New Item ───────────────────────────────");
        System.out.println("Item Types:  1. Perishable  2. Furniture  3. Electronics  4. Other");
        System.out.print("Type: "); String typeChoice = input();

        System.out.print("Item Name   : "); String name = input();
        System.out.print("Description : "); String desc = input();

        int ownerId = authService.getCurrentUser().getId();
        Item item = null;

        switch (typeChoice) {
            case "1" -> {
                System.out.print("Expiry Date (YYYY-MM-DD, or blank): ");
                String expiry = input();
                item = new PerishableItem(name, desc, ownerId, expiry.isEmpty() ? null : expiry);
            }
            case "2" -> {
                System.out.print("Open for exchange? (y/n): ");
                boolean exc = input().equalsIgnoreCase("y");
                item = new Furniture(name, desc, ownerId, exc);
            }
            case "3" -> {
                System.out.print("Open for exchange? (y/n): ");
                boolean exc = input().equalsIgnoreCase("y");
                item = new Electronics(name, desc, ownerId, exc);
            }
            default -> {
                Item.Category cat = chooseCategory();
                System.out.print("Open for exchange? (y/n): ");
                boolean exc = input().equalsIgnoreCase("y");
                item = new GenericItem(name, desc, cat, ownerId, exc);
            }
        }

        Item saved = itemService.addItem(item);
        if (saved != null) {
            System.out.println("✅ Item listed with ID: " + saved.getId());
        }
    }

    // ─── MY LISTINGS ──────────────────────────────────────────────────────────
    private void myListings() {
        int userId = authService.getCurrentUser().getId();
        List<Item> items = itemService.getItemsByOwner(userId);
        if (items.isEmpty()) { System.out.println("📭 You have no listings."); return; }
        System.out.println("\n── Your Listings ─────────────────────────────────");
        printItems(items);
        System.out.println("\nOptions: E=Edit  D=Delete  B=Back");
        System.out.print("Choice: ");
        String choice = input().toUpperCase();
        if (choice.equals("E")) {
            System.out.print("Item ID to edit: ");
            int id = parseInt(input());
            Item target = itemService.getItemById(id);
            if (target == null || target.getOwnerId() != userId) {
                System.out.println("❌ Item not found or not yours.");
                return;
            }
            System.out.print("New Name (blank to keep): "); String nn = input();
            System.out.print("New Description (blank to keep): "); String nd = input();
            System.out.print("Open for exchange? (y/n): "); boolean exc = input().equalsIgnoreCase("y");
            itemService.updateItem(id, userId,
                nn.isEmpty() ? target.getName() : nn,
                nd.isEmpty() ? target.getDescription() : nd,
                exc);
        } else if (choice.equals("D")) {
            System.out.print("Item ID to delete: ");
            int id = parseInt(input());
            itemService.deleteItem(id, userId);
        }
    }

    // ─── MY TRANSACTIONS ──────────────────────────────────────────────────────
    private void myTransactions() {
        int userId = authService.getCurrentUser().getId();
        List<Transaction> txns = itemService.getMyTransactions(userId);
        if (txns.isEmpty()) { System.out.println("📭 No transactions yet."); return; }
        System.out.println("\n── Your Transactions ─────────────────────────────");
        for (Transaction t : txns) {
            System.out.println("  " + t);
        }
    }

    // ─── MANAGE INCOMING REQUESTS ─────────────────────────────────────────────
    private void manageRequests() {
        int userId = authService.getCurrentUser().getId();
        List<Transaction> pending = itemService.getPendingTransactionsForOwner(userId);
        if (pending.isEmpty()) { System.out.println("📭 No pending requests for your items."); return; }
        System.out.println("\n── Pending Requests on Your Items ────────────────");
        for (Transaction t : pending) {
            System.out.println("  " + t);
        }
        System.out.print("\nEnter Transaction ID to resolve (or 0 to skip): ");
        int txnId = parseInt(input());
        if (txnId == 0) return;
        System.out.print("Action: C=Complete  X=Cancel : ");
        String action = input().toUpperCase();
        if (action.equals("C")) {
            itemService.resolveTransaction(txnId, userId, true);
        } else if (action.equals("X")) {
            itemService.resolveTransaction(txnId, userId, false);
        }
    }

    // ─── RATE USER ────────────────────────────────────────────────────────────
    private void rateUser() {
        System.out.print("User ID to rate: ");
        int userId = parseInt(input());
        if (userId == authService.getCurrentUser().getId()) {
            System.out.println("❌ You cannot rate yourself."); return;
        }
        System.out.print("Rating (1-5 stars): ");
        int stars = parseInt(input());
        if (authService.rateUser(userId, stars)) {
            User rated = authService.getUserById(userId);
            NotificationService.notifyRatingGiven(rated != null ? rated.getName() : "User#" + userId, stars);
            System.out.println("✅ Rating submitted!");
        }
    }

    // ─── INTERACT WITH ITEM ───────────────────────────────────────────────────
    private void interactWithItem(List<Item> items) {
        System.out.print("\nEnter Item ID to Claim/Exchange (or 0 to go back): ");
        int id = parseInt(input());
        if (id == 0) return;

        Item item = items.stream().filter(i -> i.getId() == id).findFirst().orElse(null);
        if (item == null) { System.out.println("❌ Item not found in list."); return; }

        int myId = authService.getCurrentUser().getId();
        if (item.getOwnerId() == myId) { System.out.println("❌ This is your own item."); return; }

        System.out.println("\nItem: " + item.getSummary());
        System.out.print("Action: C=Claim  E=Exchange  B=Back : ");
        String action = input().toUpperCase();

        if (action.equals("C")) {
            if (item instanceof com.w2w.interfaces.Claimable claimable) {
                claimable.claimItem(myId);
            } else {
                itemService.claimItem(item.getId(), myId);
            }
        } else if (action.equals("E")) {
            if (item instanceof com.w2w.interfaces.Exchangeable exchangeable) {
                System.out.print("What will you offer in exchange? ");
                String offer = input();
                exchangeable.requestExchange(myId, offer);
            } else {
                System.out.println("❌ This item type does not support exchange.");
            }
        }
    }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private void printItems(List<Item> items) {
        System.out.println();
        for (int i = 0; i < items.size(); i++) {
            System.out.printf("  %2d. %s%n", i + 1, items.get(i).getSummary());
        }
    }

    private Item.Category chooseCategory() {
        System.out.println("Category: 1.CLOTHING  2.BOOKS  3.TOYS  4.OTHER");
        System.out.print("Choose: ");
        return switch (input()) {
            case "1" -> Item.Category.CLOTHING;
            case "2" -> Item.Category.BOOKS;
            case "3" -> Item.Category.TOYS;
            default -> Item.Category.OTHER;
        };
    }

    private String input() {
        String line = sc.nextLine();
        return line == null ? "" : line.trim();
    }

    private int parseInt(String s) {
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
    }
}
