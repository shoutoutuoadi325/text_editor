package com.texteditor.logging;

/**
 * Observer interface for the Observer pattern.
 * Used for logging command execution events.
 */
public interface CommandObserver {
    /**
     * Called when a command is about to be executed.
     */
    void onCommandExecuted(String filePath, String commandDescription);
}
