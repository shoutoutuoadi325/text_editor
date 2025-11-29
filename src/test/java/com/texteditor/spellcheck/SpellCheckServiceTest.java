package com.texteditor.spellcheck;

import com.texteditor.editor.TextEditor;
import com.texteditor.editor.XmlEditor;
import com.texteditor.xml.XmlParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SpellCheckService 的单元测试。
 * 测试拼写检查服务对文本文件和 XML 文件的处理。
 */
class SpellCheckServiceTest {

    private SpellCheckService service;
    private SpellChecker mockChecker;

    @BeforeEach
    void setUp() {
        // 使用实际的 SimpleSpellChecker
        service = new SpellCheckService(new SimpleSpellChecker());
    }

    @Test
    void testCheckTextEditor() {
        TextEditor editor = new TextEditor("test.txt");
        editor.append("Hello world");
        
        String result = service.check(editor);
        
        assertNotNull(result);
        assertTrue(result.contains("拼写检查结果"));
    }

    @Test
    void testCheckTextEditorWithErrors() {
        TextEditor editor = new TextEditor("test.txt");
        editor.append("Helo wrold");  // 拼写错误
        
        String result = service.check(editor);
        
        assertNotNull(result);
        assertTrue(result.contains("拼写检查结果"));
        // 应该检测到错误
        assertTrue(result.contains("第") || result.contains("行"));
    }

    @Test
    void testCheckXmlEditor() throws XmlParseException {
        XmlEditor editor = new XmlEditor("test.xml");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root id=\"root\">\n" +
                "    <title id=\"title1\">Hello world</title>\n" +
                "</root>";
        editor.loadFromContent(xml);
        
        String result = service.check(editor);
        
        assertNotNull(result);
        assertTrue(result.contains("拼写检查结果"));
    }

    @Test
    void testCheckXmlEditorWithErrors() throws XmlParseException {
        XmlEditor editor = new XmlEditor("test.xml");
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root id=\"root\">\n" +
                "    <title id=\"title1\">Helo wrold</title>\n" +
                "</root>";
        editor.loadFromContent(xml);
        
        String result = service.check(editor);
        
        assertNotNull(result);
        assertTrue(result.contains("拼写检查结果"));
        // XML 错误格式：元素 elementId: ...
        assertTrue(result.contains("元素") || result.contains("title1"));
    }

    @Test
    void testCheckWithUnavailableChecker() {
        // 创建一个不可用的检查器
        SpellChecker unavailableChecker = new SpellChecker() {
            @Override
            public List<SpellingError> checkText(String text) {
                return Arrays.asList();
            }
            @Override
            public boolean isCorrect(String word) {
                return true;
            }
            @Override
            public List<String> getSuggestions(String word) {
                return Arrays.asList();
            }
            @Override
            public boolean isAvailable() {
                return false;
            }
        };
        
        SpellCheckService unavailableService = new SpellCheckService(unavailableChecker);
        TextEditor editor = new TextEditor("test.txt");
        editor.append("test");
        
        String result = unavailableService.check(editor);
        
        assertTrue(result.contains("不可用") || result.contains("警告"));
    }

    @Test
    void testSpellingErrorFormat() {
        // 测试文本文件错误格式
        SpellingError textError = new SpellingError(1, 5, "helo", Arrays.asList("hello"));
        String textFormat = textError.format();
        assertTrue(textFormat.contains("第 1 行"));
        assertTrue(textFormat.contains("第 5 列"));
        assertTrue(textFormat.contains("helo"));
        assertTrue(textFormat.contains("hello"));

        // 测试 XML 文件错误格式
        SpellingError xmlError = new SpellingError("title1", "helo", Arrays.asList("hello"));
        String xmlFormat = xmlError.format();
        assertTrue(xmlFormat.contains("元素 title1"));
        assertTrue(xmlFormat.contains("helo"));
        assertTrue(xmlFormat.contains("hello"));
    }

    @Test
    void testSpellingErrorIsTextFileError() {
        SpellingError textError = new SpellingError(1, 5, "helo", Arrays.asList("hello"));
        assertTrue(textError.isTextFileError());

        SpellingError xmlError = new SpellingError("title1", "helo", Arrays.asList("hello"));
        assertFalse(xmlError.isTextFileError());
    }
}
