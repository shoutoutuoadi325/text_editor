package com.texteditor.command;

import com.texteditor.editor.Editor;
import com.texteditor.model.Position;
import java.util.List;

/**
 * Command to replace characters at a specific position with new text.
 */
public class ReplaceCommand implements Command {
    private final Editor editor;
    private final Position position;
    private final int length;
    private final String text;
    private List<String> linesBefore;

    public ReplaceCommand(Editor editor, Position position, int length, String text) {
        this.editor = editor;
        this.position = position;
        this.length = length;
        this.text = text;
    }

    @Override
    public void execute() {
        linesBefore = editor.getLines();
        editor.replace(position, length, text);
    }

    @Override
    public void undo() {
        editor.setLines(linesBefore);
    }

    @Override
    public String getDescription() {
        return "replace " + position + " " + length + " \"" + text + "\"";
    }

    @Override
    public boolean isModifying() {
        return true;
    }
}
