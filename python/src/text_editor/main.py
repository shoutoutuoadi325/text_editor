"""
Main entry point for the text editor application.
"""
from .workspace.workspace import Workspace
from .command_processor import CommandProcessor


def main():
    """Main application loop."""
    print("Text Editor v1.0")
    print("Type 'exit' to quit")
    print()
    
    # Create workspace
    workspace = Workspace()
    
    # Try to restore previous session
    if workspace.load_workspace_state():
        print("Workspace restored from previous session")
    
    # Create command processor
    processor = CommandProcessor(workspace)
    
    # Main loop
    while processor.running:
        try:
            # Show prompt with active file
            active_file = workspace.get_active_file()
            if active_file:
                prompt = f"[{active_file}]> "
            else:
                prompt = "> "
            
            command = input(prompt)
            
            # Process command
            result = processor.process_command(command)
            
            if result:
                print(result)
        
        except KeyboardInterrupt:
            print("\nUse 'exit' to quit")
        except EOFError:
            break
        except Exception as e:
            print(f"Error: {e}")
    
    print("Editor closed")


if __name__ == "__main__":
    main()
