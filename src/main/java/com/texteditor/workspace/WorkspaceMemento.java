package com.texteditor.workspace;

import com.texteditor.model.EditorState;
import java.util.List;

/**
 * Memento class for storing workspace state.
 * Used for persistence across sessions.
 */
public class WorkspaceMemento {
    private final List<EditorState> editorStates;
    private final String activeFilePath;

    public WorkspaceMemento(List<EditorState> editorStates, String activeFilePath) {
        this.editorStates = editorStates;
        this.activeFilePath = activeFilePath;
    }

    public List<EditorState> getEditorStates() {
        return editorStates;
    }

    public String getActiveFilePath() {
        return activeFilePath;
    }
}
