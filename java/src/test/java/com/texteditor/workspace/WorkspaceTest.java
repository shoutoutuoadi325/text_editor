package com.texteditor.workspace;

import com.texteditor.editor.Editor;
import com.texteditor.logging.LogManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Workspace.
 */
class WorkspaceTest {
    private Workspace workspace;
    private LogManager logManager;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        logManager = new LogManager();
        workspace = new Workspace(logManager);
    }

    @Test
    void testLoadExistingFile() throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "Line 1\nLine 2");

        Editor editor = workspace.loadFile(testFile.toString());
        
        assertNotNull(editor);
        assertEquals(2, editor.getLines().size());
        assertEquals(testFile.toString(), workspace.getActiveFilePath());
    }

    @Test
    void testLoadNonexistentFile() throws IOException {
        Path testFile = tempDir.resolve("new.txt");

        Editor editor = workspace.loadFile(testFile.toString());
        
        assertNotNull(editor);
        assertTrue(editor.isModified());
        assertTrue(editor.getLines().isEmpty());
    }

    @Test
    void testInitFile() throws IOException {
        Path testFile = tempDir.resolve("init.txt");

        Editor editor = workspace.initFile(testFile.toString(), false);
        
        assertNotNull(editor);
        assertTrue(editor.isModified());
        assertEquals(testFile.toString(), workspace.getActiveFilePath());
    }

    @Test
    void testInitFileWithLog() throws IOException {
        Path testFile = tempDir.resolve("init_log.txt");

        Editor editor = workspace.initFile(testFile.toString(), true);
        
        assertEquals(1, editor.getLines().size());
        assertEquals("# log", editor.getLines().get(0));
        assertTrue(logManager.isLoggingEnabled(testFile.toString()));
    }

    @Test
    void testSaveFile() throws IOException {
        Path testFile = tempDir.resolve("save.txt");

        Editor editor = workspace.loadFile(testFile.toString());
        editor.append("Test content");
        
        workspace.saveFile(testFile.toString());
        
        assertFalse(editor.isModified());
        assertTrue(Files.exists(testFile));
        assertEquals("Test content", Files.readString(testFile));
    }

    @Test
    void testSaveAll() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");

        workspace.loadFile(file1.toString()).append("Content 1");
        workspace.loadFile(file2.toString()).append("Content 2");

        workspace.saveAll();

        assertTrue(Files.exists(file1));
        assertTrue(Files.exists(file2));
    }

    @Test
    void testCloseFile() throws IOException {
        Path testFile = tempDir.resolve("close.txt");
        workspace.loadFile(testFile.toString());

        boolean closed = workspace.closeFile(testFile.toString(), false);
        
        assertTrue(closed);
        assertNull(workspace.getEditor(testFile.toString()));
    }

    @Test
    void testSwitchToFile() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");

        workspace.loadFile(file1.toString());
        workspace.loadFile(file2.toString());

        assertEquals(file2.toString(), workspace.getActiveFilePath());

        workspace.switchToFile(file1.toString());
        assertEquals(file1.toString(), workspace.getActiveFilePath());
    }

    @Test
    void testGetOpenFiles() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");

        workspace.loadFile(file1.toString());
        workspace.loadFile(file2.toString());

        assertEquals(2, workspace.getOpenFiles().size());
        assertTrue(workspace.getOpenFiles().contains(file1.toString()));
        assertTrue(workspace.getOpenFiles().contains(file2.toString()));
    }

    @Test
    void testHasUnsavedChanges() throws IOException {
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "existing content");
        
        Editor editor = workspace.loadFile(testFile.toString());

        assertFalse(workspace.hasUnsavedChanges());

        editor.append("New content");
        assertTrue(workspace.hasUnsavedChanges());
    }

    @Test
    void testMemento() throws IOException {
        Path file1 = tempDir.resolve("file1.txt");
        workspace.loadFile(file1.toString());

        WorkspaceMemento memento = workspace.saveToMemento();
        
        assertEquals(1, memento.getEditorStates().size());
        assertEquals(file1.toString(), memento.getActiveFilePath());
    }
}
