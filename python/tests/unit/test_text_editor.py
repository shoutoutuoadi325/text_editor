"""
Unit tests for TextEditor class.
"""
import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '../../src'))

import pytest
from text_editor.editors.text_editor import TextEditor


class TestTextEditor:
    """Test TextEditor class."""
    
    def test_init_empty(self):
        """Test initialization with empty content."""
        editor = TextEditor("test.txt")
        
        assert editor.filepath == "test.txt"
        assert editor.get_content() == ""
        assert editor.is_modified() == False
    
    def test_init_with_content(self):
        """Test initialization with content."""
        editor = TextEditor("test.txt", "Line 1\nLine 2")
        
        assert editor.get_content() == "Line 1\nLine 2"
        assert editor.get_line_count() == 2
    
    def test_append(self):
        """Test append operation."""
        editor = TextEditor("test.txt", "Line 1")
        
        result = editor.append("Line 2")
        
        assert result == True
        assert editor.get_content() == "Line 1\nLine 2"
        assert editor.is_modified() == True
    
    def test_insert_simple(self):
        """Test simple insert operation."""
        editor = TextEditor("test.txt", "abcdef")
        
        success, msg = editor.insert(1, 4, "XYZ")
        
        assert success == True
        assert editor.get_content() == "abcXYZdef"
    
    def test_insert_empty_file(self):
        """Test insert in empty file."""
        editor = TextEditor("test.txt")
        
        success, msg = editor.insert(1, 1, "Hello")
        
        assert success == True
        assert editor.get_content() == "Hello"
    
    def test_insert_empty_file_wrong_position(self):
        """Test insert in empty file at wrong position."""
        editor = TextEditor("test.txt")
        
        success, msg = editor.insert(1, 2, "Hello")
        
        assert success == False
        assert "空文件只能在1:1位置插入" in msg
    
    def test_insert_with_newline(self):
        """Test insert with newline character."""
        editor = TextEditor("test.txt", "Line 1")
        
        success, msg = editor.insert(1, 7, "\nLine 2")
        
        assert success == True
        assert editor.get_content() == "Line 1\nLine 2"
    
    def test_insert_out_of_bounds(self):
        """Test insert with invalid position."""
        editor = TextEditor("test.txt", "Line 1")
        
        success, msg = editor.insert(5, 1, "text")
        
        assert success == False
        assert "行号或列号越界" in msg
    
    def test_delete_simple(self):
        """Test simple delete operation."""
        editor = TextEditor("test.txt", "Hello world")
        
        success, msg = editor.delete(1, 7, 5)
        
        assert success == True
        assert editor.get_content() == "Hello "
    
    def test_delete_beyond_line_end(self):
        """Test delete beyond line end."""
        editor = TextEditor("test.txt", "Hello")
        
        success, msg = editor.delete(1, 3, 10)
        
        assert success == False
        assert "删除长度超出行尾" in msg
    
    def test_delete_out_of_bounds(self):
        """Test delete with invalid position."""
        editor = TextEditor("test.txt", "Line 1")
        
        success, msg = editor.delete(5, 1, 1)
        
        assert success == False
        assert "行号或列号越界" in msg
    
    def test_replace_simple(self):
        """Test simple replace operation."""
        editor = TextEditor("test.txt", "fast fox")
        
        success, msg = editor.replace(1, 1, 4, "slow")
        
        assert success == True
        assert editor.get_content() == "slow fox"
    
    def test_show_all(self):
        """Test show all content."""
        editor = TextEditor("test.txt", "Line 1\nLine 2\nLine 3")
        
        result = editor.show()
        
        assert "1: Line 1" in result
        assert "2: Line 2" in result
        assert "3: Line 3" in result
    
    def test_show_range(self):
        """Test show with range."""
        editor = TextEditor("test.txt", "Line 1\nLine 2\nLine 3")
        
        result = editor.show(1, 2)
        
        assert "1: Line 1" in result
        assert "2: Line 2" in result
        assert "3: Line 3" not in result
    
    def test_show_empty(self):
        """Test show empty file."""
        editor = TextEditor("test.txt")
        
        result = editor.show()
        
        assert result == ""
    
    def test_undo_append(self):
        """Test undo after append."""
        editor = TextEditor("test.txt", "Line 1")
        
        editor.append("Line 2")
        assert editor.get_content() == "Line 1\nLine 2"
        
        editor.undo()
        assert editor.get_content() == "Line 1"
    
    def test_undo_insert(self):
        """Test undo after insert."""
        editor = TextEditor("test.txt", "Hello")
        
        editor.insert(1, 6, " world")
        assert editor.get_content() == "Hello world"
        
        editor.undo()
        assert editor.get_content() == "Hello"
    
    def test_undo_delete(self):
        """Test undo after delete."""
        editor = TextEditor("test.txt", "Hello world")
        
        editor.delete(1, 7, 5)
        assert editor.get_content() == "Hello "
        
        editor.undo()
        assert editor.get_content() == "Hello world"
    
    def test_redo(self):
        """Test redo operation."""
        editor = TextEditor("test.txt", "Line 1")
        
        editor.append("Line 2")
        editor.undo()
        editor.redo()
        
        assert editor.get_content() == "Line 1\nLine 2"
    
    def test_can_undo_redo(self):
        """Test can_undo and can_redo."""
        editor = TextEditor("test.txt", "Line 1")
        
        assert editor.can_undo() == False
        assert editor.can_redo() == False
        
        editor.append("Line 2")
        assert editor.can_undo() == True
        assert editor.can_redo() == False
        
        editor.undo()
        assert editor.can_undo() == False
        assert editor.can_redo() == True


if __name__ == '__main__':
    pytest.main([__file__, '-v'])
