@echo off
REM ============================================================
REM  Waste-to-Wealth Community Exchange — Build & Run (Windows)
REM ============================================================

set SQLITE_JAR=libs\sqlite-jdbc-3.45.1.0.jar
set SQLITE_URL=https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.45.1.0/sqlite-jdbc-3.45.1.0.jar
set SRC_DIR=src\main\java
set OUT_DIR=out
set MAIN_CLASS=com.w2w.Main

echo.
echo   ♻  Waste-to-Wealth Community Exchange
echo =========================================

REM Check javac
where javac >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ javac not found. Install JDK 17+ and add it to PATH.
    pause & exit /b 1
)

REM Download SQLite JAR if missing
if not exist libs mkdir libs
if not exist %SQLITE_JAR% (
    echo 📥 Downloading SQLite JDBC driver...
    powershell -Command "Invoke-WebRequest -Uri '%SQLITE_URL%' -OutFile '%SQLITE_JAR%'"
    echo ✅ SQLite JDBC driver downloaded.
)

REM Collect sources
echo 🔨 Compiling sources...
if not exist %OUT_DIR% mkdir %OUT_DIR%
dir /s /b %SRC_DIR%\*.java > sources.txt
javac -cp %SQLITE_JAR% -d %OUT_DIR% @sources.txt
del sources.txt
echo ✅ Compilation successful.

REM Run
echo 🚀 Launching application...
echo.
java -cp "%OUT_DIR%;%SQLITE_JAR%" %MAIN_CLASS%
pause
