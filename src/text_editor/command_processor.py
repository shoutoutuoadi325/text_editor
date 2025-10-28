"""
Command processor for parsing and executing user commands.
"""
import os
import re
from typing import Optional, Tuple
from .workspace.workspace import Workspace


class CommandProcessor:
    """Processes user commands and executes them on the workspace."""
    
    def __init__(self, workspace: Workspace):
        """
        Initialize command processor.
        
        Args:
            workspace: Workspace instance
        """
        self.workspace = workspace
        self.running = True
    
    def process_command(self, command_line: str) -> str:
        """
        Process a command line.
        
        Args:
            command_line: Command string from user
            
        Returns:
            str: Result message
        """
        command_line = command_line.strip()
        if not command_line:
            return ""
        
        # Parse command and arguments
        parts = self._parse_command(command_line)
        if not parts:
            return "Invalid command"
        
        cmd = parts[0]
        args = parts[1:]
        
        # Route to appropriate handler
        if cmd == "load":
            return self._cmd_load(args)
        elif cmd == "save":
            return self._cmd_save(args)
        elif cmd == "init":
            return self._cmd_init(args)
        elif cmd == "close":
            return self._cmd_close(args)
        elif cmd == "edit":
            return self._cmd_edit(args)
        elif cmd == "editor-list":
            return self._cmd_editor_list(args)
        elif cmd == "dir-tree":
            return self._cmd_dir_tree(args)
        elif cmd == "undo":
            return self._cmd_undo(args)
        elif cmd == "redo":
            return self._cmd_redo(args)
        elif cmd == "exit":
            return self._cmd_exit(args)
        elif cmd == "append":
            return self._cmd_append(args, command_line)
        elif cmd == "insert":
            return self._cmd_insert(args, command_line)
        elif cmd == "delete":
            return self._cmd_delete(args, command_line)
        elif cmd == "replace":
            return self._cmd_replace(args, command_line)
        elif cmd == "show":
            return self._cmd_show(args)
        elif cmd == "log-on":
            return self._cmd_log_on(args)
        elif cmd == "log-off":
            return self._cmd_log_off(args)
        elif cmd == "log-show":
            return self._cmd_log_show(args)
        else:
            return f"Unknown command: {cmd}"
    
    def _parse_command(self, command_line: str) -> list:
        """Parse command line into parts, handling quoted strings."""
        parts = []
        current = []
        in_quotes = False
        
        i = 0
        while i < len(command_line):
            char = command_line[i]
            
            if char == '"':
                in_quotes = not in_quotes
            elif char == ' ' and not in_quotes:
                if current:
                    parts.append(''.join(current))
                    current = []
            else:
                current.append(char)
            
            i += 1
        
        if current:
            parts.append(''.join(current))
        
        return parts
    
    def _get_active_editor(self):
        """Get active editor or return error message."""
        editor = self.workspace.get_active_editor()
        if not editor:
            return None, "No active file"
        return editor, None
    
    # Workspace commands
    
    def _cmd_load(self, args: list) -> str:
        """Load a file."""
        if len(args) < 1:
            return "Usage: load <file>"
        
        filepath = args[0]
        success, msg = self.workspace.load_file(filepath)
        
        if success:
            self.workspace.notify_command(f"load {filepath}")
        
        return msg
    
    def _cmd_save(self, args: list) -> str:
        """Save file(s)."""
        if len(args) == 0:
            # Save active file
            success, msg = self.workspace.save_file()
            if success:
                self.workspace.notify_command("save")
            return msg
        elif args[0] == "all":
            # Save all files
            success, msg = self.workspace.save_all()
            if success:
                self.workspace.notify_command("save all")
            return msg
        else:
            # Save specific file
            filepath = args[0]
            success, msg = self.workspace.save_file(filepath)
            if success:
                self.workspace.notify_command(f"save {filepath}")
            return msg
    
    def _cmd_init(self, args: list) -> str:
        """Initialize a new buffer."""
        if len(args) < 1:
            return "Usage: init <file> [with-log]"
        
        filepath = args[0]
        with_log = len(args) > 1 and args[1] == "with-log"
        
        success, msg = self.workspace.init_file(filepath, with_log)
        
        if success:
            cmd_str = f"init {filepath}" + (" with-log" if with_log else "")
            self.workspace.notify_command(cmd_str)
        
        return msg
    
    def _cmd_close(self, args: list) -> str:
        """Close a file."""
        filepath = args[0] if args else None
        
        success, msg, needs_save = self.workspace.close_file(filepath)
        
        if needs_save:
            # Prompt user
            print(f"{msg} 文件已修改，是否保存？(y/n)")
            response = input().strip().lower()
            
            if response == 'y':
                # Save then close
                target = filepath if filepath else self.workspace.get_active_file()
                self.workspace.save_file(target)
                success, msg, _ = self.workspace.close_file(filepath, force=True)
            elif response == 'n':
                # Close without saving
                success, msg, _ = self.workspace.close_file(filepath, force=True)
            else:
                return "Close cancelled"
        
        if success:
            cmd_str = f"close {filepath}" if filepath else "close"
            self.workspace.notify_command(cmd_str)
        
        return msg
    
    def _cmd_edit(self, args: list) -> str:
        """Switch to a different file."""
        if len(args) < 1:
            return "Usage: edit <file>"
        
        filepath = args[0]
        success, msg = self.workspace.switch_editor(filepath)
        
        if success:
            self.workspace.notify_command(f"edit {filepath}")
        
        return msg
    
    def _cmd_editor_list(self, args: list) -> str:
        """Display list of open editors."""
        open_files = self.workspace.get_open_files()
        
        if not open_files:
            return "No files open"
        
        active_file = self.workspace.get_active_file()
        result = []
        
        for filepath in open_files:
            editor = self.workspace.get_editor(filepath)
            is_active = filepath == active_file
            is_modified = editor.is_modified()
            
            # Format: >file.txt* for active+modified, file.txt for normal
            prefix = ">" if is_active else " "
            suffix = "*" if is_modified else ""
            result.append(f"{prefix}{filepath}{suffix}")
        
        return '\n'.join(result)
    
    def _cmd_dir_tree(self, args: list) -> str:
        """Display directory tree."""
        path = args[0] if args else "."
        
        if not os.path.exists(path):
            return f"Path not found: {path}"
        
        if not os.path.isdir(path):
            return f"Not a directory: {path}"
        
        return self._build_tree(path, "")
    
    def _build_tree(self, path: str, prefix: str = "", is_last: bool = True) -> str:
        """Recursively build directory tree."""
        result = []
        
        try:
            entries = sorted(os.listdir(path))
            # Filter out hidden files and common ignore patterns
            entries = [e for e in entries if not e.startswith('.') and e != '__pycache__']
            
            for i, entry in enumerate(entries):
                is_last_entry = i == len(entries) - 1
                entry_path = os.path.join(path, entry)
                
                # Add entry
                result.append(f"{prefix}{entry}")
                
                # Recurse for directories
                if os.path.isdir(entry_path):
                    new_prefix = prefix + "  "
                    subtree = self._build_tree(entry_path, new_prefix, is_last_entry)
                    if subtree:
                        result.append(subtree)
        except PermissionError:
            result.append(f"{prefix}[Permission Denied]")
        
        return '\n'.join(result)
    
    def _cmd_undo(self, args: list) -> str:
        """Undo last operation."""
        editor, err = self._get_active_editor()
        if err:
            return err
        
        if editor.undo():
            self.workspace.notify_command("undo")
            return "Undo successful"
        else:
            return "Nothing to undo"
    
    def _cmd_redo(self, args: list) -> str:
        """Redo last undone operation."""
        editor, err = self._get_active_editor()
        if err:
            return err
        
        if editor.redo():
            self.workspace.notify_command("redo")
            return "Redo successful"
        else:
            return "Nothing to redo"
    
    def _cmd_exit(self, args: list) -> str:
        """Exit the editor."""
        # Check for unsaved files
        unsaved = []
        for filepath in self.workspace.get_open_files():
            editor = self.workspace.get_editor(filepath)
            if editor.is_modified():
                unsaved.append(filepath)
        
        # Prompt for each unsaved file
        for filepath in unsaved:
            print(f"Save {filepath}? (y/n)")
            response = input().strip().lower()
            
            if response == 'y':
                self.workspace.save_file(filepath)
        
        # Save workspace state
        self.workspace.save_workspace_state()
        
        self.running = False
        return "Goodbye!"
    
    # Text editing commands
    
    def _cmd_append(self, args: list, full_command: str) -> str:
        """Append text to end of file."""
        editor, err = self._get_active_editor()
        if err:
            return err
        
        # Extract quoted text
        text = self._extract_quoted_text(full_command)
        if text is None:
            return "Usage: append \"text\""
        
        if editor.append(text):
            self.workspace.notify_command(full_command)
            return "Text appended"
        else:
            return "Append failed"
    
    def _cmd_insert(self, args: list, full_command: str) -> str:
        """Insert text at position."""
        editor, err = self._get_active_editor()
        if err:
            return err
        
        if len(args) < 2:
            return "Usage: insert <line:col> \"text\""
        
        # Parse line:col
        pos = args[0].split(':')
        if len(pos) != 2:
            return "Invalid position format. Use line:col"
        
        try:
            line = int(pos[0])
            col = int(pos[1])
        except ValueError:
            return "Invalid line or column number"
        
        # Extract quoted text
        text = self._extract_quoted_text(full_command)
        if text is None:
            return "Usage: insert <line:col> \"text\""
        
        success, msg = editor.insert(line, col, text)
        
        if success:
            self.workspace.notify_command(full_command)
            return "Text inserted"
        else:
            return msg
    
    def _cmd_delete(self, args: list, full_command: str) -> str:
        """Delete characters."""
        editor, err = self._get_active_editor()
        if err:
            return err
        
        if len(args) < 2:
            return "Usage: delete <line:col> <len>"
        
        # Parse line:col
        pos = args[0].split(':')
        if len(pos) != 2:
            return "Invalid position format. Use line:col"
        
        try:
            line = int(pos[0])
            col = int(pos[1])
            length = int(args[1])
        except ValueError:
            return "Invalid parameters"
        
        success, msg = editor.delete(line, col, length)
        
        if success:
            self.workspace.notify_command(full_command)
            return "Text deleted"
        else:
            return msg
    
    def _cmd_replace(self, args: list, full_command: str) -> str:
        """Replace characters with text."""
        editor, err = self._get_active_editor()
        if err:
            return err
        
        if len(args) < 3:
            return "Usage: replace <line:col> <len> \"text\""
        
        # Parse line:col
        pos = args[0].split(':')
        if len(pos) != 2:
            return "Invalid position format. Use line:col"
        
        try:
            line = int(pos[0])
            col = int(pos[1])
            length = int(args[1])
        except ValueError:
            return "Invalid parameters"
        
        # Extract quoted text
        text = self._extract_quoted_text(full_command)
        if text is None:
            return "Usage: replace <line:col> <len> \"text\""
        
        success, msg = editor.replace(line, col, length, text)
        
        if success:
            self.workspace.notify_command(full_command)
            return "Text replaced"
        else:
            return msg
    
    def _cmd_show(self, args: list) -> str:
        """Show file content."""
        editor, err = self._get_active_editor()
        if err:
            return err
        
        if len(args) == 0:
            # Show all
            return editor.show()
        else:
            # Parse range
            range_parts = args[0].split(':')
            if len(range_parts) != 2:
                return "Invalid range format. Use start:end"
            
            try:
                start = int(range_parts[0])
                end = int(range_parts[1])
            except ValueError:
                return "Invalid line numbers"
            
            return editor.show(start, end)
    
    # Logging commands
    
    def _cmd_log_on(self, args: list) -> str:
        """Enable logging."""
        if args:
            filepath = args[0]
            editor = self.workspace.get_editor(filepath)
            if not editor:
                return f"File not open: {filepath}"
        else:
            filepath = self.workspace.get_active_file()
            if not filepath:
                return "No active file"
        
        logger = self.workspace.get_logger()
        logger.enable_logging(filepath)
        
        cmd_str = f"log-on {filepath}" if args else "log-on"
        self.workspace.notify_command(cmd_str)
        
        return f"Logging enabled for {filepath}"
    
    def _cmd_log_off(self, args: list) -> str:
        """Disable logging."""
        if args:
            filepath = args[0]
        else:
            filepath = self.workspace.get_active_file()
            if not filepath:
                return "No active file"
        
        logger = self.workspace.get_logger()
        logger.disable_logging(filepath)
        
        cmd_str = f"log-off {filepath}" if args else "log-off"
        self.workspace.notify_command(cmd_str)
        
        return f"Logging disabled for {filepath}"
    
    def _cmd_log_show(self, args: list) -> str:
        """Show log content."""
        if args:
            filepath = args[0]
        else:
            filepath = self.workspace.get_active_file()
            if not filepath:
                return "No active file"
        
        logger = self.workspace.get_logger()
        return logger.show_log(filepath)
    
    def _extract_quoted_text(self, command_line: str) -> Optional[str]:
        """Extract text from within quotes."""
        match = re.search(r'"([^"]*)"', command_line)
        if match:
            return match.group(1)
        return None
