#!/usr/bin/env bash
# ============================================================
#  Waste-to-Wealth Community Exchange — Build & Run Script
# ============================================================
set -e

SQLITE_JAR_URL="https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar"
SQLITE_JAR="libs/sqlite-jdbc-3.45.1.0.jar"
SRC_DIR="src/main/java"
OUT_DIR="out"
MAIN_CLASS="com.w2w.Main"

echo ""
echo "♻️  Waste-to-Wealth Community Exchange"
echo "======================================="

# ── 1. Check Java ──────────────────────────────────────────
if ! command -v javac &> /dev/null; then
    echo "❌ javac not found. Install JDK 17+ and try again."
    echo "   Ubuntu/Debian : sudo apt install default-jdk"
    echo "   macOS         : brew install openjdk@17"
    exit 1
fi
echo "✅ Java compiler: $(javac -version 2>&1)"

# ── 2. Download SQLite driver if missing ───────────────────
mkdir -p libs
if [ ! -f "$SQLITE_JAR" ]; then
    echo "📥 Downloading SQLite JDBC driver..."
    if command -v curl &> /dev/null; then
        curl -fsSL -o "$SQLITE_JAR" "$SQLITE_JAR_URL"
    elif command -v wget &> /dev/null; then
        wget -q -O "$SQLITE_JAR" "$SQLITE_JAR_URL"
    else
        echo "❌ Neither curl nor wget found. Please download manually:"
        echo "   $SQLITE_JAR_URL"
        echo "   and place it in: $SQLITE_JAR"
        exit 1
    fi
    echo "✅ SQLite JDBC driver downloaded."
fi

# ── 3. Compile ─────────────────────────────────────────────
echo "🔨 Compiling sources..."
mkdir -p "$OUT_DIR"
find "$SRC_DIR" -name "*.java" > sources.txt
javac -cp "$SQLITE_JAR" -d "$OUT_DIR" @sources.txt
rm sources.txt
echo "✅ Compilation successful."

# ── 4. Run ─────────────────────────────────────────────────
echo "🚀 Launching application..."
echo ""
java -cp "$OUT_DIR:$SQLITE_JAR" "$MAIN_CLASS"
