package com.texteditor.command;

import com.texteditor.editor.TextEditor;
import com.texteditor.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Command implementations.
 */
class CommandTest {
    private TextEditor editor;

    @BeforeEach
    void setUp() {
        editor = new TextEditor("test.txt");
    }

    @Test
    void testAppendCommand() {
        Command cmd = new AppendCommand(editor, "Hello");
        cmd.execute();

        assertEquals(1, editor.getLines().size());
        assertEquals("Hello", editor.getLines().get(0));

        cmd.undo();
        assertEquals(0, editor.getLines().size());
    }

    @Test
    void testInsertCommand() {
        editor.append("abcdef");
        Command cmd = new InsertCommand(editor, new Position(1, 4), "XYZ");
        cmd.execute();

        assertEquals("abcXYZdef", editor.getLines().get(0));

        cmd.undo();
        assertEquals("abcdef", editor.getLines().get(0));
    }

    @Test
    void testDeleteCommand() {
        editor.append("Hello world");
        Command cmd = new DeleteCommand(editor, new Position(1, 7), 5);
        cmd.execute();

        assertEquals("Hello ", editor.getLines().get(0));

        cmd.undo();
        assertEquals("Hello world", editor.getLines().get(0));
    }

    @Test
    void testReplaceCommand() {
        editor.append("fast fox");
        Command cmd = new ReplaceCommand(editor, new Position(1, 1), 4, "slow");
        cmd.execute();

        assertEquals("slow fox", editor.getLines().get(0));

        cmd.undo();
        assertEquals("fast fox", editor.getLines().get(0));
    }

    @Test
    void testUndoRedoChain() {
        editor.executeCommand(new AppendCommand(editor, "Line 1"));
        editor.executeCommand(new AppendCommand(editor, "Line 2"));

        assertEquals(2, editor.getLines().size());

        assertTrue(editor.undo());
        assertEquals(1, editor.getLines().size());

        assertTrue(editor.redo());
        assertEquals(2, editor.getLines().size());
    }

    @Test
    void testCommandIsModifying() {
        Command append = new AppendCommand(editor, "text");
        assertTrue(append.isModifying());
    }

    @Test
    void testCommandDescription() {
        Command cmd = new AppendCommand(editor, "test");
        assertEquals("append \"test\"", cmd.getDescription());
    }
}
