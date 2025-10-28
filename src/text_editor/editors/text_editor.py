"""
Text editor implementation with line-based text storage.
"""
from typing import List, Optional, Tuple
from ..commands.command import Command, CommandHistory


class TextEditor:
    """
    Text editor that stores content as a list of lines.
    Supports basic text operations: append, insert, delete, replace.
    """
    
    def __init__(self, filepath: str, content: str = ""):
        """
        Initialize text editor.
        
        Args:
            filepath: Path to the file
            content: Initial content (optional)
        """
        self.filepath = filepath
        self._lines: List[str] = []
        self._modified = False
        self._command_history = CommandHistory()
        
        if content:
            self.set_content(content)
    
    def set_content(self, content: str):
        """Set editor content from string."""
        if content:
            self._lines = content.split('\n')
        else:
            self._lines = []
    
    def get_content(self) -> str:
        """Get editor content as string."""
        return '\n'.join(self._lines)
    
    def get_lines(self) -> List[str]:
        """Get a copy of the lines."""
        return self._lines.copy()
    
    def set_lines(self, lines: List[str]):
        """Set the lines directly."""
        self._lines = lines.copy()
        self._modified = True
    
    def is_modified(self) -> bool:
        """Check if editor has unsaved changes."""
        return self._modified
    
    def set_modified(self, modified: bool):
        """Set modified status."""
        self._modified = modified
    
    def get_line_count(self) -> int:
        """Get number of lines."""
        return len(self._lines)
    
    def append(self, text: str) -> bool:
        """
        Append a line of text at the end.
        
        Args:
            text: Text to append
            
        Returns:
            bool: True if successful
        """
        cmd = AppendCommand(self, text)
        return self._command_history.execute_command(cmd)
    
    def insert(self, line: int, col: int, text: str) -> Tuple[bool, str]:
        """
        Insert text at specified position.
        
        Args:
            line: Line number (1-based)
            col: Column number (1-based)
            text: Text to insert (may contain newlines)
            
        Returns:
            Tuple of (success, error_message)
        """
        # Validate position
        if line < 1:
            return False, "行号或列号越界"
        
        if len(self._lines) == 0:
            if line != 1 or col != 1:
                return False, "空文件只能在1:1位置插入"
        else:
            if line > len(self._lines):
                return False, "行号或列号越界"
            if col < 1 or col > len(self._lines[line - 1]) + 1:
                return False, "行号或列号越界"
        
        cmd = InsertCommand(self, line, col, text)
        success = self._command_history.execute_command(cmd)
        return success, "" if success else "插入失败"
    
    def delete(self, line: int, col: int, length: int) -> Tuple[bool, str]:
        """
        Delete characters starting from specified position.
        
        Args:
            line: Line number (1-based)
            col: Column number (1-based)
            length: Number of characters to delete
            
        Returns:
            Tuple of (success, error_message)
        """
        # Validate position
        if line < 1 or line > len(self._lines):
            return False, "行号或列号越界"
        
        line_content = self._lines[line - 1]
        if col < 1 or col > len(line_content):
            return False, "行号或列号越界"
        
        # Check if deletion would go beyond line end
        if col + length - 1 > len(line_content):
            return False, "删除长度超出行尾"
        
        cmd = DeleteCommand(self, line, col, length)
        success = self._command_history.execute_command(cmd)
        return success, "" if success else "删除失败"
    
    def replace(self, line: int, col: int, length: int, text: str) -> Tuple[bool, str]:
        """
        Replace characters with new text.
        
        Args:
            line: Line number (1-based)
            col: Column number (1-based)
            length: Number of characters to replace
            text: New text
            
        Returns:
            Tuple of (success, error_message)
        """
        cmd = ReplaceCommand(self, line, col, length, text)
        success = self._command_history.execute_command(cmd)
        return success, "" if success else "替换失败"
    
    def show(self, start_line: Optional[int] = None, end_line: Optional[int] = None) -> str:
        """
        Get text content for display.
        
        Args:
            start_line: Start line number (1-based, optional)
            end_line: End line number (1-based, inclusive, optional)
            
        Returns:
            Formatted text with line numbers
        """
        if not self._lines:
            return ""
        
        start = 1 if start_line is None else start_line
        end = len(self._lines) if end_line is None else end_line
        
        # Clamp to valid range
        start = max(1, min(start, len(self._lines)))
        end = max(1, min(end, len(self._lines)))
        
        result = []
        for i in range(start - 1, end):
            result.append(f"{i + 1}: {self._lines[i]}")
        
        return '\n'.join(result)
    
    def undo(self) -> bool:
        """Undo last operation."""
        cmd = self._command_history.undo()
        if cmd:
            self._modified = True
            return True
        return False
    
    def redo(self) -> bool:
        """Redo last undone operation."""
        cmd = self._command_history.redo()
        if cmd:
            self._modified = True
            return True
        return False
    
    def can_undo(self) -> bool:
        """Check if undo is available."""
        return self._command_history.can_undo()
    
    def can_redo(self) -> bool:
        """Check if redo is available."""
        return self._command_history.can_redo()


# Text editing commands

class AppendCommand(Command):
    """Command to append text to the end."""
    
    def __init__(self, editor: TextEditor, text: str):
        self.editor = editor
        self.text = text
        self.line_count_before = 0
    
    def execute(self) -> bool:
        self.line_count_before = self.editor.get_line_count()
        self.editor._lines.append(self.text)
        self.editor._modified = True
        return True
    
    def undo(self) -> bool:
        if len(self.editor._lines) > self.line_count_before:
            self.editor._lines.pop()
            self.editor._modified = True
            return True
        return False
    
    def redo(self) -> bool:
        return self.execute()


class InsertCommand(Command):
    """Command to insert text at a position."""
    
    def __init__(self, editor: TextEditor, line: int, col: int, text: str):
        self.editor = editor
        self.line = line
        self.col = col
        self.text = text
        self.old_lines = []
    
    def execute(self) -> bool:
        # Save old state
        self.old_lines = self.editor._lines.copy()
        
        # Handle empty file
        if len(self.editor._lines) == 0:
            if '\n' in self.text:
                self.editor._lines = self.text.split('\n')
            else:
                self.editor._lines = [self.text]
        else:
            line_idx = self.line - 1
            col_idx = self.col - 1
            
            current_line = self.editor._lines[line_idx]
            
            # Split text by newlines
            if '\n' in self.text:
                text_lines = self.text.split('\n')
                # First part: before insertion point + first line of new text
                new_first_line = current_line[:col_idx] + text_lines[0]
                # Last part: last line of new text + after insertion point
                new_last_line = text_lines[-1] + current_line[col_idx:]
                
                # Build new lines list
                new_lines = (
                    self.editor._lines[:line_idx] +
                    [new_first_line] +
                    text_lines[1:-1] +
                    [new_last_line] +
                    self.editor._lines[line_idx + 1:]
                )
                self.editor._lines = new_lines
            else:
                # Simple insertion on same line
                new_line = current_line[:col_idx] + self.text + current_line[col_idx:]
                self.editor._lines[line_idx] = new_line
        
        self.editor._modified = True
        return True
    
    def undo(self) -> bool:
        self.editor._lines = self.old_lines.copy()
        self.editor._modified = True
        return True
    
    def redo(self) -> bool:
        return self.execute()


class DeleteCommand(Command):
    """Command to delete characters."""
    
    def __init__(self, editor: TextEditor, line: int, col: int, length: int):
        self.editor = editor
        self.line = line
        self.col = col
        self.length = length
        self.deleted_text = ""
        self.old_line = ""
    
    def execute(self) -> bool:
        line_idx = self.line - 1
        col_idx = self.col - 1
        
        self.old_line = self.editor._lines[line_idx]
        self.deleted_text = self.old_line[col_idx:col_idx + self.length]
        
        new_line = self.old_line[:col_idx] + self.old_line[col_idx + self.length:]
        self.editor._lines[line_idx] = new_line
        self.editor._modified = True
        return True
    
    def undo(self) -> bool:
        line_idx = self.line - 1
        self.editor._lines[line_idx] = self.old_line
        self.editor._modified = True
        return True
    
    def redo(self) -> bool:
        return self.execute()


class ReplaceCommand(Command):
    """Command to replace characters with text."""
    
    def __init__(self, editor: TextEditor, line: int, col: int, length: int, text: str):
        self.editor = editor
        self.line = line
        self.col = col
        self.length = length
        self.text = text
        self.delete_cmd = None
        self.insert_cmd = None
    
    def execute(self) -> bool:
        # First delete, then insert
        self.delete_cmd = DeleteCommand(self.editor, self.line, self.col, self.length)
        if not self.delete_cmd.execute():
            return False
        
        self.insert_cmd = InsertCommand(self.editor, self.line, self.col, self.text)
        if not self.insert_cmd.execute():
            # Rollback delete
            self.delete_cmd.undo()
            return False
        
        return True
    
    def undo(self) -> bool:
        if self.insert_cmd and self.delete_cmd:
            self.insert_cmd.undo()
            self.delete_cmd.undo()
            return True
        return False
    
    def redo(self) -> bool:
        return self.execute()
