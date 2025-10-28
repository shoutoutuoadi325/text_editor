"""
Command pattern implementation for undo/redo functionality.
"""
from abc import ABC, abstractmethod
from typing import Optional


class Command(ABC):
    """Abstract base class for all commands that can be undone/redone."""
    
    @abstractmethod
    def execute(self) -> bool:
        """
        Execute the command.
        
        Returns:
            bool: True if execution succeeded, False otherwise
        """
        pass
    
    @abstractmethod
    def undo(self) -> bool:
        """
        Undo the command.
        
        Returns:
            bool: True if undo succeeded, False otherwise
        """
        pass
    
    @abstractmethod
    def redo(self) -> bool:
        """
        Redo the command (by default, same as execute).
        
        Returns:
            bool: True if redo succeeded, False otherwise
        """
        pass


class CommandHistory:
    """Manages command history for undo/redo operations."""
    
    def __init__(self):
        self._undo_stack = []
        self._redo_stack = []
    
    def execute_command(self, command: Command) -> bool:
        """
        Execute a command and add it to history.
        
        Args:
            command: Command to execute
            
        Returns:
            bool: True if execution succeeded
        """
        if command.execute():
            self._undo_stack.append(command)
            self._redo_stack.clear()  # Clear redo stack on new command
            return True
        return False
    
    def undo(self) -> Optional[Command]:
        """
        Undo the last command.
        
        Returns:
            The command that was undone, or None if nothing to undo
        """
        if not self._undo_stack:
            return None
        
        command = self._undo_stack.pop()
        if command.undo():
            self._redo_stack.append(command)
            return command
        else:
            # If undo fails, put it back
            self._undo_stack.append(command)
            return None
    
    def redo(self) -> Optional[Command]:
        """
        Redo the last undone command.
        
        Returns:
            The command that was redone, or None if nothing to redo
        """
        if not self._redo_stack:
            return None
        
        command = self._redo_stack.pop()
        if command.redo():
            self._undo_stack.append(command)
            return command
        else:
            # If redo fails, put it back
            self._redo_stack.append(command)
            return None
    
    def can_undo(self) -> bool:
        """Check if there are commands to undo."""
        return len(self._undo_stack) > 0
    
    def can_redo(self) -> bool:
        """Check if there are commands to redo."""
        return len(self._redo_stack) > 0
    
    def clear(self):
        """Clear all command history."""
        self._undo_stack.clear()
        self._redo_stack.clear()
