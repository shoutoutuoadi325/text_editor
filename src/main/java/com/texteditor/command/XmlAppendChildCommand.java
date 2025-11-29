package com.texteditor.command;

import com.texteditor.editor.XmlEditor;
import com.texteditor.model.XmlElement;

/**
 * XML append-child 命令，支持撤销。
 * 在父元素内追加子元素。
 */
public class XmlAppendChildCommand implements Command {
    private final XmlEditor editor;
    private final String tagName;
    private final String newId;
    private final String parentId;
    private final String text;
    
    // 用于撤销的状态
    private XmlElement appendedElement;

    public XmlAppendChildCommand(XmlEditor editor, String tagName, String newId, 
                                  String parentId, String text) {
        this.editor = editor;
        this.tagName = tagName;
        this.newId = newId;
        this.parentId = parentId;
        this.text = text;
    }

    @Override
    public void execute() {
        editor.appendChild(tagName, newId, parentId, text);
        appendedElement = editor.getElementById(newId);
    }

    @Override
    public void undo() {
        if (appendedElement != null) {
            // 删除追加的元素
            editor.deleteElement(newId);
        }
    }

    @Override
    public String getDescription() {
        return "append-child " + tagName + " " + newId + " " + parentId + 
               (text != null && !text.isEmpty() ? " \"" + text + "\"" : "");
    }

    @Override
    public boolean isModifying() {
        return true;
    }
}
