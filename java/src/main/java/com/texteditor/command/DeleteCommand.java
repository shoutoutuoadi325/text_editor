package com.texteditor.command;

import com.texteditor.editor.Editor;
import com.texteditor.model.Position;
import java.util.List;

/**
 * Command to delete characters from a specific position.
 */
public class DeleteCommand implements Command {
    private final Editor editor;
    private final Position position;
    private final int length;
    private List<String> linesBefore;

    public DeleteCommand(Editor editor, Position position, int length) {
        this.editor = editor;
        this.position = position;
        this.length = length;
    }

    @Override
    public void execute() {
        linesBefore = editor.getLines();
        editor.delete(position, length);
    }

    @Override
    public void undo() {
        editor.setLines(linesBefore);
    }

    @Override
    public String getDescription() {
        return "delete " + position + " " + length;
    }

    @Override
    public boolean isModifying() {
        return true;
    }
}
