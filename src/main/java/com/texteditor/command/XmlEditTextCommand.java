package com.texteditor.command;

import com.texteditor.editor.XmlEditor;

/**
 * XML edit-text 命令，支持撤销。
 * 修改元素的文本内容。
 */
public class XmlEditTextCommand implements Command {
    private final XmlEditor editor;
    private final String elementId;
    private final String newText;
    
    // 用于撤销的状态
    private String oldText;

    public XmlEditTextCommand(XmlEditor editor, String elementId, String newText) {
        this.editor = editor;
        this.elementId = elementId;
        this.newText = newText;
    }

    @Override
    public void execute() {
        // 保存旧文本用于撤销
        var element = editor.getElementById(elementId);
        if (element != null) {
            oldText = element.getTextContent();
        }
        editor.editText(elementId, newText);
    }

    @Override
    public void undo() {
        // 撤销：恢复旧文本
        editor.editText(elementId, oldText);
    }

    @Override
    public String getDescription() {
        return "edit-text " + elementId + " \"" + newText + "\"";
    }

    @Override
    public boolean isModifying() {
        return true;
    }
}
