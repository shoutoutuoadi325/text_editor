package com.texteditor.editor;

import com.texteditor.model.Position;
import com.texteditor.command.Command;
import java.util.List;

/**
 * Interface for text editors that support basic editing operations.
 */
public interface Editor {
    /**
     * Get the file path associated with this editor.
     */
    String getFilePath();

    /**
     * Check if the editor has unsaved modifications.
     */
    boolean isModified();

    /**
     * Mark the editor as modified or unmodified.
     */
    void setModified(boolean modified);

    /**
     * Get all lines of text.
     */
    List<String> getLines();

    /**
     * Set all lines of text.
     */
    void setLines(List<String> lines);

    /**
     * Get the content as a single string with newlines.
     */
    String getContent();

    /**
     * Append a line of text at the end.
     */
    void append(String text);

    /**
     * Insert text at the specified position.
     */
    void insert(Position position, String text);

    /**
     * Delete characters starting from the specified position.
     */
    void delete(Position position, int length);

    /**
     * Replace characters with new text.
     */
    void replace(Position position, int length, String text);

    /**
     * Show lines from start to end (inclusive, 1-indexed).
     * If both are -1, show all lines.
     */
    String show(int startLine, int endLine);

    /**
     * Execute a command and add to history.
     */
    void executeCommand(Command command);

    /**
     * Undo the last command.
     */
    boolean undo();

    /**
     * Redo the last undone command.
     */
    boolean redo();

    /**
     * Check if undo is available.
     */
    boolean canUndo();

    /**
     * Check if redo is available.
     */
    boolean canRedo();
}
