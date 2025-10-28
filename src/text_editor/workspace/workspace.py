"""
Workspace module managing multiple editors and session state.
Implements Memento pattern for state persistence.
"""
import os
import json
from typing import Dict, Optional, List
from ..editors.text_editor import TextEditor
from ..utils.observer import Subject, EventType
from ..logging.logger import Logger


class WorkspaceMemento:
    """
    Memento for saving/restoring workspace state.
    Implements the Memento pattern.
    """
    
    def __init__(self, state: dict):
        """
        Initialize memento with state.
        
        Args:
            state: Workspace state dictionary
        """
        self._state = state.copy()
    
    def get_state(self) -> dict:
        """Get saved state."""
        return self._state.copy()


class Workspace(Subject):
    """
    Workspace managing multiple text editors and global state.
    Implements Subject for event notifications.
    """
    
    WORKSPACE_FILE = ".editorWorkspace"
    
    def __init__(self):
        """Initialize workspace."""
        super().__init__()
        self._editors: Dict[str, TextEditor] = {}
        self._active_file: Optional[str] = None
        self._file_order: List[str] = []  # Track order for "most recently used"
        self._logger = Logger(self)
        self.attach(self._logger)
    
    def load_file(self, filepath: str) -> tuple[bool, str]:
        """
        Load a file into the workspace.
        
        Args:
            filepath: Path to the file
            
        Returns:
            Tuple of (success, message)
        """
        # Normalize path
        filepath = os.path.abspath(filepath)
        
        # If already open, just switch to it
        if filepath in self._editors:
            self._active_file = filepath
            self._update_file_order(filepath)
            self.notify(EventType.EDITOR_SWITCHED, {'file': filepath})
            return True, f"Switched to {filepath}"
        
        # Try to load file
        content = ""
        file_exists = os.path.exists(filepath)
        
        if file_exists:
            try:
                with open(filepath, 'r', encoding='utf-8') as f:
                    content = f.read()
            except Exception as e:
                return False, f"Failed to read file: {e}"
        
        # Create editor
        editor = TextEditor(filepath, content)
        
        if not file_exists:
            editor.set_modified(True)  # New file is modified
        
        self._editors[filepath] = editor
        self._active_file = filepath
        self._update_file_order(filepath)
        
        # Check for auto-log
        if content:
            self._logger.check_auto_log(filepath, content)
        
        self.notify(EventType.FILE_LOADED, {'file': filepath})
        
        return True, f"Loaded {filepath}" + (" (new file)" if not file_exists else "")
    
    def save_file(self, filepath: Optional[str] = None) -> tuple[bool, str]:
        """
        Save a file to disk.
        
        Args:
            filepath: File to save (None = active file)
            
        Returns:
            Tuple of (success, message)
        """
        target = filepath if filepath else self._active_file
        
        if not target:
            return False, "No file to save"
        
        if target not in self._editors:
            return False, f"File not open: {target}"
        
        editor = self._editors[target]
        
        try:
            # Ensure directory exists
            directory = os.path.dirname(target)
            if directory and not os.path.exists(directory):
                os.makedirs(directory)
            
            with open(target, 'w', encoding='utf-8') as f:
                f.write(editor.get_content())
            
            editor.set_modified(False)
            self.notify(EventType.FILE_SAVED, {'file': target})
            return True, f"Saved {target}"
        except Exception as e:
            return False, f"Failed to save file: {e}"
    
    def save_all(self) -> tuple[bool, str]:
        """
        Save all open files.
        
        Returns:
            Tuple of (success, message)
        """
        saved_count = 0
        failed = []
        
        for filepath in self._editors:
            success, msg = self.save_file(filepath)
            if success:
                saved_count += 1
            else:
                failed.append(filepath)
        
        if failed:
            return False, f"Saved {saved_count} files, failed: {', '.join(failed)}"
        else:
            return True, f"Saved {saved_count} file(s)"
    
    def init_file(self, filepath: str, with_log: bool = False) -> tuple[bool, str]:
        """
        Create a new buffer file.
        
        Args:
            filepath: Path for the new file
            with_log: Whether to add #log as first line
            
        Returns:
            Tuple of (success, message)
        """
        filepath = os.path.abspath(filepath)
        
        # Check if file already exists
        if os.path.exists(filepath):
            return False, f"File already exists: {filepath}"
        
        # Check if already open
        if filepath in self._editors:
            return False, f"File already open: {filepath}"
        
        # Create editor with initial content
        content = "# log" if with_log else ""
        editor = TextEditor(filepath, content)
        editor.set_modified(True)
        
        self._editors[filepath] = editor
        self._active_file = filepath
        self._update_file_order(filepath)
        
        # Enable logging if with_log
        if with_log:
            self._logger.enable_logging(filepath)
        
        return True, f"Created new buffer: {filepath}"
    
    def close_file(self, filepath: Optional[str] = None, force: bool = False) -> tuple[bool, str, bool]:
        """
        Close a file.
        
        Args:
            filepath: File to close (None = active file)
            force: Force close without save prompt
            
        Returns:
            Tuple of (success, message, needs_save_prompt)
        """
        target = filepath if filepath else self._active_file
        
        if not target:
            return False, "No file to close", False
        
        if target not in self._editors:
            return False, f"File not open: {target}", False
        
        editor = self._editors[target]
        
        # Check if modified and needs save
        if editor.is_modified() and not force:
            return False, f"File modified: {target}", True
        
        # Remove editor
        del self._editors[target]
        
        # Remove from file order
        if target in self._file_order:
            self._file_order.remove(target)
        
        # Update active file
        if self._active_file == target:
            if self._file_order:
                self._active_file = self._file_order[-1]  # Most recent
            else:
                self._active_file = None
        
        self.notify(EventType.FILE_CLOSED, {'file': target})
        return True, f"Closed {target}", False
    
    def switch_editor(self, filepath: str) -> tuple[bool, str]:
        """
        Switch to a different editor.
        
        Args:
            filepath: File to switch to
            
        Returns:
            Tuple of (success, message)
        """
        filepath = os.path.abspath(filepath)
        
        if filepath not in self._editors:
            return False, f"文件未打开：{filepath}"
        
        self._active_file = filepath
        self._update_file_order(filepath)
        self.notify(EventType.EDITOR_SWITCHED, {'file': filepath})
        return True, f"Switched to {filepath}"
    
    def get_active_editor(self) -> Optional[TextEditor]:
        """Get the currently active editor."""
        if self._active_file and self._active_file in self._editors:
            return self._editors[self._active_file]
        return None
    
    def get_active_file(self) -> Optional[str]:
        """Get the currently active file path."""
        return self._active_file
    
    def get_editor(self, filepath: str) -> Optional[TextEditor]:
        """Get editor for a specific file."""
        return self._editors.get(filepath)
    
    def get_open_files(self) -> List[str]:
        """Get list of all open files."""
        return list(self._editors.keys())
    
    def get_logger(self) -> Logger:
        """Get the logger instance."""
        return self._logger
    
    def _update_file_order(self, filepath: str):
        """Update file order for most recently used tracking."""
        if filepath in self._file_order:
            self._file_order.remove(filepath)
        self._file_order.append(filepath)
    
    def create_memento(self) -> WorkspaceMemento:
        """
        Create a memento of current workspace state.
        
        Returns:
            WorkspaceMemento: Saved state
        """
        state = {
            'active_file': self._active_file,
            'files': {},
            'log_enabled': list(self._logger._log_enabled_files)
        }
        
        for filepath, editor in self._editors.items():
            state['files'][filepath] = {
                'modified': editor.is_modified()
            }
        
        return WorkspaceMemento(state)
    
    def restore_from_memento(self, memento: WorkspaceMemento) -> bool:
        """
        Restore workspace state from memento.
        
        Args:
            memento: Saved state
            
        Returns:
            bool: True if successful
        """
        try:
            state = memento.get_state()
            
            # Restore open files
            for filepath in state.get('files', {}).keys():
                if os.path.exists(filepath):
                    self.load_file(filepath)
            
            # Restore active file
            active_file = state.get('active_file')
            if active_file and active_file in self._editors:
                self._active_file = active_file
            
            # Restore logging state
            for filepath in state.get('log_enabled', []):
                if filepath in self._editors:
                    self._logger.enable_logging(filepath)
            
            return True
        except Exception as e:
            print(f"Warning: Failed to restore workspace: {e}")
            return False
    
    def save_workspace_state(self) -> bool:
        """
        Save workspace state to file.
        
        Returns:
            bool: True if successful
        """
        try:
            memento = self.create_memento()
            state = memento.get_state()
            
            with open(self.WORKSPACE_FILE, 'w', encoding='utf-8') as f:
                json.dump(state, f, indent=2)
            
            return True
        except Exception as e:
            print(f"Warning: Failed to save workspace state: {e}")
            return False
    
    def load_workspace_state(self) -> bool:
        """
        Load workspace state from file.
        
        Returns:
            bool: True if successful
        """
        if not os.path.exists(self.WORKSPACE_FILE):
            return False
        
        try:
            with open(self.WORKSPACE_FILE, 'r', encoding='utf-8') as f:
                state = json.load(f)
            
            memento = WorkspaceMemento(state)
            return self.restore_from_memento(memento)
        except Exception as e:
            print(f"Warning: Failed to load workspace state: {e}")
            return False
    
    def notify_command(self, command: str):
        """
        Notify observers that a command was executed.
        
        Args:
            command: Command string
        """
        if self._active_file:
            self.notify(EventType.COMMAND_EXECUTED, {
                'command': command,
                'file': self._active_file
            })
