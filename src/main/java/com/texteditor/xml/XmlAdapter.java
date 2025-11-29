package com.texteditor.xml;

import com.texteditor.model.XmlElement;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * XML 解析和序列化适配器。
 * 使用适配器模式封装底层 XML 解析库（Java DOM API），
 * 将标准 DOM 转换为自定义的 XmlElement 结构。
 */
public class XmlAdapter {

    /**
     * 从 XML 字符串解析为 XmlElement 树。
     * @param xmlContent XML 字符串内容
     * @return 根元素
     * @throws XmlParseException 解析失败时抛出
     */
    public XmlElement parse(String xmlContent) throws XmlParseException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 禁用外部实体以防止 XXE 攻击
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlContent));
            Document doc = builder.parse(is);
            
            Element root = doc.getDocumentElement();
            return convertToXmlElement(root);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new XmlParseException("XML 解析失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从文件解析 XML。
     * @param filePath 文件路径
     * @return 根元素
     * @throws XmlParseException 解析失败时抛出
     */
    public XmlElement parseFile(String filePath) throws XmlParseException, IOException {
        try (BufferedReader reader = new BufferedReader(
                new FileReader(filePath, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return parse(content.toString());
        }
    }

    /**
     * 将 DOM Element 转换为 XmlElement。
     */
    private XmlElement convertToXmlElement(Element domElement) throws XmlParseException {
        // 获取 id 属性
        String id = domElement.getAttribute("id");
        if (id == null || id.isEmpty()) {
            throw new XmlParseException("元素缺少 id 属性: " + domElement.getTagName());
        }

        XmlElement element = new XmlElement(domElement.getTagName(), id);

        // 复制所有属性（除了 id）
        NamedNodeMap attrs = domElement.getAttributes();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr attr = (Attr) attrs.item(i);
            if (!attr.getName().equals("id")) {
                element.setAttribute(attr.getName(), attr.getValue());
            }
        }

        // 处理子节点
        NodeList childNodes = domElement.getChildNodes();
        boolean hasElementChildren = false;
        StringBuilder textContent = new StringBuilder();

        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                hasElementChildren = true;
                element.addChild(convertToXmlElement((Element) child));
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getTextContent().trim();
                if (!text.isEmpty()) {
                    textContent.append(text);
                }
            }
        }

        // 如果没有子元素，设置文本内容
        if (!hasElementChildren && textContent.length() > 0) {
            element.setTextContent(textContent.toString());
        }

        return element;
    }

    /**
     * 将 XmlElement 树序列化为 XML 字符串。
     * @param root 根元素
     * @return XML 字符串
     */
    public String serialize(XmlElement root) {
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        serializeElement(root, sb, 0);
        return sb.toString();
    }

    /**
     * 递归序列化元素。
     */
    private void serializeElement(XmlElement element, StringBuilder sb, int indent) {
        // 添加缩进
        String indentStr = "    ".repeat(indent);
        sb.append(indentStr);

        // 开始标签
        sb.append("<").append(element.getTagName());
        
        // id 属性放在最前面
        sb.append(" id=\"").append(escapeXml(element.getId())).append("\"");
        
        // 其他属性
        for (Map.Entry<String, String> attr : element.getAttributes().entrySet()) {
            sb.append(" ").append(attr.getKey()).append("=\"")
              .append(escapeXml(attr.getValue())).append("\"");
        }

        // 检查内容
        if (element.hasChildren()) {
            sb.append(">\n");
            for (XmlElement child : element.getChildren()) {
                serializeElement(child, sb, indent + 1);
            }
            sb.append(indentStr).append("</").append(element.getTagName()).append(">\n");
        } else if (element.hasTextContent()) {
            sb.append(">").append(escapeXml(element.getTextContent()))
              .append("</").append(element.getTagName()).append(">\n");
        } else {
            sb.append("></").append(element.getTagName()).append(">\n");
        }
    }

    /**
     * 转义 XML 特殊字符。
     */
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    /**
     * 将 XmlElement 树保存到文件。
     * @param root 根元素
     * @param filePath 文件路径
     */
    public void saveToFile(XmlElement root, String filePath) throws IOException {
        String content = serialize(root);
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(filePath, StandardCharsets.UTF_8))) {
            writer.write(content);
        }
    }
}
