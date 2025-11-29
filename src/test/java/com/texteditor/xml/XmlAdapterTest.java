package com.texteditor.xml;

import com.texteditor.model.XmlElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XmlAdapter 的单元测试。
 * 测试 XML 解析和序列化功能。
 */
class XmlAdapterTest {

    private XmlAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new XmlAdapter();
    }

    @Test
    void testParseSimpleXml() throws XmlParseException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root id=\"root\"></root>";
        
        XmlElement root = adapter.parse(xml);
        
        assertNotNull(root);
        assertEquals("root", root.getTagName());
        assertEquals("root", root.getId());
        assertTrue(root.getChildren().isEmpty());
    }

    @Test
    void testParseXmlWithChildren() throws XmlParseException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<parent id=\"p1\">\n" +
                "    <child id=\"c1\">text</child>\n" +
                "</parent>";
        
        XmlElement root = adapter.parse(xml);
        
        assertEquals(1, root.getChildren().size());
        XmlElement child = root.getChildren().get(0);
        assertEquals("child", child.getTagName());
        assertEquals("c1", child.getId());
        assertEquals("text", child.getTextContent());
    }

    @Test
    void testParseXmlWithAttributes() throws XmlParseException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<element id=\"e1\" attr1=\"value1\" attr2=\"value2\"></element>";
        
        XmlElement root = adapter.parse(xml);
        
        assertEquals("value1", root.getAttribute("attr1"));
        assertEquals("value2", root.getAttribute("attr2"));
    }

    @Test
    void testParseMissingId() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root></root>";  // 缺少 id 属性
        
        assertThrows(XmlParseException.class, () -> adapter.parse(xml));
    }

    @Test
    void testParseInvalidXml() {
        String xml = "not valid xml";
        
        assertThrows(XmlParseException.class, () -> adapter.parse(xml));
    }

    @Test
    void testSerializeSimple() {
        XmlElement root = new XmlElement("root", "root");
        
        String xml = adapter.serialize(root);
        
        assertTrue(xml.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(xml.contains("<root id=\"root\""));
    }

    @Test
    void testSerializeWithChildren() {
        XmlElement root = new XmlElement("parent", "p1");
        XmlElement child = new XmlElement("child", "c1");
        child.setTextContent("test text");
        root.addChild(child);
        
        String xml = adapter.serialize(root);
        
        assertTrue(xml.contains("<parent"));
        assertTrue(xml.contains("<child"));
        assertTrue(xml.contains("test text"));
    }

    @Test
    void testSerializeWithAttributes() {
        XmlElement root = new XmlElement("element", "e1");
        root.setAttribute("attr1", "value1");
        
        String xml = adapter.serialize(root);
        
        assertTrue(xml.contains("id=\"e1\""));
        assertTrue(xml.contains("attr1=\"value1\""));
    }

    @Test
    void testRoundTrip() throws XmlParseException {
        String original = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bookstore id=\"root\">\n" +
                "    <book id=\"book1\" category=\"COOKING\">\n" +
                "        <title id=\"title1\" lang=\"en\">Everyday Italian</title>\n" +
                "    </book>\n" +
                "</bookstore>";
        
        // 解析
        XmlElement root = adapter.parse(original);
        
        // 序列化
        String serialized = adapter.serialize(root);
        
        // 再次解析
        XmlElement root2 = adapter.parse(serialized);
        
        // 验证结构相同
        assertEquals(root.getTagName(), root2.getTagName());
        assertEquals(root.getId(), root2.getId());
        assertEquals(root.getChildren().size(), root2.getChildren().size());
    }

    @Test
    void testEscapeSpecialCharacters() {
        XmlElement root = new XmlElement("element", "e1");
        root.setTextContent("Text with <special> & \"characters\"");
        
        String xml = adapter.serialize(root);
        
        assertTrue(xml.contains("&lt;special&gt;"));
        assertTrue(xml.contains("&amp;"));
        assertTrue(xml.contains("&quot;"));
    }
}
