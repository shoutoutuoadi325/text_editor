"""
Unit tests for Observer pattern implementation.
"""
import sys
import os
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '../../src'))

import pytest
from text_editor.utils.observer import Observer, Subject, EventType


class MockObserver(Observer):
    """Mock observer for testing."""
    
    def __init__(self):
        self.events = []
    
    def update(self, event_type, data):
        self.events.append((event_type, data))


class TestObserver:
    """Test Observer pattern."""
    
    def test_attach_observer(self):
        """Test attaching an observer."""
        subject = Subject()
        observer = MockObserver()
        
        subject.attach(observer)
        subject.notify(EventType.COMMAND_EXECUTED, {'test': 'data'})
        
        assert len(observer.events) == 1
        assert observer.events[0][0] == EventType.COMMAND_EXECUTED
        assert observer.events[0][1] == {'test': 'data'}
    
    def test_detach_observer(self):
        """Test detaching an observer."""
        subject = Subject()
        observer = MockObserver()
        
        subject.attach(observer)
        subject.detach(observer)
        subject.notify(EventType.COMMAND_EXECUTED, {})
        
        assert len(observer.events) == 0
    
    def test_multiple_observers(self):
        """Test multiple observers."""
        subject = Subject()
        observer1 = MockObserver()
        observer2 = MockObserver()
        
        subject.attach(observer1)
        subject.attach(observer2)
        subject.notify(EventType.FILE_LOADED, {'file': 'test.txt'})
        
        assert len(observer1.events) == 1
        assert len(observer2.events) == 1
    
    def test_notify_without_data(self):
        """Test notify without explicit data."""
        subject = Subject()
        observer = MockObserver()
        
        subject.attach(observer)
        subject.notify(EventType.FILE_SAVED)
        
        assert len(observer.events) == 1
        assert observer.events[0][1] == {}
    
    def test_observer_error_doesnt_break_notification(self):
        """Test that observer errors don't break other observers."""
        
        class ErrorObserver(Observer):
            def update(self, event_type, data):
                raise Exception("Test error")
        
        subject = Subject()
        error_observer = ErrorObserver()
        good_observer = MockObserver()
        
        subject.attach(error_observer)
        subject.attach(good_observer)
        
        # Should not raise exception
        subject.notify(EventType.COMMAND_EXECUTED, {})
        
        # Good observer should still be notified
        assert len(good_observer.events) == 1


if __name__ == '__main__':
    pytest.main([__file__, '-v'])
