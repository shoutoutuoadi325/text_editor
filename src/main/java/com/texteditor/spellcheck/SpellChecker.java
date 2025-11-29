package com.texteditor.spellcheck;

import java.util.List;

/**
 * 拼写检查器接口，定义拼写检查的核心功能。
 * 使用适配器模式实现，支持不同的拼写检查后端。
 */
public interface SpellChecker {
    
    /**
     * 检查文本内容中的拼写错误。
     * @param text 要检查的文本
     * @return 拼写错误列表
     */
    List<SpellingError> checkText(String text);
    
    /**
     * 检查单词是否拼写正确。
     * @param word 要检查的单词
     * @return 是否正确
     */
    boolean isCorrect(String word);
    
    /**
     * 获取单词的拼写建议。
     * @param word 错误的单词
     * @return 建议列表
     */
    List<String> getSuggestions(String word);
    
    /**
     * 检查拼写检查器是否可用。
     * @return 是否可用
     */
    boolean isAvailable();
}
