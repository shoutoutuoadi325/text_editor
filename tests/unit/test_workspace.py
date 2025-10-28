"""
Unit tests for Workspace class.
"""
import sys
import os
import tempfile
import shutil
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '../../src'))

import pytest
from text_editor.workspace.workspace import Workspace, WorkspaceMemento


class TestWorkspace:
    """Test Workspace class."""
    
    @pytest.fixture
    def temp_dir(self):
        """Create temporary directory for tests."""
        temp = tempfile.mkdtemp()
        yield temp
        shutil.rmtree(temp)
    
    @pytest.fixture
    def workspace(self, temp_dir):
        """Create workspace instance."""
        os.chdir(temp_dir)
        return Workspace()
    
    def test_init(self, workspace):
        """Test workspace initialization."""
        assert workspace.get_active_file() is None
        assert workspace.get_open_files() == []
    
    def test_load_new_file(self, workspace, temp_dir):
        """Test loading a new file."""
        filepath = os.path.join(temp_dir, "test.txt")
        
        success, msg = workspace.load_file(filepath)
        
        assert success == True
        assert workspace.get_active_file() == filepath
        assert filepath in workspace.get_open_files()
    
    def test_load_existing_file(self, workspace, temp_dir):
        """Test loading an existing file."""
        filepath = os.path.join(temp_dir, "test.txt")
        
        # Create file
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write("Test content")
        
        success, msg = workspace.load_file(filepath)
        
        assert success == True
        editor = workspace.get_active_editor()
        assert editor.get_content() == "Test content"
    
    def test_load_already_open_file(self, workspace, temp_dir):
        """Test loading a file that's already open."""
        filepath = os.path.join(temp_dir, "test.txt")
        
        workspace.load_file(filepath)
        success, msg = workspace.load_file(filepath)
        
        assert success == True
        assert "Switched" in msg
    
    def test_save_file(self, workspace, temp_dir):
        """Test saving a file."""
        filepath = os.path.join(temp_dir, "test.txt")
        
        workspace.load_file(filepath)
        editor = workspace.get_active_editor()
        editor.append("Test content")
        
        success, msg = workspace.save_file()
        
        assert success == True
        assert os.path.exists(filepath)
        
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
        assert "Test content" in content
    
    def test_save_all(self, workspace, temp_dir):
        """Test saving all files."""
        file1 = os.path.join(temp_dir, "test1.txt")
        file2 = os.path.join(temp_dir, "test2.txt")
        
        workspace.load_file(file1)
        workspace.get_active_editor().append("Content 1")
        
        workspace.load_file(file2)
        workspace.get_active_editor().append("Content 2")
        
        success, msg = workspace.save_all()
        
        assert success == True
        assert os.path.exists(file1)
        assert os.path.exists(file2)
    
    def test_init_file(self, workspace, temp_dir):
        """Test initializing a new file."""
        filepath = os.path.join(temp_dir, "new.txt")
        
        success, msg = workspace.init_file(filepath, with_log=False)
        
        assert success == True
        assert workspace.get_active_file() == filepath
        editor = workspace.get_active_editor()
        assert editor.is_modified() == True
    
    def test_init_file_with_log(self, workspace, temp_dir):
        """Test initializing a new file with log."""
        filepath = os.path.join(temp_dir, "new.txt")
        
        success, msg = workspace.init_file(filepath, with_log=True)
        
        assert success == True
        editor = workspace.get_active_editor()
        assert "# log" in editor.get_content()
    
    def test_close_file(self, workspace, temp_dir):
        """Test closing a file."""
        filepath = os.path.join(temp_dir, "test.txt")
        
        workspace.load_file(filepath)
        success, msg, needs_save = workspace.close_file(force=True)
        
        assert success == True
        assert workspace.get_active_file() is None
        assert filepath not in workspace.get_open_files()
    
    def test_close_modified_file(self, workspace, temp_dir):
        """Test closing a modified file."""
        filepath = os.path.join(temp_dir, "test.txt")
        
        workspace.load_file(filepath)
        workspace.get_active_editor().append("New line")
        
        success, msg, needs_save = workspace.close_file()
        
        assert needs_save == True
        assert filepath in workspace.get_open_files()
    
    def test_switch_editor(self, workspace, temp_dir):
        """Test switching between editors."""
        file1 = os.path.join(temp_dir, "test1.txt")
        file2 = os.path.join(temp_dir, "test2.txt")
        
        workspace.load_file(file1)
        workspace.load_file(file2)
        
        success, msg = workspace.switch_editor(file1)
        
        assert success == True
        assert workspace.get_active_file() == file1
    
    def test_switch_to_unopened_file(self, workspace, temp_dir):
        """Test switching to an unopened file."""
        filepath = os.path.join(temp_dir, "test.txt")
        
        success, msg = workspace.switch_editor(filepath)
        
        assert success == False
        assert "文件未打开" in msg
    
    def test_create_memento(self, workspace, temp_dir):
        """Test creating a memento."""
        filepath = os.path.join(temp_dir, "test.txt")
        
        workspace.load_file(filepath)
        memento = workspace.create_memento()
        
        state = memento.get_state()
        assert filepath in state['files']
        assert state['active_file'] == filepath
    
    def test_restore_from_memento(self, workspace, temp_dir):
        """Test restoring from memento."""
        filepath = os.path.join(temp_dir, "test.txt")
        
        # Create and save file
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write("Test")
        
        # Create workspace state
        workspace.load_file(filepath)
        memento = workspace.create_memento()
        
        # Create new workspace and restore
        workspace2 = Workspace()
        os.chdir(temp_dir)
        success = workspace2.restore_from_memento(memento)
        
        assert success == True
        assert workspace2.get_active_file() == filepath


if __name__ == '__main__':
    pytest.main([__file__, '-v'])
