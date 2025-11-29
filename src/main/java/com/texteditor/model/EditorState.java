package com.texteditor.model;

/**
 * Represents the state of an editor that can be persisted.
 */
public class EditorState {
    private final String filePath;
    private final boolean modified;
    private final boolean loggingEnabled;

    public EditorState(String filePath, boolean modified, boolean loggingEnabled) {
        this.filePath = filePath;
        this.modified = modified;
        this.loggingEnabled = loggingEnabled;
    }

    public String getFilePath() {
        return filePath;
    }

    public boolean isModified() {
        return modified;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }
}
