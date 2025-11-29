package com.texteditor.editor;

import com.texteditor.command.Command;
import com.texteditor.model.Position;
import com.texteditor.model.XmlElement;
import com.texteditor.xml.XmlAdapter;
import com.texteditor.xml.XmlParseException;

import java.util.*;

/**
 * XML 编辑器实现，支持树形结构的 XML 文件编辑。
 * 使用组合模式表示 XML 树形结构，
 * 使用命令模式实现 undo/redo 功能。
 */
public class XmlEditor implements Editor {
    private final String filePath;
    private XmlElement root;                           // XML 根元素
    private Map<String, XmlElement> idMap;             // id -> element 快速查找映射
    private boolean modified;
    private boolean loggingEnabled;                    // 日志开关（通过根元素 log="true" 属性）
    private final Stack<Command> undoStack;
    private final Stack<Command> redoStack;
    private final XmlAdapter xmlAdapter;

    /**
     * 创建一个新的 XML 编辑器。
     * @param filePath 文件路径
     */
    public XmlEditor(String filePath) {
        this.filePath = filePath;
        this.idMap = new HashMap<>();
        this.modified = false;
        this.loggingEnabled = false;
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
        this.xmlAdapter = new XmlAdapter();
        this.root = null;
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

    /**
     * 获取根元素。
     */
    public XmlElement getRoot() {
        return root;
    }

    /**
     * 设置根元素并重建 id 映射。
     */
    public void setRoot(XmlElement root) {
        this.root = root;
        rebuildIdMap();
    }

    /**
     * 从 XML 内容加载。
     * @param content XML 字符串内容
     */
    public void loadFromContent(String content) throws XmlParseException {
        this.root = xmlAdapter.parse(content);
        rebuildIdMap();
        // 检查是否启用日志
        if (root != null && "true".equals(root.getAttribute("log"))) {
            this.loggingEnabled = true;
        }
    }

    /**
     * 重建 id -> element 映射。
     */
    private void rebuildIdMap() {
        idMap.clear();
        if (root != null) {
            buildIdMap(root);
        }
    }

    /**
     * 递归构建 id 映射。
     */
    private void buildIdMap(XmlElement element) {
        idMap.put(element.getId(), element);
        for (XmlElement child : element.getChildren()) {
            buildIdMap(child);
        }
    }

    /**
     * 根据 id 查找元素。
     */
    public XmlElement getElementById(String id) {
        return idMap.get(id);
    }

    /**
     * 检查 id 是否已存在。
     */
    public boolean idExists(String id) {
        return idMap.containsKey(id);
    }

    /**
     * 检查是否启用日志。
     */
    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    /**
     * 设置日志开关。
     */
    public void setLoggingEnabled(boolean enabled) {
        this.loggingEnabled = enabled;
        if (root != null) {
            if (enabled) {
                root.setAttribute("log", "true");
            } else {
                root.removeAttribute("log");
            }
            modified = true;
        }
    }

    // ===================== XML 编辑操作 =====================

    /**
     * 在目标元素前（同级）插入一个新元素。
     * @param tagName 新元素的标签名
     * @param newId 新元素的 ID
     * @param targetId 目标元素的 ID
     * @param text 可选的文本内容
     * @throws IllegalArgumentException 操作无效时抛出
     */
    public void insertBefore(String tagName, String newId, String targetId, String text) {
        // 验证 newId 不存在
        if (idExists(newId)) {
            throw new IllegalArgumentException("元素ID已存在: " + newId);
        }

        // 验证 targetId 存在
        XmlElement target = getElementById(targetId);
        if (target == null) {
            throw new IllegalArgumentException("目标元素不存在: " + targetId);
        }

        // 不能在根元素前插入
        if (target.isRoot()) {
            throw new IllegalArgumentException("不能在根元素前插入元素");
        }

        // 创建新元素
        XmlElement newElement = new XmlElement(tagName, newId);
        if (text != null && !text.isEmpty()) {
            newElement.setTextContent(text);
        }

        // 在目标元素前插入
        XmlElement parent = target.getParent();
        int index = parent.getChildIndex(target);
        parent.insertChild(index, newElement);
        
        // 更新 id 映射
        idMap.put(newId, newElement);
        modified = true;
    }

    /**
     * 在父元素内追加一个子元素。
     * @param tagName 子元素的标签名
     * @param newId 子元素的 ID
     * @param parentId 父元素的 ID
     * @param text 可选的文本内容
     * @throws IllegalArgumentException 操作无效时抛出
     */
    public void appendChild(String tagName, String newId, String parentId, String text) {
        // 验证 newId 不存在
        if (idExists(newId)) {
            throw new IllegalArgumentException("元素ID已存在: " + newId);
        }

        // 验证 parentId 存在
        XmlElement parent = getElementById(parentId);
        if (parent == null) {
            throw new IllegalArgumentException("父元素不存在: " + parentId);
        }

        // 父元素不能有文本内容（不支持混合内容）
        if (parent.hasTextContent()) {
            throw new IllegalArgumentException("该元素已有文本内容，不支持混合内容");
        }

        // 创建新元素
        XmlElement newElement = new XmlElement(tagName, newId);
        if (text != null && !text.isEmpty()) {
            newElement.setTextContent(text);
        }

        // 追加子元素
        parent.addChild(newElement);
        
        // 更新 id 映射
        idMap.put(newId, newElement);
        modified = true;
    }

    /**
     * 修改元素的 ID。
     * @param oldId 原始 ID
     * @param newId 新 ID
     * @throws IllegalArgumentException 操作无效时抛出
     */
    public void editId(String oldId, String newId) {
        // 验证 oldId 存在
        XmlElement element = getElementById(oldId);
        if (element == null) {
            throw new IllegalArgumentException("元素不存在: " + oldId);
        }

        // 不允许修改根元素 ID
        if (element.isRoot()) {
            throw new IllegalArgumentException("不允许修改根元素ID");
        }

        // 验证 newId 不存在
        if (idExists(newId)) {
            throw new IllegalArgumentException("目标ID已存在: " + newId);
        }

        // 更新 id 映射
        idMap.remove(oldId);
        element.setId(newId);
        idMap.put(newId, element);
        modified = true;
    }

    /**
     * 修改元素的文本内容。
     * @param elementId 元素 ID
     * @param text 新的文本内容
     * @throws IllegalArgumentException 操作无效时抛出
     */
    public void editText(String elementId, String text) {
        // 验证元素存在
        XmlElement element = getElementById(elementId);
        if (element == null) {
            throw new IllegalArgumentException("元素不存在: " + elementId);
        }

        // 元素不能有子元素（不支持混合内容）
        if (element.hasChildren()) {
            throw new IllegalArgumentException("该元素有子元素，不支持混合内容");
        }

        element.setTextContent(text);
        modified = true;
    }

    /**
     * 删除指定元素及其所有子元素。
     * @param elementId 元素 ID
     * @throws IllegalArgumentException 操作无效时抛出
     */
    public void deleteElement(String elementId) {
        // 验证元素存在
        XmlElement element = getElementById(elementId);
        if (element == null) {
            throw new IllegalArgumentException("元素不存在: " + elementId);
        }

        // 不能删除根元素
        if (element.isRoot()) {
            throw new IllegalArgumentException("不能删除根元素");
        }

        // 从父元素中移除
        XmlElement parent = element.getParent();
        parent.removeChild(element);

        // 递归移除所有子元素的 id 映射
        removeFromIdMap(element);
        modified = true;
    }

    /**
     * 递归从 id 映射中移除元素及其子元素。
     */
    private void removeFromIdMap(XmlElement element) {
        idMap.remove(element.getId());
        for (XmlElement child : element.getChildren()) {
            removeFromIdMap(child);
        }
    }

    /**
     * 生成 XML 树形结构的可视化字符串。
     */
    public String generateTree() {
        if (root == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        generateTreeRecursive(root, sb, "", true);
        return sb.toString();
    }

    /**
     * 递归生成树形结构。
     */
    private void generateTreeRecursive(XmlElement element, StringBuilder sb, 
                                        String prefix, boolean isLast) {
        // 构建当前行
        sb.append(element.getTagName());
        
        // 显示属性
        StringBuilder attrs = new StringBuilder();
        attrs.append("id=\"").append(element.getId()).append("\"");
        for (Map.Entry<String, String> attr : element.getAttributes().entrySet()) {
            // 跳过 log 属性的显示（只用于内部控制）
            if (!"log".equals(attr.getKey())) {
                attrs.append(", ").append(attr.getKey()).append("=\"")
                     .append(attr.getValue()).append("\"");
            }
        }
        sb.append(" [").append(attrs).append("]\n");

        // 显示文本内容
        if (element.hasTextContent()) {
            sb.append(prefix).append("└── \"").append(element.getTextContent()).append("\"\n");
        }

        // 递归处理子元素
        List<XmlElement> children = element.getChildren();
        for (int i = 0; i < children.size(); i++) {
            boolean lastChild = (i == children.size() - 1);
            String childPrefix = prefix + (lastChild ? "└── " : "├── ");
            String nextPrefix = prefix + (lastChild ? "    " : "│   ");
            
            sb.append(prefix).append(lastChild ? "└── " : "├── ");
            generateTreeRecursive(children.get(i), sb, nextPrefix, lastChild);
        }
    }

    // ===================== Editor 接口实现（文本操作方法，XML 不直接支持）=====================

    @Override
    public List<String> getLines() {
        // 返回序列化后的 XML 内容按行分割
        if (root == null) {
            return new ArrayList<>();
        }
        String content = xmlAdapter.serialize(root);
        return new ArrayList<>(Arrays.asList(content.split("\n")));
    }

    @Override
    public void setLines(List<String> lines) {
        // 从行列表重新解析 XML
        String content = String.join("\n", lines);
        try {
            loadFromContent(content);
        } catch (XmlParseException e) {
            throw new IllegalArgumentException("无效的 XML 内容: " + e.getMessage());
        }
    }

    @Override
    public String getContent() {
        if (root == null) {
            return "";
        }
        return xmlAdapter.serialize(root);
    }

    @Override
    public void append(String text) {
        // XML 编辑器不支持直接追加文本行
        throw new UnsupportedOperationException("XML 编辑器不支持 append 命令，请使用 append-child");
    }

    @Override
    public void insert(Position position, String text) {
        // XML 编辑器不支持直接插入文本
        throw new UnsupportedOperationException("XML 编辑器不支持 insert 命令，请使用 insert-before");
    }

    @Override
    public void delete(Position position, int length) {
        // XML 编辑器不支持直接删除文本
        throw new UnsupportedOperationException("XML 编辑器不支持 delete 命令，请使用 delete-element");
    }

    @Override
    public void replace(Position position, int length, String text) {
        // XML 编辑器不支持直接替换文本
        throw new UnsupportedOperationException("XML 编辑器不支持 replace 命令");
    }

    @Override
    public String show(int startLine, int endLine) {
        // 显示 XML 内容
        List<String> lines = getLines();
        if (lines.isEmpty()) {
            return "";
        }

        if (startLine == -1 && endLine == -1) {
            startLine = 1;
            endLine = lines.size();
        }

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

    /**
     * 获取所有元素的文本内容（用于拼写检查）。
     * @return 元素 ID 到文本内容的映射
     */
    public Map<String, String> getAllTextContents() {
        Map<String, String> textContents = new LinkedHashMap<>();
        if (root != null) {
            collectTextContents(root, textContents);
        }
        return textContents;
    }

    /**
     * 递归收集所有元素的文本内容。
     */
    private void collectTextContents(XmlElement element, Map<String, String> textContents) {
        if (element.hasTextContent()) {
            textContents.put(element.getId(), element.getTextContent());
        }
        for (XmlElement child : element.getChildren()) {
            collectTextContents(child, textContents);
        }
    }
}
