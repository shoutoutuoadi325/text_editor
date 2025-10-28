"""
Observer pattern implementation for event notification system.
"""
from abc import ABC, abstractmethod
from typing import Any, Dict, List
from enum import Enum


class EventType(Enum):
    """Types of events that can be published."""
    COMMAND_EXECUTED = "command_executed"
    FILE_LOADED = "file_loaded"
    FILE_SAVED = "file_saved"
    FILE_CLOSED = "file_closed"
    FILE_MODIFIED = "file_modified"
    EDITOR_SWITCHED = "editor_switched"


class Observer(ABC):
    """Abstract observer interface."""
    
    @abstractmethod
    def update(self, event_type: EventType, data: Dict[str, Any]):
        """
        Called when an observed event occurs.
        
        Args:
            event_type: Type of event
            data: Event data
        """
        pass


class Subject:
    """Subject that can be observed."""
    
    def __init__(self):
        self._observers: List[Observer] = []
    
    def attach(self, observer: Observer):
        """Attach an observer."""
        if observer not in self._observers:
            self._observers.append(observer)
    
    def detach(self, observer: Observer):
        """Detach an observer."""
        if observer in self._observers:
            self._observers.remove(observer)
    
    def notify(self, event_type: EventType, data: Dict[str, Any] = None):
        """
        Notify all observers of an event.
        
        Args:
            event_type: Type of event
            data: Event data (optional)
        """
        if data is None:
            data = {}
        
        for observer in self._observers:
            try:
                observer.update(event_type, data)
            except Exception as e:
                # Don't let observer errors break the subject
                print(f"Warning: Observer error: {e}")
