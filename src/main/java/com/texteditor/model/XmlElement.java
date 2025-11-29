package com.texteditor.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 表示 XML 元素节点，使用组合模式构建树形结构。
 * 每个元素必须有唯一的 id 属性，用于命令操作中的元素定位。
 */
public class XmlElement {
    private String tagName;                           // 标签名
    private String id;                                // 元素的唯一 ID
    private Map<String, String> attributes;           // 其他属性（不包括 id）
    private String textContent;                       // 文本内容（叶子节点）
    private List<XmlElement> children;                // 子元素列表
    private XmlElement parent;                        // 父元素引用

    /**
     * 创建一个新的 XML 元素。
     * @param tagName 标签名
     * @param id 元素的唯一 ID
     */
    public XmlElement(String tagName, String id) {
        this.tagName = tagName;
        this.id = id;
        this.attributes = new LinkedHashMap<>();      // 保持属性插入顺序
        this.children = new ArrayList<>();
        this.textContent = null;
        this.parent = null;
    }

    // ===================== Getter 和 Setter 方法 =====================

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttribute(String name, String value) {
        attributes.put(name, value);
    }

    public String getAttribute(String name) {
        return attributes.get(name);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public List<XmlElement> getChildren() {
        return children;
    }

    public XmlElement getParent() {
        return parent;
    }

    public void setParent(XmlElement parent) {
        this.parent = parent;
    }

    // ===================== 子元素操作方法 =====================

    /**
     * 添加子元素到末尾。
     * @param child 要添加的子元素
     */
    public void addChild(XmlElement child) {
        child.setParent(this);
        children.add(child);
    }

    /**
     * 在指定位置插入子元素。
     * @param index 插入位置
     * @param child 要插入的子元素
     */
    public void insertChild(int index, XmlElement child) {
        child.setParent(this);
        children.add(index, child);
    }

    /**
     * 移除子元素。
     * @param child 要移除的子元素
     * @return 是否成功移除
     */
    public boolean removeChild(XmlElement child) {
        if (children.remove(child)) {
            child.setParent(null);
            return true;
        }
        return false;
    }

    /**
     * 获取子元素在父元素中的索引。
     * @param child 子元素
     * @return 索引位置，如果不存在返回 -1
     */
    public int getChildIndex(XmlElement child) {
        return children.indexOf(child);
    }

    // ===================== 辅助方法 =====================

    /**
     * 检查元素是否有子元素。
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * 检查元素是否有文本内容。
     */
    public boolean hasTextContent() {
        return textContent != null && !textContent.isEmpty();
    }

    /**
     * 检查元素是否为根元素（无父元素）。
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * 深度复制元素及其所有子元素。
     */
    public XmlElement deepCopy() {
        XmlElement copy = new XmlElement(this.tagName, this.id);
        copy.attributes.putAll(this.attributes);
        copy.textContent = this.textContent;
        for (XmlElement child : this.children) {
            XmlElement childCopy = child.deepCopy();
            copy.addChild(childCopy);
        }
        return copy;
    }

    @Override
    public String toString() {
        return "XmlElement{tagName='" + tagName + "', id='" + id + "'}";
    }
}
