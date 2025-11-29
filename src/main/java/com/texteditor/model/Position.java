package com.texteditor.model;

/**
 * Represents a position in text with line and column numbers (1-indexed).
 */
public class Position {
    private final int line;
    private final int column;

    public Position(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return line + ":" + column;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return line == position.line && column == position.column;
    }

    @Override
    public int hashCode() {
        return 31 * line + column;
    }
}
