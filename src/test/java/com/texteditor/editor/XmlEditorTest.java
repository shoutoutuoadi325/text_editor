package com.texteditor.editor;

import com.texteditor.model.XmlElement;
import com.texteditor.xml.XmlParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * XmlEditor 的单元测试。
 * 测试 XML 编辑器的核心功能。
 */
class XmlEditorTest {

    private XmlEditor editor;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws XmlParseException {
        editor = new XmlEditor("test.xml");
        // 加载测试用的 XML 内容
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<bookstore id=\"root\">\n" +
                "    <book id=\"book1\" category=\"COOKING\">\n" +
                "        <title id=\"title1\" lang=\"en\">Everyday Italian</title>\n" +
                "        <author id=\"author1\">Giada De Laurentiis</author>\n" +
                "    </book>\n" +
                "</bookstore>";
        editor.loadFromContent(xml);
    }

    @Test
    void testLoadFromContent() {
        // 验证根元素
        XmlElement root = editor.getRoot();
        assertNotNull(root);
        assertEquals("bookstore", root.getTagName());
        assertEquals("root", root.getId());
        
        // 验证子元素
        assertEquals(1, root.getChildren().size());
        XmlElement book = root.getChildren().get(0);
        assertEquals("book", book.getTagName());
        assertEquals("book1", book.getId());
    }

    @Test
    void testGetElementById() {
        assertNotNull(editor.getElementById("root"));
        assertNotNull(editor.getElementById("book1"));
        assertNotNull(editor.getElementById("title1"));
        assertNull(editor.getElementById("nonexistent"));
    }

    @Test
    void testInsertBefore() {
        // 在 book1 前插入新书
        editor.insertBefore("book", "newBook", "book1", "");
        
        XmlElement root = editor.getRoot();
        assertEquals(2, root.getChildren().size());
        assertEquals("newBook", root.getChildren().get(0).getId());
        assertEquals("book1", root.getChildren().get(1).getId());
        assertTrue(editor.isModified());
    }

    @Test
    void testInsertBeforeWithDuplicateId() {
        // 尝试使用已存在的 ID
        assertThrows(IllegalArgumentException.class, () -> {
            editor.insertBefore("book", "book1", "book1", "");
        });
    }

    @Test
    void testInsertBeforeInvalidTarget() {
        // 尝试在不存在的元素前插入
        assertThrows(IllegalArgumentException.class, () -> {
            editor.insertBefore("book", "newBook", "nonexistent", "");
        });
    }

    @Test
    void testInsertBeforeRoot() {
        // 尝试在根元素前插入
        assertThrows(IllegalArgumentException.class, () -> {
            editor.insertBefore("book", "newBook", "root", "");
        });
    }

    @Test
    void testAppendChild() {
        // 向 book1 追加子元素
        editor.appendChild("price", "price1", "book1", "29.99");
        
        XmlElement book = editor.getElementById("book1");
        assertEquals(3, book.getChildren().size());
        XmlElement price = book.getChildren().get(2);
        assertEquals("price", price.getTagName());
        assertEquals("price1", price.getId());
        assertEquals("29.99", price.getTextContent());
        assertTrue(editor.isModified());
    }

    @Test
    void testAppendChildWithDuplicateId() {
        assertThrows(IllegalArgumentException.class, () -> {
            editor.appendChild("price", "title1", "book1", "29.99");
        });
    }

    @Test
    void testAppendChildToInvalidParent() {
        assertThrows(IllegalArgumentException.class, () -> {
            editor.appendChild("price", "price1", "nonexistent", "29.99");
        });
    }

    @Test
    void testAppendChildToElementWithText() {
        // title1 有文本内容，不能添加子元素
        assertThrows(IllegalArgumentException.class, () -> {
            editor.appendChild("subtitle", "sub1", "title1", "test");
        });
    }

    @Test
    void testEditId() {
        editor.editId("book1", "book");
        
        assertNull(editor.getElementById("book1"));
        assertNotNull(editor.getElementById("book"));
        assertTrue(editor.isModified());
    }

    @Test
    void testEditIdNonexistent() {
        assertThrows(IllegalArgumentException.class, () -> {
            editor.editId("nonexistent", "newId");
        });
    }

    @Test
    void testEditIdDuplicate() {
        assertThrows(IllegalArgumentException.class, () -> {
            editor.editId("book1", "title1");
        });
    }

    @Test
    void testEditIdRoot() {
        // 不允许修改根元素 ID
        assertThrows(IllegalArgumentException.class, () -> {
            editor.editId("root", "newRoot");
        });
    }

    @Test
    void testEditText() {
        editor.editText("title1", "New Book Title");
        
        XmlElement title = editor.getElementById("title1");
        assertEquals("New Book Title", title.getTextContent());
        assertTrue(editor.isModified());
    }

    @Test
    void testEditTextNonexistent() {
        assertThrows(IllegalArgumentException.class, () -> {
            editor.editText("nonexistent", "test");
        });
    }

    @Test
    void testEditTextOnParent() {
        // book1 有子元素，不能设置文本内容
        assertThrows(IllegalArgumentException.class, () -> {
            editor.editText("book1", "test");
        });
    }

    @Test
    void testDeleteElement() {
        editor.deleteElement("title1");
        
        assertNull(editor.getElementById("title1"));
        XmlElement book = editor.getElementById("book1");
        assertEquals(1, book.getChildren().size());
        assertTrue(editor.isModified());
    }

    @Test
    void testDeleteElementNonexistent() {
        assertThrows(IllegalArgumentException.class, () -> {
            editor.deleteElement("nonexistent");
        });
    }

    @Test
    void testDeleteRoot() {
        assertThrows(IllegalArgumentException.class, () -> {
            editor.deleteElement("root");
        });
    }

    @Test
    void testDeleteElementWithChildren() {
        // 删除 book1 应该也删除其所有子元素
        editor.deleteElement("book1");
        
        assertNull(editor.getElementById("book1"));
        assertNull(editor.getElementById("title1"));
        assertNull(editor.getElementById("author1"));
    }

    @Test
    void testGenerateTree() {
        String tree = editor.generateTree();
        
        assertNotNull(tree);
        assertTrue(tree.contains("bookstore"));
        assertTrue(tree.contains("book"));
        assertTrue(tree.contains("title"));
        assertTrue(tree.contains("Everyday Italian"));
    }

    @Test
    void testGetContent() {
        String content = editor.getContent();
        
        assertNotNull(content);
        assertTrue(content.contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(content.contains("<bookstore"));
        assertTrue(content.contains("</bookstore>"));
    }

    @Test
    void testGetAllTextContents() {
        var textContents = editor.getAllTextContents();
        
        assertTrue(textContents.containsKey("title1"));
        assertTrue(textContents.containsKey("author1"));
        assertEquals("Everyday Italian", textContents.get("title1"));
        assertEquals("Giada De Laurentiis", textContents.get("author1"));
    }

    @Test
    void testLoggingEnabled() throws XmlParseException {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root id=\"root\" log=\"true\"></root>";
        XmlEditor logEditor = new XmlEditor("log_test.xml");
        logEditor.loadFromContent(xml);
        
        assertTrue(logEditor.isLoggingEnabled());
    }
}
