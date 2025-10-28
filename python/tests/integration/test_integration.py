"""
Integration tests for the text editor system.
"""
import sys
import os
import tempfile
import shutil
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '../../src'))

import pytest
from text_editor.workspace.workspace import Workspace
from text_editor.command_processor import CommandProcessor


class TestIntegration:
    """Integration tests for the text editor."""
    
    @pytest.fixture
    def temp_dir(self):
        """Create temporary directory for tests."""
        temp = tempfile.mkdtemp()
        original_dir = os.getcwd()
        os.chdir(temp)
        yield temp
        os.chdir(original_dir)
        shutil.rmtree(temp)
    
    @pytest.fixture
    def system(self, temp_dir):
        """Create workspace and command processor."""
        workspace = Workspace()
        processor = CommandProcessor(workspace)
        return workspace, processor
    
    def test_load_and_edit_file(self, system, temp_dir):
        """Test loading a file and performing edits."""
        workspace, processor = system
        filepath = os.path.join(temp_dir, "test.txt")
        
        # Load file
        result = processor.process_command(f"load {filepath}")
        assert "Loaded" in result
        
        # Append text
        result = processor.process_command('append "Line 1"')
        assert "appended" in result
        
        # Show content
        result = processor.process_command("show")
        assert "1: Line 1" in result
        
        # Save file
        result = processor.process_command("save")
        assert "Saved" in result
        
        # Verify file was saved
        assert os.path.exists(filepath)
    
    def test_undo_redo_workflow(self, system, temp_dir):
        """Test undo and redo operations."""
        workspace, processor = system
        filepath = os.path.join(temp_dir, "test.txt")
        
        processor.process_command(f"load {filepath}")
        processor.process_command('append "Line 1"')
        processor.process_command('append "Line 2"')
        
        # Undo
        result = processor.process_command("undo")
        assert "successful" in result
        
        result = processor.process_command("show")
        assert "Line 1" in result
        assert "Line 2" not in result
        
        # Redo
        result = processor.process_command("redo")
        assert "successful" in result
        
        result = processor.process_command("show")
        assert "Line 2" in result
    
    def test_multiple_files(self, system, temp_dir):
        """Test working with multiple files."""
        workspace, processor = system
        file1 = os.path.join(temp_dir, "test1.txt")
        file2 = os.path.join(temp_dir, "test2.txt")
        
        # Load both files
        processor.process_command(f"load {file1}")
        processor.process_command('append "File 1 content"')
        
        processor.process_command(f"load {file2}")
        processor.process_command('append "File 2 content"')
        
        # List editors
        result = processor.process_command("editor-list")
        assert "test1.txt" in result
        assert "test2.txt" in result
        
        # Switch to file1
        processor.process_command(f"edit {file1}")
        result = processor.process_command("show")
        assert "File 1 content" in result
    
    def test_logging_workflow(self, system, temp_dir):
        """Test logging functionality."""
        workspace, processor = system
        filepath = os.path.join(temp_dir, "test.txt")
        
        # Load file
        processor.process_command(f"load {filepath}")
        
        # Enable logging
        processor.process_command("log-on")
        
        # Perform some operations
        processor.process_command('append "Test line"')
        processor.process_command("save")
        
        # Check log
        result = processor.process_command("log-show")
        assert "session start at" in result
        assert "append" in result
        assert "save" in result
    
    def test_init_with_log(self, system, temp_dir):
        """Test init command with log option."""
        workspace, processor = system
        filepath = os.path.join(temp_dir, "newfile.txt")
        
        # Init with log
        result = processor.process_command(f"init {filepath} with-log")
        assert "Created" in result
        
        # Check content
        result = processor.process_command("show")
        assert "# log" in result
        
        # Verify logging is enabled
        logger = workspace.get_logger()
        assert logger.is_logging_enabled(filepath)
    
    def test_text_operations(self, system, temp_dir):
        """Test various text editing operations."""
        workspace, processor = system
        filepath = os.path.join(temp_dir, "test.txt")
        
        processor.process_command(f"load {filepath}")
        processor.process_command('append "Hello world"')
        
        # Insert
        result = processor.process_command('insert 1:7 "beautiful "')
        assert "inserted" in result
        
        result = processor.process_command("show")
        assert "Hello beautiful world" in result
        
        # Delete
        processor.process_command("delete 1:7 10")
        result = processor.process_command("show")
        assert "Hello world" in result
        
        # Replace
        processor.process_command('replace 1:1 5 "Goodbye"')
        result = processor.process_command("show")
        assert "Goodbye world" in result
    
    def test_save_all(self, system, temp_dir):
        """Test save all command."""
        workspace, processor = system
        file1 = os.path.join(temp_dir, "test1.txt")
        file2 = os.path.join(temp_dir, "test2.txt")
        
        processor.process_command(f"load {file1}")
        processor.process_command('append "Content 1"')
        
        processor.process_command(f"load {file2}")
        processor.process_command('append "Content 2"')
        
        result = processor.process_command("save all")
        assert "Saved 2" in result
        
        assert os.path.exists(file1)
        assert os.path.exists(file2)
    
    def test_dir_tree(self, system, temp_dir):
        """Test directory tree command."""
        workspace, processor = system
        
        # Create some files
        os.makedirs(os.path.join(temp_dir, "subdir"))
        open(os.path.join(temp_dir, "file1.txt"), 'w').close()
        open(os.path.join(temp_dir, "subdir", "file2.txt"), 'w').close()
        
        result = processor.process_command("dir-tree")
        assert "file1.txt" in result
        assert "subdir" in result


if __name__ == '__main__':
    pytest.main([__file__, '-v'])
