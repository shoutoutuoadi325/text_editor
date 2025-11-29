package com.texteditor.command;

import com.texteditor.editor.XmlEditor;

/**
 * XML edit-id 命令，支持撤销。
 * 修改元素的 ID。
 */
public class XmlEditIdCommand implements Command {
    private final XmlEditor editor;
    private final String oldId;
    private final String newId;

    public XmlEditIdCommand(XmlEditor editor, String oldId, String newId) {
        this.editor = editor;
        this.oldId = oldId;
        this.newId = newId;
    }

    @Override
    public void execute() {
        editor.editId(oldId, newId);
    }

    @Override
    public void undo() {
        // 撤销：将 newId 改回 oldId
        editor.editId(newId, oldId);
    }

    @Override
    public String getDescription() {
        return "edit-id " + oldId + " " + newId;
    }

    @Override
    public boolean isModifying() {
        return true;
    }
}
