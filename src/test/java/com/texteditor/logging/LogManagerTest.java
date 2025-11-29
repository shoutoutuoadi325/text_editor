package com.texteditor.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LogManager.
 */
class LogManagerTest {
    private LogManager logManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        logManager = new LogManager();
    }

    @Test
    void testEnableDisableLogging() {
        String filePath = "test.txt";
        
        assertFalse(logManager.isLoggingEnabled(filePath));
        
        logManager.enableLogging(filePath);
        assertTrue(logManager.isLoggingEnabled(filePath));
        
        logManager.disableLogging(filePath);
        assertFalse(logManager.isLoggingEnabled(filePath));
    }

    @Test
    void testOnCommandExecuted() throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "content");
        String filePath = testFile.toString();
        
        logManager.enableLogging(filePath);
        logManager.logSessionStart(filePath);
        logManager.onCommandExecuted(filePath, "append \"test\"");
        
        String log = logManager.showLog(filePath);
        assertTrue(log.contains("session start at"));
        assertTrue(log.contains("append \"test\""));
    }

    @Test
    void testShowLogNonexistent() {
        String result = logManager.showLog("nonexistent.txt");
        assertTrue(result.contains("日志文件不存在") || result.contains("读取日志失败"));
    }

    @Test
    void testLoggingDisabled() throws IOException {
        Path testFile = tempDir.resolve("test2.txt");
        Files.writeString(testFile, "content");
        String filePath = testFile.toString();
        
        // Don't enable logging
        logManager.onCommandExecuted(filePath, "append \"test\"");
        
        // Log file should not be created or should be empty
        String log = logManager.showLog(filePath);
        assertFalse(log.contains("append \"test\""));
    }
}
