package com.texteditor.spellcheck;

import java.util.List;

/**
 * 拼写错误信息。
 */
public class SpellingError {
    private final int line;              // 行号（1-indexed），对于 XML 为 -1
    private final int column;            // 列号（1-indexed），对于 XML 为 -1
    private final String elementId;      // XML 元素 ID（仅 XML 文件使用）
    private final String word;           // 错误单词
    private final List<String> suggestions;  // 建议的正确拼写

    /**
     * 创建文本文件的拼写错误。
     */
    public SpellingError(int line, int column, String word, List<String> suggestions) {
        this.line = line;
        this.column = column;
        this.elementId = null;
        this.word = word;
        this.suggestions = suggestions;
    }

    /**
     * 创建 XML 文件的拼写错误。
     */
    public SpellingError(String elementId, String word, List<String> suggestions) {
        this.line = -1;
        this.column = -1;
        this.elementId = elementId;
        this.word = word;
        this.suggestions = suggestions;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getElementId() {
        return elementId;
    }

    public String getWord() {
        return word;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    /**
     * 检查是否为文本文件的错误。
     */
    public boolean isTextFileError() {
        return elementId == null;
    }

    /**
     * 格式化输出拼写错误。
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        if (isTextFileError()) {
            // 文本文件格式：第 X 行，第 Y 列: "word" -> 建议: suggestion1, suggestion2
            sb.append("第 ").append(line).append(" 行，第 ").append(column).append(" 列: ");
        } else {
            // XML 文件格式：元素 elementId: "word" -> 建议: suggestion1, suggestion2
            sb.append("元素 ").append(elementId).append(": ");
        }
        sb.append("\"").append(word).append("\" -> 建议: ");
        if (suggestions.isEmpty()) {
            sb.append("(无建议)");
        } else {
            sb.append(String.join(", ", suggestions));
        }
        return sb.toString();
    }
}
