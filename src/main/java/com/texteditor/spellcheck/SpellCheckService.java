package com.texteditor.spellcheck;

import com.texteditor.editor.Editor;
import com.texteditor.editor.TextEditor;
import com.texteditor.editor.XmlEditor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 拼写检查服务，统一处理文本文件和 XML 文件的拼写检查。
 * 
 * 检查范围：
 * - 文本文件(.txt)：检查所有文本内容
 * - XML文件(.xml)：仅检查元素的文本内容，不检查标签名、属性名、属性值
 */
public class SpellCheckService {
    
    private final SpellChecker spellChecker;

    /**
     * 使用指定的拼写检查器创建服务。
     * @param spellChecker 拼写检查器实现
     */
    public SpellCheckService(SpellChecker spellChecker) {
        this.spellChecker = spellChecker;
    }

    /**
     * 检查编辑器中的拼写错误。
     * @param editor 编辑器
     * @return 格式化的拼写检查结果
     */
    public String check(Editor editor) {
        if (!spellChecker.isAvailable()) {
            return "警告: 拼写检查服务不可用";
        }

        List<SpellingError> errors;
        
        if (editor instanceof XmlEditor) {
            errors = checkXmlEditor((XmlEditor) editor);
        } else if (editor instanceof TextEditor) {
            errors = checkTextEditor((TextEditor) editor);
        } else {
            return "不支持的编辑器类型";
        }

        return formatResults(errors);
    }

    /**
     * 检查文本编辑器中的拼写错误。
     * 如果文件启用了日志（第一行为 "# log"），则跳过第一行。
     */
    private List<SpellingError> checkTextEditor(TextEditor editor) {
        List<String> lines = editor.getLines();
        
        // 检查是否启用了日志（第一行为 "# log"）
        boolean hasLogMarker = !lines.isEmpty() && lines.get(0).trim().equals("# log");
        
        String content;
        if (hasLogMarker && lines.size() > 1) {
            // 跳过第一行，从第二行开始检查
            content = String.join("\n", lines.subList(1, lines.size()));
            List<SpellingError> errors = spellChecker.checkText(content);
            // 调整行号，因为跳过了第一行
            List<SpellingError> adjustedErrors = new ArrayList<>();
            for (SpellingError error : errors) {
                adjustedErrors.add(new SpellingError(
                    error.getLine() + 1,  // 行号加 1
                    error.getColumn(),
                    error.getWord(),
                    error.getSuggestions()
                ));
            }
            return adjustedErrors;
        } else if (hasLogMarker) {
            // 文件只有 "# log" 这一行，无需检查
            return new ArrayList<>();
        } else {
            content = editor.getContent();
            return spellChecker.checkText(content);
        }
    }

    /**
     * 检查 XML 编辑器中的拼写错误（仅检查元素文本内容）。
     */
    private List<SpellingError> checkXmlEditor(XmlEditor editor) {
        List<SpellingError> errors = new ArrayList<>();
        
        // 获取所有元素的文本内容
        Map<String, String> textContents = editor.getAllTextContents();
        
        for (Map.Entry<String, String> entry : textContents.entrySet()) {
            String elementId = entry.getKey();
            String text = entry.getValue();
            
            if (text == null || text.isEmpty()) {
                continue;
            }
            
            // 检查文本内容中的每个单词
            List<SpellingError> textErrors = spellChecker.checkText(text);
            
            // 将文本文件格式的错误转换为 XML 格式
            for (SpellingError error : textErrors) {
                errors.add(new SpellingError(elementId, error.getWord(), error.getSuggestions()));
            }
        }
        
        return errors;
    }

    /**
     * 格式化拼写检查结果。
     */
    private String formatResults(List<SpellingError> errors) {
        StringBuilder sb = new StringBuilder();
        sb.append("拼写检查结果:\n");
        
        if (errors.isEmpty()) {
            sb.append("未发现拼写错误");
        } else {
            for (SpellingError error : errors) {
                sb.append(error.format()).append("\n");
            }
        }
        
        return sb.toString().trim();
    }

    /**
     * 获取底层的拼写检查器。
     */
    public SpellChecker getSpellChecker() {
        return spellChecker;
    }
}
