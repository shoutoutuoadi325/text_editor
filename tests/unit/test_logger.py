"""
Unit tests for Logger class.
"""
import sys
import os
import tempfile
import shutil
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '../../src'))

import pytest
from text_editor.logging.logger import Logger
from text_editor.utils.observer import EventType


class TestLogger:
    """Test Logger class."""
    
    @pytest.fixture
    def temp_dir(self):
        """Create temporary directory for tests."""
        temp = tempfile.mkdtemp()
        yield temp
        shutil.rmtree(temp)
    
    @pytest.fixture
    def logger(self, temp_dir):
        """Create logger instance."""
        os.chdir(temp_dir)
        return Logger()
    
    def test_enable_logging(self, logger, temp_dir):
        """Test enabling logging for a file."""
        filepath = os.path.join(temp_dir, "test.txt")
        
        result = logger.enable_logging(filepath)
        
        assert result == True
        assert logger.is_logging_enabled(filepath) == True
    
    def test_disable_logging(self, logger, temp_dir):
        """Test disabling logging for a file."""
        filepath = os.path.join(temp_dir, "test.txt")
        
        logger.enable_logging(filepath)
        logger.disable_logging(filepath)
        
        assert logger.is_logging_enabled(filepath) == False
    
    def test_log_command(self, logger, temp_dir):
        """Test logging a command."""
        filepath = os.path.join(temp_dir, "test.txt")
        
        logger.enable_logging(filepath)
        logger.update(EventType.COMMAND_EXECUTED, {
            'command': 'append "test"',
            'file': filepath
        })
        
        log_path = os.path.join(temp_dir, ".test.txt.log")
        assert os.path.exists(log_path)
        
        with open(log_path, 'r', encoding='utf-8') as f:
            content = f.read()
        
        assert "session start at" in content
        assert 'append "test"' in content
    
    def test_log_not_enabled(self, logger, temp_dir):
        """Test logging when not enabled."""
        filepath = os.path.join(temp_dir, "test.txt")
        
        logger.update(EventType.COMMAND_EXECUTED, {
            'command': 'append "test"',
            'file': filepath
        })
        
        log_path = os.path.join(temp_dir, ".test.txt.log")
        assert not os.path.exists(log_path)
    
    def test_show_log(self, logger, temp_dir):
        """Test showing log content."""
        filepath = os.path.join(temp_dir, "test.txt")
        
        logger.enable_logging(filepath)
        logger.update(EventType.COMMAND_EXECUTED, {
            'command': 'test command',
            'file': filepath
        })
        
        log_content = logger.show_log(filepath)
        
        assert "session start at" in log_content
        assert "test command" in log_content
    
    def test_show_log_not_exists(self, logger, temp_dir):
        """Test showing log for file without log."""
        filepath = os.path.join(temp_dir, "test.txt")
        
        log_content = logger.show_log(filepath)
        
        assert "No log file found" in log_content
    
    def test_check_auto_log_with_hash_log(self, logger, temp_dir):
        """Test auto-enabling log when file starts with #log."""
        filepath = os.path.join(temp_dir, "test.txt")
        content = "#log\nLine 1\nLine 2"
        
        result = logger.check_auto_log(filepath, content)
        
        assert result == True
        assert logger.is_logging_enabled(filepath) == True
    
    def test_check_auto_log_with_hash_space_log(self, logger, temp_dir):
        """Test auto-enabling log when file starts with # log."""
        filepath = os.path.join(temp_dir, "test.txt")
        content = "# log\nLine 1\nLine 2"
        
        result = logger.check_auto_log(filepath, content)
        
        assert result == True
        assert logger.is_logging_enabled(filepath) == True
    
    def test_check_auto_log_without_marker(self, logger, temp_dir):
        """Test auto-log check with file without marker."""
        filepath = os.path.join(temp_dir, "test.txt")
        content = "Line 1\nLine 2"
        
        result = logger.check_auto_log(filepath, content)
        
        assert result == False
        assert logger.is_logging_enabled(filepath) == False


if __name__ == '__main__':
    pytest.main([__file__, '-v'])
