package com.texteditor.command;

import com.texteditor.editor.Editor;

/**
 * Command to append text to the end of the file.
 */
public class AppendCommand implements Command {
    private final Editor editor;
    private final String text;
    private int lineCountBefore;

    public AppendCommand(Editor editor, String text) {
        this.editor = editor;
        this.text = text;
    }

    @Override
    public void execute() {
        lineCountBefore = editor.getLines().size();
        editor.append(text);
    }

    @Override
    public void undo() {
        var lines = editor.getLines();
        if (lines.size() > lineCountBefore) {
            lines.remove(lines.size() - 1);
            editor.setLines(lines);
        }
    }

    @Override
    public String getDescription() {
        return "append \"" + text + "\"";
    }

    @Override
    public boolean isModifying() {
        return true;
    }
}
