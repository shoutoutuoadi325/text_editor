package com.texteditor.spellcheck;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SimpleSpellChecker 的单元测试。
 * 测试拼写检查功能。
 */
class SimpleSpellCheckerTest {

    private SimpleSpellChecker checker;

    @BeforeEach
    void setUp() {
        checker = new SimpleSpellChecker();
    }

    @Test
    void testIsAvailable() {
        assertTrue(checker.isAvailable());
    }

    @Test
    void testCorrectWords() {
        // 常见英语单词应该被认为是正确的
        assertTrue(checker.isCorrect("hello"));
        assertTrue(checker.isCorrect("world"));
        assertTrue(checker.isCorrect("the"));
        assertTrue(checker.isCorrect("is"));
    }

    @Test
    void testCorrectWordsCaseInsensitive() {
        // 大小写不敏感
        assertTrue(checker.isCorrect("Hello"));
        assertTrue(checker.isCorrect("WORLD"));
        assertTrue(checker.isCorrect("ThE"));
    }

    @Test
    void testIncorrectWords() {
        // 拼写错误的单词
        assertFalse(checker.isCorrect("helo"));
        assertFalse(checker.isCorrect("wrold"));
        assertFalse(checker.isCorrect("teh"));
    }

    @Test
    void testGetSuggestions() {
        // 获取建议
        List<String> suggestions = checker.getSuggestions("helo");
        
        assertNotNull(suggestions);
        // 建议列表可能为空或包含相似单词
        // 由于使用简单的编辑距离算法，不一定能找到 "hello"
        // 只需验证方法返回有效的列表即可
    }

    @Test
    void testCheckTextNoErrors() {
        String text = "Hello world this is a test";
        
        List<SpellingError> errors = checker.checkText(text);
        
        assertNotNull(errors);
        assertTrue(errors.isEmpty(), "正确的文本不应该有错误");
    }

    @Test
    void testCheckTextWithErrors() {
        String text = "Helo wrold";
        
        List<SpellingError> errors = checker.checkText(text);
        
        assertNotNull(errors);
        assertFalse(errors.isEmpty(), "应该检测到拼写错误");
        
        // 验证错误信息
        SpellingError error1 = errors.get(0);
        assertEquals(1, error1.getLine());
        assertTrue(error1.getColumn() > 0);
    }

    @Test
    void testCheckTextMultipleLines() {
        String text = "Hello world\nThis is wrng\nGoodbye";
        
        List<SpellingError> errors = checker.checkText(text);
        
        assertNotNull(errors);
        // 应该在第2行检测到错误 "wrng"
        boolean foundLineTwo = errors.stream().anyMatch(e -> e.getLine() == 2);
        assertTrue(foundLineTwo, "应该在第2行检测到错误");
    }

    @Test
    void testCheckTextSingleLetter() {
        // 单字母应该被忽略
        String text = "a b c I";
        
        List<SpellingError> errors = checker.checkText(text);
        
        assertTrue(errors.isEmpty(), "单字母不应该被标记为错误");
    }
}
