package com.texteditor.editor;

import com.texteditor.model.Position;
import com.texteditor.command.Command;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Text editor implementation that stores content as a list of lines.
 * Supports undo/redo using the Command pattern.
 */
public class TextEditor implements Editor {
    private final String filePath;
    private List<String> lines;
    private boolean modified;
    private final Stack<Command> undoStack;
    private final Stack<Command> redoStack;

    public TextEditor(String filePath) {
        this.filePath = filePath;
        this.lines = new ArrayList<>();
        this.modified = false;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    @Override
    public String getFilePath() {
        return filePath;
    }

    @Override
    public boolean isModified() {
        return modified;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    @Override
    public void setLines(List<String> lines) {
        this.lines = new ArrayList<>(lines);
    }

    @Override
    public String getContent() {
        return String.join("\n", lines);
    }

    @Override
    public void append(String text) {
        lines.add(text);
        modified = true;
    }

    @Override
    public void insert(Position position, String text) {
        int line = position.getLine() - 1; // Convert to 0-indexed
        int col = position.getColumn() - 1;

        // Validate position
        if (lines.isEmpty()) {
            if (line != 0 || col != 0) {
                throw new IllegalArgumentException("空文件只能在1:1位置插入");
            }
            lines.add(text);
        } else {
            if (line < 0 || line >= lines.size()) {
                throw new IllegalArgumentException("行号或列号越界");
            }

            String currentLine = lines.get(line);
            if (col < 0 || col > currentLine.length()) {
                throw new IllegalArgumentException("行号或列号越界");
            }

            // Handle multiline text
            if (text.contains("\n")) {
                String[] parts = text.split("\n", -1);
                String before = currentLine.substring(0, col);
                String after = currentLine.substring(col);

                // Replace current line with first part
                lines.set(line, before + parts[0]);

                // Insert middle lines
                for (int i = 1; i < parts.length - 1; i++) {
                    lines.add(line + i, parts[i]);
                }

                // Insert last part
                if (parts.length > 1) {
                    lines.add(line + parts.length - 1, parts[parts.length - 1] + after);
                }
            } else {
                String newLine = currentLine.substring(0, col) + text + currentLine.substring(col);
                lines.set(line, newLine);
            }
        }
        modified = true;
    }

    @Override
    public void delete(Position position, int length) {
        int line = position.getLine() - 1;
        int col = position.getColumn() - 1;

        if (line < 0 || line >= lines.size()) {
            throw new IllegalArgumentException("行号或列号越界");
        }

        String currentLine = lines.get(line);
        if (col < 0 || col >= currentLine.length()) {
            throw new IllegalArgumentException("行号或列号越界");
        }

        if (col + length > currentLine.length()) {
            throw new IllegalArgumentException("删除长度超出行尾");
        }

        String newLine = currentLine.substring(0, col) + currentLine.substring(col + length);
        lines.set(line, newLine);
        modified = true;
    }

    @Override
    public void replace(Position position, int length, String text) {
        delete(position, length);
        insert(position, text);
    }

    @Override
    public String show(int startLine, int endLine) {
        if (startLine == -1 && endLine == -1) {
            // Show all lines
            startLine = 1;
            endLine = lines.size();
        }

        if (lines.isEmpty()) {
            return "";
        }

        // Convert to 0-indexed
        int start = startLine - 1;
        int end = endLine - 1;

        if (start < 0 || end >= lines.size() || start > end) {
            throw new IllegalArgumentException("行号范围无效");
        }

        StringBuilder result = new StringBuilder();
        for (int i = start; i <= end; i++) {
            result.append(i + 1).append(": ").append(lines.get(i));
            if (i < end) {
                result.append("\n");
            }
        }
        return result.toString();
    }

    @Override
    public void executeCommand(Command command) {
        command.execute();
        if (command.isModifying()) {
            undoStack.push(command);
            redoStack.clear();
        }
    }

    @Override
    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        return true;
    }

    @Override
    public boolean redo() {
        if (redoStack.isEmpty()) {
            return false;
        }
        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        return true;
    }

    @Override
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    @Override
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
