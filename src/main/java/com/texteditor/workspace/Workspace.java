package com.texteditor.workspace;

import com.texteditor.editor.Editor;
import com.texteditor.editor.TextEditor;
import com.texteditor.logging.LogManager;
import com.texteditor.model.EditorState;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Workspace manages all open editors and their state.
 * Implements Memento pattern for state persistence.
 */
public class Workspace {
    private static final String WORKSPACE_FILE = ".editorWorkspace";
    private static final String LOG_MARKER = "# log";

    private final Map<String, Editor> editors;
    private final List<String> recentFiles; // Track file access order
    private String activeFilePath;
    private final LogManager logManager;

    public Workspace(LogManager logManager) {
        this.editors = new HashMap<>();
        this.recentFiles = new ArrayList<>();
        this.activeFilePath = null;
        this.logManager = logManager;
    }

    /**
     * Load a file into the workspace.
     */
    public Editor loadFile(String filePath) throws IOException {
        // If already open, just switch to it
        if (editors.containsKey(filePath)) {
            switchToFile(filePath);
            return editors.get(filePath);
        }

        Editor editor = new TextEditor(filePath);
        Path path = Paths.get(filePath);

        // Read file if it exists
        if (Files.exists(path)) {
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            editor.setLines(lines);
            editor.setModified(false);

            // Check for log marker
            if (!lines.isEmpty() && lines.get(0).trim().equals(LOG_MARKER)) {
                logManager.enableLogging(filePath);
                logManager.logSessionStart(filePath);
            }
        } else {
            // New file - mark as modified
            editor.setModified(true);
        }

        editors.put(filePath, editor);
        switchToFile(filePath);
        return editor;
    }

    /**
     * Initialize a new buffer with optional logging.
     */
    public Editor initFile(String filePath, boolean withLog) throws IOException {
        if (editors.containsKey(filePath) || Files.exists(Paths.get(filePath))) {
            throw new IOException("文件已存在: " + filePath);
        }

        Editor editor = new TextEditor(filePath);
        if (withLog) {
            editor.append(LOG_MARKER);
            logManager.enableLogging(filePath);
            logManager.logSessionStart(filePath);
        }
        editor.setModified(true);

        editors.put(filePath, editor);
        switchToFile(filePath);
        return editor;
    }

    /**
     * Save a file.
     */
    public void saveFile(String filePath) throws IOException {
        Editor editor = editors.get(filePath);
        if (editor == null) {
            throw new IOException("文件未打开: " + filePath);
        }

        Path path = Paths.get(filePath);
        // Create parent directories if needed
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }

        String content = editor.getContent();
        Files.write(path, content.getBytes(StandardCharsets.UTF_8));
        editor.setModified(false);

        // Log the save command
        if (logManager.isLoggingEnabled(filePath)) {
            logManager.onCommandExecuted(filePath, "save");
        }
    }

    /**
     * Save all open files.
     */
    public void saveAll() throws IOException {
        for (String filePath : editors.keySet()) {
            if (editors.get(filePath).isModified()) {
                saveFile(filePath);
            }
        }
    }

    /**
     * Close a file with optional save prompt.
     */
    public boolean closeFile(String filePath, boolean forceSave) throws IOException {
        Editor editor = editors.get(filePath);
        if (editor == null) {
            return false;
        }

        if (editor.isModified() && forceSave) {
            saveFile(filePath);
        }

        // Log the close command
        if (logManager.isLoggingEnabled(filePath)) {
            logManager.onCommandExecuted(filePath, "close");
        }

        editors.remove(filePath);
        recentFiles.remove(filePath);

        // Switch to most recent file if available
        if (filePath.equals(activeFilePath)) {
            if (!recentFiles.isEmpty()) {
                activeFilePath = recentFiles.get(recentFiles.size() - 1);
            } else {
                activeFilePath = null;
            }
        }

        return true;
    }

    /**
     * Switch the active file to the specified file.
     */
    public void switchToFile(String filePath) {
        if (!editors.containsKey(filePath)) {
            throw new IllegalArgumentException("文件未打开: " + filePath);
        }

        activeFilePath = filePath;
        recentFiles.remove(filePath);
        recentFiles.add(filePath);
    }

    /**
     * Get the active editor.
     */
    public Editor getActiveEditor() {
        if (activeFilePath == null) {
            return null;
        }
        return editors.get(activeFilePath);
    }

    /**
     * Get editor by file path.
     */
    public Editor getEditor(String filePath) {
        return editors.get(filePath);
    }

    /**
     * Get the active file path.
     */
    public String getActiveFilePath() {
        return activeFilePath;
    }

    /**
     * Get all open file paths.
     */
    public List<String> getOpenFiles() {
        return new ArrayList<>(editors.keySet());
    }

    /**
     * Check if any file has unsaved changes.
     */
    public boolean hasUnsavedChanges() {
        return editors.values().stream().anyMatch(Editor::isModified);
    }

    /**
     * Get list of modified files.
     */
    public List<String> getModifiedFiles() {
        List<String> modified = new ArrayList<>();
        for (Map.Entry<String, Editor> entry : editors.entrySet()) {
            if (entry.getValue().isModified()) {
                modified.add(entry.getKey());
            }
        }
        return modified;
    }

    /**
     * Create a memento of the current workspace state.
     */
    public WorkspaceMemento saveToMemento() {
        List<EditorState> states = new ArrayList<>();
        for (Map.Entry<String, Editor> entry : editors.entrySet()) {
            String filePath = entry.getKey();
            Editor editor = entry.getValue();
            boolean loggingEnabled = logManager.isLoggingEnabled(filePath);
            states.add(new EditorState(filePath, editor.isModified(), loggingEnabled));
        }
        return new WorkspaceMemento(states, activeFilePath);
    }

    /**
     * Restore workspace state from a memento.
     */
    public void restoreFromMemento(WorkspaceMemento memento) throws IOException {
        for (EditorState state : memento.getEditorStates()) {
            loadFile(state.getFilePath());
            if (state.isLoggingEnabled()) {
                logManager.enableLogging(state.getFilePath());
            }
        }

        if (memento.getActiveFilePath() != null && editors.containsKey(memento.getActiveFilePath())) {
            switchToFile(memento.getActiveFilePath());
        }
    }

    /**
     * Save workspace state to file.
     */
    public void saveWorkspaceState() {
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(WORKSPACE_FILE, StandardCharsets.UTF_8))) {
            
            WorkspaceMemento memento = saveToMemento();
            
            // Write active file
            if (memento.getActiveFilePath() != null) {
                writer.write("active=" + memento.getActiveFilePath());
                writer.newLine();
            }

            // Write editor states
            for (EditorState state : memento.getEditorStates()) {
                writer.write("file=" + state.getFilePath());
                writer.write(",modified=" + state.isModified());
                writer.write(",logging=" + state.isLoggingEnabled());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("警告: 保存工作区状态失败: " + e.getMessage());
        }
    }

    /**
     * Load workspace state from file.
     */
    public void loadWorkspaceState() {
        try {
            Path workspacePath = Paths.get(WORKSPACE_FILE);
            if (!Files.exists(workspacePath)) {
                return;
            }

            List<String> lines = Files.readAllLines(workspacePath, StandardCharsets.UTF_8);
            String activeFile = null;
            List<EditorState> states = new ArrayList<>();

            for (String line : lines) {
                if (line.startsWith("active=")) {
                    activeFile = line.substring(7);
                } else if (line.startsWith("file=")) {
                    String[] parts = line.split(",");
                    String filePath = parts[0].substring(5);
                    boolean modified = parts[1].substring(9).equals("true");
                    boolean logging = parts[2].substring(8).equals("true");
                    states.add(new EditorState(filePath, modified, logging));
                }
            }

            WorkspaceMemento memento = new WorkspaceMemento(states, activeFile);
            restoreFromMemento(memento);
        } catch (IOException e) {
            System.err.println("警告: 加载工作区状态失败: " + e.getMessage());
        }
    }

    /**
     * Get the log manager.
     */
    public LogManager getLogManager() {
        return logManager;
    }
}
