package com.texteditor.command;

import com.texteditor.editor.XmlEditor;
import com.texteditor.model.XmlElement;

/**
 * XML delete-element 命令，支持撤销。
 * 删除元素及其所有子元素。
 */
public class XmlDeleteElementCommand implements Command {
    private final XmlEditor editor;
    private final String elementId;
    
    // 用于撤销的状态
    private XmlElement deletedElement;
    private String parentId;
    private int indexInParent;

    public XmlDeleteElementCommand(XmlEditor editor, String elementId) {
        this.editor = editor;
        this.elementId = elementId;
    }

    @Override
    public void execute() {
        // 保存删除前的状态用于撤销
        XmlElement element = editor.getElementById(elementId);
        if (element != null) {
            deletedElement = element.deepCopy();
            XmlElement parent = element.getParent();
            if (parent != null) {
                parentId = parent.getId();
                indexInParent = parent.getChildIndex(element);
            }
        }
        editor.deleteElement(elementId);
    }

    @Override
    public void undo() {
        if (deletedElement != null && parentId != null) {
            // 恢复删除的元素
            XmlElement parent = editor.getElementById(parentId);
            if (parent != null) {
                // 在原来的位置重新插入
                restoreElement(deletedElement, parent, indexInParent);
            }
        }
    }

    /**
     * 递归恢复元素及其子元素。
     */
    private void restoreElement(XmlElement element, XmlElement parent, int index) {
        XmlElement newElement = new XmlElement(element.getTagName(), element.getId());
        newElement.setTextContent(element.getTextContent());
        for (var entry : element.getAttributes().entrySet()) {
            newElement.setAttribute(entry.getKey(), entry.getValue());
        }
        
        parent.insertChild(index, newElement);
        
        // 递归添加子元素
        int childIndex = 0;
        for (XmlElement child : element.getChildren()) {
            restoreChildElement(child, newElement);
            childIndex++;
        }
        
        // 重建 id 映射 - 通过重新设置根元素触发
        editor.setRoot(editor.getRoot());
        editor.setModified(true);
    }

    /**
     * 递归恢复子元素。
     */
    private void restoreChildElement(XmlElement element, XmlElement parent) {
        XmlElement newElement = new XmlElement(element.getTagName(), element.getId());
        newElement.setTextContent(element.getTextContent());
        for (var entry : element.getAttributes().entrySet()) {
            newElement.setAttribute(entry.getKey(), entry.getValue());
        }
        
        parent.addChild(newElement);
        
        for (XmlElement child : element.getChildren()) {
            restoreChildElement(child, newElement);
        }
    }

    @Override
    public String getDescription() {
        return "delete-element " + elementId;
    }

    @Override
    public boolean isModifying() {
        return true;
    }
}
