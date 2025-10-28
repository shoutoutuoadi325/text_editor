package com.texteditor.command;

import com.texteditor.editor.Editor;
import com.texteditor.model.Position;
import java.util.List;

/**
 * Command to insert text at a specific position.
 */
public class InsertCommand implements Command {
    private final Editor editor;
    private final Position position;
    private final String text;
    private List<String> linesBefore;

    public InsertCommand(Editor editor, Position position, String text) {
        this.editor = editor;
        this.position = position;
        this.text = text;
    }

    @Override
    public void execute() {
        linesBefore = editor.getLines();
        editor.insert(position, text);
    }

    @Override
    public void undo() {
        editor.setLines(linesBefore);
    }

    @Override
    public String getDescription() {
        return "insert " + position + " \"" + text + "\"";
    }

    @Override
    public boolean isModifying() {
        return true;
    }
}
