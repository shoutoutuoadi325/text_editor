package com.texteditor.command;

import com.texteditor.editor.XmlEditor;
import com.texteditor.model.XmlElement;

/**
 * XML insert-before 命令，支持撤销。
 * 在目标元素前插入新元素。
 */
public class XmlInsertBeforeCommand implements Command {
    private final XmlEditor editor;
    private final String tagName;
    private final String newId;
    private final String targetId;
    private final String text;
    
    // 用于撤销的状态
    private XmlElement insertedElement;

    public XmlInsertBeforeCommand(XmlEditor editor, String tagName, String newId, 
                                   String targetId, String text) {
        this.editor = editor;
        this.tagName = tagName;
        this.newId = newId;
        this.targetId = targetId;
        this.text = text;
    }

    @Override
    public void execute() {
        editor.insertBefore(tagName, newId, targetId, text);
        insertedElement = editor.getElementById(newId);
    }

    @Override
    public void undo() {
        if (insertedElement != null) {
            // 删除插入的元素
            editor.deleteElement(newId);
        }
    }

    @Override
    public String getDescription() {
        return "insert-before " + tagName + " " + newId + " " + targetId + 
               (text != null && !text.isEmpty() ? " \"" + text + "\"" : "");
    }

    @Override
    public boolean isModifying() {
        return true;
    }
}
