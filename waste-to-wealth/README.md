# ♻️ Waste-to-Wealth Community Exchange

A Java console application that enables community members to **give away, claim, or exchange items** instead of discarding them — promoting reuse and sustainability.

---

## 📦 Project Structure

```
waste-to-wealth/
├── src/main/java/com/w2w/
│   ├── Main.java                        ← Entry point
│   ├── db/
│   │   └── DatabaseConnection.java      ← SQLite singleton
│   ├── interfaces/
│   │   ├── Claimable.java               ← claimItem() contract
│   │   └── Exchangeable.java            ← requestExchange() contract
│   ├── model/
│   │   ├── User.java                    ← User entity
│   │   ├── Item.java                    ← Abstract base class
│   │   ├── PerishableItem.java          ← Food/perishables (implements Claimable)
│   │   ├── Furniture.java               ← Furniture (Claimable + Exchangeable)
│   │   ├── Electronics.java             ← Electronics (Claimable + Exchangeable)
│   │   ├── GenericItem.java             ← Clothing/Books/Toys/Other
│   │   └── Transaction.java             ← Swap/claim transaction entity
│   ├── service/
│   │   ├── AuthService.java             ← Register, login, logout, rating
│   │   └── ItemService.java             ← CRUD, claim, exchange, resolve
│   ├── ui/
│   │   └── ConsoleUI.java               ← Full interactive menu system
│   └── util/
│       ├── NotificationService.java     ← Console-based notifications
│       └── Validator.java               ← Input validation helpers
├── pom.xml                              ← Maven build (optional)
├── run.sh                               ← Linux/macOS build & run
└── run.bat                              ← Windows build & run
```

---

## 🚀 How to Run

### Prerequisites
- **JDK 17 or higher** ([Download](https://adoptium.net/))
- Internet access (first run only — to download SQLite JDBC driver)

### Linux / macOS
```bash
chmod +x run.sh
./run.sh
```

### Windows
```bat
run.bat
```

### Using Maven
```bash
mvn package
java -jar target/waste-to-wealth-all.jar
```

### Manual compile
```bash
# After placing sqlite-jdbc-3.45.1.0.jar in libs/
mkdir out
find src -name "*.java" | xargs javac -cp libs/sqlite-jdbc-3.45.1.0.jar -d out
java -cp "out:libs/sqlite-jdbc-3.45.1.0.jar" com.w2w.Main
```

---

## 🎮 Usage Guide

### 1. Register & Login
```
1. Register   → Enter Name, Email, Password (min 6 chars)
2. Login      → Enter Email + Password
```

### 2. List an Item
```
Main Menu → 4. List a New Item
  Choose type: Perishable / Furniture / Electronics / Other
  Enter name, description
  For Perishables: set expiry date (YYYY-MM-DD)
  For Furniture/Electronics: mark as exchangeable (y/n)
```

### 3. Browse & Claim
```
Main Menu → 1. Browse Available Items
  View all listings → Enter Item ID
  Action: C=Claim  E=Exchange (if allowed)
```

### 4. Manage Your Requests (as owner)
```
Main Menu → 7. Manage Incoming Requests
  View pending claims/exchanges on your items
  Enter Transaction ID → C=Complete  X=Cancel
```

### 5. Search & Filter
```
2. Search Items       → keyword search by name/description
3. Filter by Category → PERISHABLE, FURNITURE, ELECTRONICS, etc.
```

### 6. Rate Other Users
```
Main Menu → 8. Rate a User → Enter User ID + 1-5 stars
```

---

## 🧩 OOP Design

### Inheritance Hierarchy
```
Item (abstract)
 ├── PerishableItem   implements Claimable
 ├── Furniture        implements Claimable, Exchangeable
 ├── Electronics      implements Claimable, Exchangeable
 └── GenericItem      implements Claimable, Exchangeable
```

### Interfaces
| Interface      | Method                                          |
|----------------|-------------------------------------------------|
| `Claimable`    | `claimItem(int requesterId): boolean`           |
| `Exchangeable` | `requestExchange(int requesterId, String): boolean` |

### State Machine
```
AVAILABLE ──► PENDING ──► COMPLETED
                  └──────► CANCELLED ──► AVAILABLE (re-listed)
```

---

## 🗄️ Database Schema (SQLite)

### `users`
| Column        | Type    |
|---------------|---------|
| id            | INTEGER PK |
| name          | TEXT    |
| email         | TEXT UNIQUE |
| password      | TEXT    |
| rating        | REAL    |
| rating_count  | INTEGER |

### `items`
| Column         | Type    |
|----------------|---------|
| id             | INTEGER PK |
| name           | TEXT    |
| description    | TEXT    |
| category       | TEXT    |
| item_type      | TEXT    |
| owner_id       | FK → users |
| status         | TEXT (AVAILABLE/PENDING/CLAIMED/EXCHANGED/CANCELLED) |
| is_exchangeable| INTEGER |
| expiry_date    | TEXT    |

### `transactions`
| Column       | Type    |
|--------------|---------|
| id           | INTEGER PK |
| item_id      | FK → items |
| requester_id | FK → users |
| owner_id     | FK → users |
| type         | TEXT (CLAIM/EXCHANGE) |
| status       | TEXT (PENDING/COMPLETED/CANCELLED) |

---

## ✨ Features Summary

| Feature                        | Status |
|-------------------------------|--------|
| User Registration & Login      | ✅ |
| Session Handling               | ✅ |
| Input Validation               | ✅ |
| Add / Edit / Delete Items      | ✅ |
| Browse Available Items         | ✅ |
| Search by Keyword              | ✅ |
| Filter by Category             | ✅ |
| Claim Items                    | ✅ |
| Request Exchange               | ✅ |
| Concurrent Claim Protection    | ✅ (DB transaction lock) |
| State Machine (Pending→Done)   | ✅ |
| Owner Approve/Cancel Requests  | ✅ |
| Console Notifications          | ✅ |
| Perishable Expiry Detection    | ✅ |
| Expiry Warning (≤3 days)       | ✅ |
| User Rating System             | ✅ |
| SQLite Persistence             | ✅ |
| OOP Inheritance & Interfaces   | ✅ |

---

## 🔒 Security Notes

- Passwords are hashed using `hashCode()` (sufficient for demo use)
- For production: replace with **BCrypt** (`org.mindrot:jbcrypt`)
- Database file: `waste_to_wealth.db` created in working directory
- Foreign key constraints enforced via `PRAGMA foreign_keys = ON`
