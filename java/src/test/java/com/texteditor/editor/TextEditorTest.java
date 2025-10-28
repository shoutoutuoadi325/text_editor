package com.texteditor.editor;

import com.texteditor.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

/**
 * Unit tests for TextEditor.
 */
class TextEditorTest {
    private TextEditor editor;

    @BeforeEach
    void setUp() {
        editor = new TextEditor("test.txt");
    }

    @Test
    void testInitialState() {
        assertEquals("test.txt", editor.getFilePath());
        assertFalse(editor.isModified());
        assertTrue(editor.getLines().isEmpty());
    }

    @Test
    void testAppend() {
        editor.append("Hello");
        editor.append("World");

        assertEquals(2, editor.getLines().size());
        assertEquals("Hello", editor.getLines().get(0));
        assertEquals("World", editor.getLines().get(1));
        assertTrue(editor.isModified());
    }

    @Test
    void testInsertSingleLine() {
        editor.append("abcdef");
        editor.insert(new Position(1, 4), "XYZ");

        assertEquals("abcXYZdef", editor.getLines().get(0));
    }

    @Test
    void testInsertMultiline() {
        editor.append("Hello World");
        editor.insert(new Position(1, 7), "Beautiful\nNew\n");

        assertEquals(3, editor.getLines().size());
        assertEquals("Hello Beautiful", editor.getLines().get(0));
        assertEquals("New", editor.getLines().get(1));
        assertEquals("World", editor.getLines().get(2));
    }

    @Test
    void testInsertInEmptyFile() {
        editor.insert(new Position(1, 1), "First line");
        assertEquals(1, editor.getLines().size());
        assertEquals("First line", editor.getLines().get(0));
    }

    @Test
    void testInsertInvalidPositionEmptyFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            editor.insert(new Position(2, 1), "text");
        });
    }

    @Test
    void testDelete() {
        editor.append("Hello world");
        editor.delete(new Position(1, 7), 5);

        assertEquals("Hello ", editor.getLines().get(0));
    }

    @Test
    void testDeleteInvalidLength() {
        editor.append("Hello");
        assertThrows(IllegalArgumentException.class, () -> {
            editor.delete(new Position(1, 3), 10);
        });
    }

    @Test
    void testReplace() {
        editor.append("fast fox");
        editor.replace(new Position(1, 1), 4, "slow");

        assertEquals("slow fox", editor.getLines().get(0));
    }

    @Test
    void testShow() {
        editor.append("Line 1");
        editor.append("Line 2");
        editor.append("Line 3");

        String result = editor.show(-1, -1);
        assertTrue(result.contains("1: Line 1"));
        assertTrue(result.contains("2: Line 2"));
        assertTrue(result.contains("3: Line 3"));
    }

    @Test
    void testShowRange() {
        editor.append("Line 1");
        editor.append("Line 2");
        editor.append("Line 3");

        String result = editor.show(1, 2);
        assertTrue(result.contains("1: Line 1"));
        assertTrue(result.contains("2: Line 2"));
        assertFalse(result.contains("3: Line 3"));
    }

    @Test
    void testGetContent() {
        editor.append("Line 1");
        editor.append("Line 2");

        assertEquals("Line 1\nLine 2", editor.getContent());
    }

    @Test
    void testSetLines() {
        editor.setLines(Arrays.asList("A", "B", "C"));
        assertEquals(3, editor.getLines().size());
        assertEquals("A", editor.getLines().get(0));
    }
}
