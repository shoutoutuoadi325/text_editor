package com.texteditor.logging;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Log manager that implements the Observer pattern.
 * Records command execution to log files.
 */
public class LogManager implements CommandObserver {
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
    
    private final Map<String, Boolean> loggingEnabled;
    private boolean sessionStartLogged;

    public LogManager() {
        this.loggingEnabled = new HashMap<>();
        this.sessionStartLogged = false;
    }

    /**
     * Enable logging for a specific file.
     */
    public void enableLogging(String filePath) {
        loggingEnabled.put(filePath, true);
    }

    /**
     * Disable logging for a specific file.
     */
    public void disableLogging(String filePath) {
        loggingEnabled.put(filePath, false);
    }

    /**
     * Check if logging is enabled for a file.
     */
    public boolean isLoggingEnabled(String filePath) {
        return loggingEnabled.getOrDefault(filePath, false);
    }

    /**
     * Get the log file path for a given source file.
     */
    private String getLogFilePath(String filePath) {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        String directory = path.getParent() != null ? path.getParent().toString() : ".";
        return directory + File.separator + "." + fileName + ".log";
    }

    /**
     * Log session start (only once per session).
     */
    public void logSessionStart(String filePath) {
        if (!sessionStartLogged && isLoggingEnabled(filePath)) {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String logEntry = "session start at " + timestamp + "\n";
            writeLog(filePath, logEntry);
            sessionStartLogged = true;
        }
    }

    @Override
    public void onCommandExecuted(String filePath, String commandDescription) {
        if (!isLoggingEnabled(filePath)) {
            return;
        }

        // Log session start if not yet done
        if (!sessionStartLogged) {
            logSessionStart(filePath);
        }

        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String logEntry = timestamp + " " + commandDescription + "\n";
        writeLog(filePath, logEntry);
    }

    /**
     * Write a log entry to the log file.
     */
    private void writeLog(String filePath, String logEntry) {
        try {
            String logFilePath = getLogFilePath(filePath);
            Path logPath = Paths.get(logFilePath);
            
            // Create parent directories if needed
            if (logPath.getParent() != null) {
                Files.createDirectories(logPath.getParent());
            }

            // Append to log file
            Files.write(logPath, logEntry.getBytes(StandardCharsets.UTF_8), 
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("警告: 日志写入失败: " + e.getMessage());
        }
    }

    /**
     * Read and return the log file content for a given file.
     */
    public String showLog(String filePath) {
        try {
            String logFilePath = getLogFilePath(filePath);
            Path logPath = Paths.get(logFilePath);
            
            if (!Files.exists(logPath)) {
                return "日志文件不存在";
            }

            return new String(Files.readAllBytes(logPath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "读取日志失败: " + e.getMessage();
        }
    }
}
