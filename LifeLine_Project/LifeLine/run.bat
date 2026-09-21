@echo off

title LifeLine Application

echo ==========================================
echo           LifeLine Application
echo ==========================================
echo.

where javac >nul 2>nul

if errorlevel 1 (
    echo ERROR: JDK/Javac is not available.
    echo Please install and configure Java JDK.
    echo.
    pause
    exit /b 1
)

echo Compiling LifeLine...
echo.

javac -d . src\LifeLineServer.java

if errorlevel 1 (
    echo.
    echo ERROR: Compilation failed.
    echo Please check the Java code and try again.
    echo.
    pause
    exit /b 1
)

echo.
echo Compilation successful.
echo Starting LifeLine server...
echo.
echo Open your browser at:
echo http://localhost:8787
echo.
echo Press Ctrl+C to stop the server.
echo.

java LifeLineServer

pause
