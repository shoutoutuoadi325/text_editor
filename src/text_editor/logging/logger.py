"""
Logger module implementing Observer pattern for command logging.
"""
import os
from datetime import datetime
from typing import Dict, Any
from ..utils.observer import Observer, EventType


class Logger(Observer):
    """
    Logger that observes workspace events and records them to log files.
    Implements the Observer pattern.
    """
    
    def __init__(self, workspace=None):
        """
        Initialize logger.
        
        Args:
            workspace: Workspace instance (for accessing file information)
        """
        self.workspace = workspace
        self._log_enabled_files = set()  # Files with logging enabled
        self._session_started = {}  # Track session start per file
    
    def enable_logging(self, filepath: str) -> bool:
        """
        Enable logging for a file.
        
        Args:
            filepath: Path to the file
            
        Returns:
            bool: True if successful
        """
        self._log_enabled_files.add(filepath)
        
        # Write session start if not already started
        if filepath not in self._session_started:
            log_path = self._get_log_path(filepath)
            try:
                timestamp = datetime.now().strftime("%Y%m%d %H:%M:%S")
                with open(log_path, 'a', encoding='utf-8') as f:
                    f.write(f"session start at {timestamp}\n")
                self._session_started[filepath] = True
                return True
            except Exception as e:
                print(f"Warning: Failed to write session start: {e}")
                return False
        return True
    
    def disable_logging(self, filepath: str):
        """
        Disable logging for a file.
        
        Args:
            filepath: Path to the file
        """
        if filepath in self._log_enabled_files:
            self._log_enabled_files.remove(filepath)
    
    def is_logging_enabled(self, filepath: str) -> bool:
        """Check if logging is enabled for a file."""
        return filepath in self._log_enabled_files
    
    def update(self, event_type: EventType, data: Dict[str, Any]):
        """
        Observer update method - called when events occur.
        
        Args:
            event_type: Type of event
            data: Event data
        """
        if event_type == EventType.COMMAND_EXECUTED:
            self._log_command(data)
    
    def _log_command(self, data: Dict[str, Any]):
        """
        Log a command execution.
        
        Args:
            data: Command data containing 'command' and 'file' keys
        """
        command = data.get('command', '')
        filepath = data.get('file', '')
        
        if not filepath or not command:
            return
        
        if filepath not in self._log_enabled_files:
            return
        
        log_path = self._get_log_path(filepath)
        try:
            timestamp = datetime.now().strftime("%Y%m%d %H:%M:%S")
            log_entry = f"{timestamp} {command}\n"
            
            with open(log_path, 'a', encoding='utf-8') as f:
                f.write(log_entry)
        except Exception as e:
            print(f"Warning: Failed to write log: {e}")
    
    def _get_log_path(self, filepath: str) -> str:
        """
        Get log file path for a given file.
        
        Args:
            filepath: Original file path
            
        Returns:
            str: Log file path
        """
        directory = os.path.dirname(filepath)
        filename = os.path.basename(filepath)
        log_filename = f".{filename}.log"
        
        if directory:
            return os.path.join(directory, log_filename)
        else:
            return log_filename
    
    def show_log(self, filepath: str) -> str:
        """
        Get log content for a file.
        
        Args:
            filepath: Path to the file
            
        Returns:
            str: Log content or error message
        """
        log_path = self._get_log_path(filepath)
        
        if not os.path.exists(log_path):
            return f"No log file found for {filepath}"
        
        try:
            with open(log_path, 'r', encoding='utf-8') as f:
                return f.read()
        except Exception as e:
            return f"Error reading log file: {e}"
    
    def check_auto_log(self, filepath: str, content: str) -> bool:
        """
        Check if file should auto-enable logging based on first line.
        
        Args:
            filepath: Path to the file
            content: File content
            
        Returns:
            bool: True if logging was auto-enabled
        """
        if content.startswith("#log") or content.startswith("# log"):
            self.enable_logging(filepath)
            return True
        return False
