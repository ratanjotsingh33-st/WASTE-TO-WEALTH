package com.w2w.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class NotificationService {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static void print(String icon, String type, String message) {
        String time = LocalTime.now().format(TIME_FMT);
        System.out.printf("%n╔══════════════════════════════════════════════════╗%n");
        System.out.printf("║ %s [%s] %s - %s%n", icon, time, type, message);
        System.out.printf("╚══════════════════════════════════════════════════╝%n");
    }

    public static void notifyItemListed(String ownerName, String itemName) {
        print("📢", "NEW ITEM", ownerName + " listed: " + itemName);
    }

    public static void notifyItemClaimed(String claimerName, String itemName, String ownerName) {
        print("🤝", "ITEM CLAIMED", claimerName + " claimed '" + itemName + "' from " + ownerName);
    }

    public static void notifyExchangeRequested(String requesterName, String itemName) {
        print("🔄", "EXCHANGE REQUEST", requesterName + " requested exchange for '" + itemName + "'");
    }

    public static void notifyTransactionCompleted(String itemName) {
        print("✅", "TRANSACTION DONE", "Transaction for '" + itemName + "' marked COMPLETED");
    }

    public static void notifyTransactionCancelled(String itemName) {
        print("❌", "CANCELLED", "Transaction for '" + itemName + "' was CANCELLED");
    }

    public static void notifyExpiringItem(String itemName, long daysLeft) {
        print("⚡", "EXPIRY WARNING", itemName + " expires in " + daysLeft + " day(s)!");
    }

    public static void notifyRatingGiven(String ratedUser, int stars) {
        print("⭐", "RATING", ratedUser + " received " + stars + " star(s)");
    }
}
