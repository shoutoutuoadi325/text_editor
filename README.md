# 📝 Command-Line Multi-File Text Editor

A powerful command-line based multi-file editor supporting both **plain text** and **XML** file editing, with workspace management, logging, session statistics, and spell checking features.

## 📋 Table of Contents

- [Features](#-features)
- [Requirements](#-requirements)
- [Installation](#-installation)
- [Quick Start](#-quick-start)
- [Command Reference](#-command-reference)
  - [Workspace Commands](#workspace-commands)
  - [Text Editing Commands](#text-editing-commands)
  - [XML Editing Commands](#xml-editing-commands)
  - [Logging Commands](#logging-commands)
  - [Spell Check Command](#spell-check-command)
- [Examples](#-examples)
- [File Formats](#-file-formats)
- [Architecture](#-architecture)
- [Testing](#-testing)

## ✨ Features

### Core Features
- **Multi-file editing**: Open and edit multiple files simultaneously
- **Dual editor support**: 
  - Text Editor for `.txt` files
  - XML Editor for `.xml` files with tree structure visualization
- **Workspace persistence**: Automatically saves and restores your session state
- **Undo/Redo**: Full undo/redo support for all editing operations

### Advanced Features
- **Session Statistics**: Track editing time for each file
- **Spell Checking**: Built-in spell checker for both text and XML content
- **Logging**: Detailed command logging with timestamps
- **Directory Tree**: Visual representation of file system structure

## 📦 Requirements

- **Java**: JDK 11 or higher
- **Maven**: 3.6.0 or higher (for building)

## 🔧 Installation

### 1. Clone or Download the Project

```bash
git clone https://github.com/shoutoutuoadi325/text_editor.git
cd text_editor
```

### 2. Build the Project

```bash
mvn clean package
```

This will compile the source code, run tests, and create an executable JAR file.

### 3. Verify the Build

```bash
mvn test
```

All tests should pass successfully.

## 🚀 Quick Start

### Running the Editor

**Option 1: Using the JAR file (Recommended)**
```bash
java -jar target/text-editor-2.0.0.jar
```

**Option 2: Using Maven**
```bash
# Linux/MacOS
mvn exec:java -Dexec.mainClass="com.texteditor.Main"

# Windows PowerShell
mvn exec:java '-Dexec.mainClass=com.texteditor.Main'
```

### Basic Usage Example

```
欢迎使用文本编辑器
输入 'exit' 退出程序

> init text hello.txt
已创建: hello.txt

[hello.txt]> append "Hello, World!"
已追加

[hello.txt]> append "This is a text editor."
已追加

[hello.txt]> show
1: Hello, World!
2: This is a text editor.

[hello.txt]> save
已保存: hello.txt

[hello.txt]> exit
再见！
```

## 📖 Command Reference

### Workspace Commands

| Command | Description | Example |
|---------|-------------|---------|
| `load <file>` | Load an existing file or create new | `load document.txt` |
| `save [file\|all]` | Save current/specified/all files | `save` or `save all` |
| `init <text\|xml> <file> [with-log]` | Create a new file buffer | `init text note.txt` |
| `close [file]` | Close current or specified file | `close` or `close note.txt` |
| `edit <file>` | Switch to another open file | `edit document.txt` |
| `editor-list` | List all open files with status | `editor-list` |
| `dir-tree [path]` | Display directory tree | `dir-tree` or `dir-tree ./src` |
| `undo` | Undo last editing operation | `undo` |
| `redo` | Redo last undone operation | `redo` |
| `exit` | Exit the editor | `exit` |

### Text Editing Commands

These commands work with `.txt` files:

| Command | Description | Example |
|---------|-------------|---------|
| `append "text"` | Append text as new line | `append "New line"` |
| `insert <line:col> "text"` | Insert text at position | `insert 1:5 "Hello"` |
| `delete <line:col> <len>` | Delete characters | `delete 1:1 5` |
| `replace <line:col> <len> "text"` | Replace text | `replace 1:1 5 "Hi"` |
| `show [start:end]` | Display content | `show` or `show 1:10` |

### XML Editing Commands

These commands work with `.xml` files:

| Command | Description | Example |
|---------|-------------|---------|
| `insert-before <tag> <newId> <targetId> ["text"]` | Insert element before target | `insert-before item item2 item1` |
| `append-child <tag> <newId> <parentId> ["text"]` | Append child element | `append-child title t1 book1 "Title"` |
| `edit-id <oldId> <newId>` | Change element ID | `edit-id book1 mainBook` |
| `edit-text <elementId> "text"` | Modify element text | `edit-text title1 "New Title"` |
| `delete-element <elementId>` | Delete element and children | `delete-element book2` |
| `xml-tree [file]` | Display XML tree structure | `xml-tree` |

### Logging Commands

| Command | Description | Example |
|---------|-------------|---------|
| `log-on [file]` | Enable logging for file | `log-on` |
| `log-off [file]` | Disable logging for file | `log-off` |
| `log-show [file]` | Display log contents | `log-show` |

### Spell Check Command

| Command | Description | Example |
|---------|-------------|---------|
| `spell-check [file]` | Check spelling in file | `spell-check` |

## 📝 Examples

### Example 1: Creating and Editing a Text File

```bash
# Create a new text file with logging enabled
> init text notes.txt with-log
已创建: notes.txt

# Add some content
[notes.txt]> append "Meeting notes for today"
已追加

[notes.txt]> append "1. Discuss project timeline"
已追加

[notes.txt]> append "2. Review code changes"
已追加

# View the content
[notes.txt]> show
1: # log
2: Meeting notes for today
3: 1. Discuss project timeline
4: 2. Review code changes

# Insert text at specific position
[notes.txt]> insert 2:1 "Important: "
已插入

# Undo the insertion
[notes.txt]> undo
已撤销

# Save and close
[notes.txt]> save
已保存: notes.txt
```

### Example 2: Working with XML Files

```bash
# Create a new XML file
> init xml config.xml
已创建: config.xml

# View initial structure
[config.xml]> xml-tree
root [id="root"]

# Add a settings element
[config.xml]> append-child settings settings1 root
已追加子元素

# Add a theme setting
[config.xml]> append-child theme theme1 settings1 "dark"
已追加子元素

# View the tree
[config.xml]> xml-tree
root [id="root"]
└── settings [id="settings1"]
    └── theme [id="theme1"]
        └── "dark"

# Modify the theme text
[config.xml]> edit-text theme1 "light"
已修改元素文本

# Save the file
[config.xml]> save
已保存: config.xml
```

### Example 3: Multi-File Editing

```bash
# Load multiple files
> load file1.txt
已加载: file1.txt

[file1.txt]> load file2.txt
已加载: file2.txt

# List all open files (shows editing time)
[file2.txt]> editor-list
 file1.txt (10秒)
>file2.txt (5秒)

# Switch between files
[file2.txt]> edit file1.txt
切换到: file1.txt

# Save all files at once
[file1.txt]> save all
已保存所有文件
```

### Example 4: Spell Checking

```bash
# For text files
[document.txt]> spell-check
拼写检查结果:
第 1 行，第 5 列: "recieve" -> 建议: receive
第 3 行，第 12 列: "occured" -> 建议: occurred

# For XML files (checks text content only)
[config.xml]> spell-check
拼写检查结果:
元素 title1: "Itallian" -> 建议: Italian
```

## 📁 File Formats

### Text Files (.txt)

Text files with `# log` as the first line will automatically enable logging:

```
# log
Your content here...
```

### XML Files (.xml)

XML files must follow these rules:
- Each element must have a unique `id` attribute
- Elements can contain either text content OR child elements (not both)
- Enable logging by adding `log="true"` to the root element

**Basic XML structure:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<root id="root">
    <child id="child1">Text content</child>
</root>
```

**XML with logging enabled:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<root id="root" log="true">
    <child id="child1">Text content</child>
</root>
```

## 🏗️ Architecture

The project follows a layered architecture:

```
┌─────────────────────────────────────────────┐
│           Presentation Layer                 │
│         (Main, CommandProcessor)             │
└─────────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────────┐
│          Business Logic Layer                │
│  ┌─────────────┐  ┌─────────────┐           │
│  │  Workspace  │  │   Command   │           │
│  └─────────────┘  └─────────────┘           │
│  ┌─────────────┐  ┌─────────────┐           │
│  │   Editor    │  │  Statistics │           │
│  └─────────────┘  └─────────────┘           │
│  ┌─────────────┐  ┌─────────────┐           │
│  │   Logging   │  │ SpellCheck  │           │
│  └─────────────┘  └─────────────┘           │
└─────────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────────┐
│              Data Layer                      │
│    (Model, XML Adapter, Persistence)         │
└─────────────────────────────────────────────┘
```

### Key Design Patterns Used

- **Command Pattern**: Implements undo/redo functionality
- **Observer Pattern**: Logging and statistics modules
- **Composite Pattern**: XML tree structure representation
- **Adapter Pattern**: XML parsing and spell checker integration
- **Memento Pattern**: Workspace state persistence

For detailed architecture information, see [architecture.md](architecture.md).

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run tests with detailed output
mvn test -Dsurefire.useFile=false

# Generate test report
mvn surefire-report:report
```

### Test Reports

After running tests, reports are available at:
- Console output: Immediate test results
- XML reports: `target/surefire-reports/*.xml`
- Text reports: `target/surefire-reports/*.txt`

### Test Coverage

The project includes comprehensive tests for:
- Text Editor operations (13 tests)
- XML Editor operations (25 tests)
- Command execution (7 tests)
- Workspace management (11 tests)
- Logging functionality (4 tests)
- Session statistics (12 tests)
- XML parsing/serialization (10 tests)
- Spell checking (various tests)

## 📊 Session Statistics

The editor tracks editing time for each file during a session:

- **Time starts**: When a file becomes active (via `load` or `edit`)
- **Time pauses**: When switching to another file or closing
- **Time accumulates**: Multiple editing sessions for the same file are summed
- **Time resets**: When a file is closed and reopened

Time is displayed in human-readable format:
| Duration | Format | Example |
|----------|--------|---------|
| < 1 minute | X秒 | 45秒 |
| 1-59 minutes | X分钟 | 25分钟 |
| 1-23 hours | X小时Y分钟 | 2小时15分钟 |
| ≥ 24 hours | X天Y小时 | 1天3小时 |

## 🔧 Configuration

### Workspace State

The editor automatically saves workspace state to `.editorWorkspace` file, including:
- List of open files
- Current active file
- File modification status
- Logging status for each file

### Log Files

Command logs are stored as hidden files in the same directory as the source file:
- For `document.txt` → `.document.txt.log`

## ⌨️ Keyboard Tips

- Use quotes `"..."` for text containing spaces
- Position format is `line:col` (both start from 1)
- Commands are case-insensitive
- Press Enter after each command

## 🐛 Troubleshooting

### Common Issues

1. **"没有活动文件" (No active file)**
   - Load or create a file first using `load` or `init`

2. **"文件未打开" (File not open)**
   - Use `load` to open the file before editing

3. **"行号或列号越界" (Line or column out of bounds)**
   - Check the file content with `show` to see valid positions

4. **XML command errors**
   - Ensure the file is an XML file (`.xml` extension)
   - Check that element IDs exist and are unique


**Version**: 2.0.0  
**Encoding**: UTF-8  
**Java Version**: 11+
