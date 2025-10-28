package com.texteditor.command;

/**
 * Command interface for implementing the Command pattern.
 * Supports undo/redo functionality.
 */
public interface Command {
    /**
     * Execute the command.
     */
    void execute();

    /**
     * Undo the command.
     */
    void undo();

    /**
     * Get a description of the command for logging.
     */
    String getDescription();

    /**
     * Check if this command modifies the editor state.
     * Non-modifying commands (like show) don't need to be in undo history.
     */
    boolean isModifying();
}
