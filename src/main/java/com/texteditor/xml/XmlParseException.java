package com.texteditor.xml;

/**
 * XML 解析异常类。
 */
public class XmlParseException extends Exception {
    
    public XmlParseException(String message) {
        super(message);
    }

    public XmlParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
