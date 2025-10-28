"""
Unit tests for Command pattern implementation.
"""
import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '../../src'))

import pytest
from text_editor.commands.command import Command, CommandHistory


class MockCommand(Command):
    """Mock command for testing."""
    
    def __init__(self, execute_result=True, undo_result=True):
        self.execute_result = execute_result
        self.undo_result = undo_result
        self.execute_count = 0
        self.undo_count = 0
        self.redo_count = 0
    
    def execute(self):
        self.execute_count += 1
        return self.execute_result
    
    def undo(self):
        self.undo_count += 1
        return self.undo_result
    
    def redo(self):
        self.redo_count += 1
        return self.execute()


class TestCommand:
    """Test Command base class."""
    
    def test_mock_command_execute(self):
        """Test command execution."""
        cmd = MockCommand()
        assert cmd.execute() == True
        assert cmd.execute_count == 1
    
    def test_mock_command_undo(self):
        """Test command undo."""
        cmd = MockCommand()
        assert cmd.undo() == True
        assert cmd.undo_count == 1


class TestCommandHistory:
    """Test CommandHistory class."""
    
    def test_execute_command(self):
        """Test executing a command through history."""
        history = CommandHistory()
        cmd = MockCommand()
        
        result = history.execute_command(cmd)
        
        assert result == True
        assert cmd.execute_count == 1
        assert history.can_undo() == True
        assert history.can_redo() == False
    
    def test_execute_command_failure(self):
        """Test executing a failing command."""
        history = CommandHistory()
        cmd = MockCommand(execute_result=False)
        
        result = history.execute_command(cmd)
        
        assert result == False
        assert history.can_undo() == False
    
    def test_undo(self):
        """Test undo operation."""
        history = CommandHistory()
        cmd = MockCommand()
        
        history.execute_command(cmd)
        undone_cmd = history.undo()
        
        assert undone_cmd is cmd
        assert cmd.undo_count == 1
        assert history.can_undo() == False
        assert history.can_redo() == True
    
    def test_undo_empty(self):
        """Test undo with no history."""
        history = CommandHistory()
        
        result = history.undo()
        
        assert result is None
    
    def test_redo(self):
        """Test redo operation."""
        history = CommandHistory()
        cmd = MockCommand()
        
        history.execute_command(cmd)
        history.undo()
        redone_cmd = history.redo()
        
        assert redone_cmd is cmd
        assert cmd.redo_count == 1
        assert history.can_undo() == True
        assert history.can_redo() == False
    
    def test_redo_empty(self):
        """Test redo with nothing to redo."""
        history = CommandHistory()
        
        result = history.redo()
        
        assert result is None
    
    def test_new_command_clears_redo(self):
        """Test that new command clears redo stack."""
        history = CommandHistory()
        cmd1 = MockCommand()
        cmd2 = MockCommand()
        
        history.execute_command(cmd1)
        history.undo()
        history.execute_command(cmd2)
        
        assert history.can_redo() == False
    
    def test_multiple_undo_redo(self):
        """Test multiple undo and redo operations."""
        history = CommandHistory()
        cmd1 = MockCommand()
        cmd2 = MockCommand()
        cmd3 = MockCommand()
        
        history.execute_command(cmd1)
        history.execute_command(cmd2)
        history.execute_command(cmd3)
        
        # Undo all
        history.undo()
        history.undo()
        history.undo()
        
        assert history.can_undo() == False
        assert history.can_redo() == True
        
        # Redo all
        history.redo()
        history.redo()
        history.redo()
        
        assert history.can_undo() == True
        assert history.can_redo() == False
    
    def test_clear(self):
        """Test clearing history."""
        history = CommandHistory()
        cmd = MockCommand()
        
        history.execute_command(cmd)
        history.clear()
        
        assert history.can_undo() == False
        assert history.can_redo() == False


if __name__ == '__main__':
    pytest.main([__file__, '-v'])
