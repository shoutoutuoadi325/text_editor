# 文本编辑器架构设计文档

## 1. 系统架构

### 1.1 模块划分

本文本编辑器系统采用分层架构，主要分为以下模块：

```
text_editor/
├── commands/           # 命令模式实现
│   └── command.py     # Command 和 CommandHistory
├── editors/           # 编辑器实现
│   └── text_editor.py # TextEditor 和编辑命令
├── workspace/         # 工作区管理
│   └── workspace.py   # Workspace 和 WorkspaceMemento
├── logging/           # 日志模块
│   └── logger.py      # Logger (Observer实现)
├── utils/             # 工具类
│   └── observer.py    # Observer 和 Subject
├── command_processor.py  # 命令处理器
└── main.py           # 主程序入口
```

### 1.2 模块职责

#### 1.2.1 命令模块 (commands/)
- **职责**: 实现命令模式，支持撤销/重做功能
- **核心类**:
  - `Command`: 抽象命令基类
  - `CommandHistory`: 管理命令历史栈
- **设计模式**: Command Pattern (命令模式)

#### 1.2.2 编辑器模块 (editors/)
- **职责**: 管理文本内容，提供编辑操作
- **核心类**:
  - `TextEditor`: 文本编辑器，使用行数组存储内容
  - `AppendCommand`, `InsertCommand`, `DeleteCommand`, `ReplaceCommand`: 具体编辑命令
- **数据结构**: List[String] 存储文本行
- **设计模式**: Command Pattern (命令模式)

#### 1.2.3 工作区模块 (workspace/)
- **职责**: 管理多个编辑器实例，处理文件加载/保存，状态持久化
- **核心类**:
  - `Workspace`: 工作区主类，管理多个编辑器
  - `WorkspaceMemento`: 工作区状态快照
- **设计模式**: 
  - Memento Pattern (备忘录模式) - 用于状态持久化
  - Observer Pattern (观察者模式) - 作为事件发布者

#### 1.2.4 日志模块 (logging/)
- **职责**: 记录命令执行历史
- **核心类**:
  - `Logger`: 日志记录器，实现Observer接口
- **设计模式**: Observer Pattern (观察者模式)

#### 1.2.5 工具模块 (utils/)
- **职责**: 提供通用工具类
- **核心类**:
  - `Observer`: 观察者接口
  - `Subject`: 被观察者基类
  - `EventType`: 事件类型枚举
- **设计模式**: Observer Pattern (观察者模式)

#### 1.2.6 命令处理器 (command_processor.py)
- **职责**: 解析用户输入，路由到相应的命令处理方法
- **核心类**:
  - `CommandProcessor`: 命令解析和执行

#### 1.2.7 主程序 (main.py)
- **职责**: 应用程序入口，主循环
- **功能**: 初始化系统，运行交互循环

### 1.3 模块依赖关系

```
main.py
  └── CommandProcessor
       └── Workspace (Subject)
            ├── TextEditor
            │    └── CommandHistory
            │         └── Command
            └── Logger (Observer)
                 └── Observer
```

**依赖说明**:
- `main.py` 依赖 `CommandProcessor` 和 `Workspace`
- `CommandProcessor` 依赖 `Workspace`
- `Workspace` 依赖 `TextEditor` 和 `Logger`
- `TextEditor` 依赖 `Command` 和 `CommandHistory`
- `Logger` 实现 `Observer` 接口
- `Workspace` 继承 `Subject` 类

**依赖原则**:
- 高层模块不依赖低层模块，都依赖于抽象 (依赖倒置原则)
- 模块间通过接口通信 (接口隔离原则)
- 单一职责，每个模块只负责一个功能领域

## 2. 核心设计

### 2.1 设计模式应用

#### 2.1.1 命令模式 (Command Pattern)

**应用场景**: 文本编辑操作的撤销/重做

**实现**:
```python
class Command(ABC):
    @abstractmethod
    def execute(self) -> bool: pass
    
    @abstractmethod
    def undo(self) -> bool: pass
    
    @abstractmethod
    def redo(self) -> bool: pass

class AppendCommand(Command):
    def __init__(self, editor, text):
        self.editor = editor
        self.text = text
    
    def execute(self):
        self.editor._lines.append(self.text)
        return True
    
    def undo(self):
        self.editor._lines.pop()
        return True
```

**优点**:
- 将操作封装为对象，便于参数化和队列化
- 支持撤销/重做功能
- 易于添加新命令

#### 2.1.2 观察者模式 (Observer Pattern)

**应用场景**: 
1. 日志记录：Logger观察Workspace的命令执行事件
2. 事件通知：Workspace通知各种状态变更

**实现**:
```python
class Observer(ABC):
    @abstractmethod
    def update(self, event_type, data): pass

class Logger(Observer):
    def update(self, event_type, data):
        if event_type == EventType.COMMAND_EXECUTED:
            self._log_command(data)

class Workspace(Subject):
    def notify_command(self, command):
        self.notify(EventType.COMMAND_EXECUTED, {
            'command': command,
            'file': self._active_file
        })
```

**优点**:
- 松耦合，Subject和Observer独立变化
- 支持广播通信
- 符合开闭原则

#### 2.1.3 备忘录模式 (Memento Pattern)

**应用场景**: 工作区状态的保存和恢复

**实现**:
```python
class WorkspaceMemento:
    def __init__(self, state: dict):
        self._state = state.copy()
    
    def get_state(self) -> dict:
        return self._state.copy()

class Workspace:
    def create_memento(self) -> WorkspaceMemento:
        state = {
            'active_file': self._active_file,
            'files': {...},
            'log_enabled': [...]
        }
        return WorkspaceMemento(state)
    
    def restore_from_memento(self, memento):
        state = memento.get_state()
        # 恢复状态...
```

**优点**:
- 保存和恢复对象状态
- 不破坏封装性
- 简化对象状态管理

### 2.2 关键设计决策

#### 2.2.1 文本存储结构

**决策**: 使用 `List[String]` 存储文本行

**理由**:
- 便于按行号访问和操作
- 简化显示操作 (show命令)
- 符合需求规格

#### 2.2.2 命令解析

**决策**: 使用正则表达式和字符串解析

**理由**:
- 简单直接，无需引入解析库
- 支持引号内文本的正确提取
- 易于维护和扩展

#### 2.2.3 状态持久化

**决策**: 使用JSON格式保存工作区状态

**理由**:
- 人类可读
- Python原生支持
- 结构化存储

#### 2.2.4 日志格式

**决策**: 纯文本格式，每行一条记录

**理由**:
- 符合需求规格
- 易于查看和调试
- 追加写入性能好

## 3. 数据流

### 3.1 命令执行流程

```
用户输入
  ↓
CommandProcessor.process_command()
  ↓
命令解析 (_parse_command)
  ↓
路由到具体命令方法 (_cmd_xxx)
  ↓
调用 Workspace/Editor 方法
  ↓
执行操作 (通过Command对象)
  ↓
通知观察者 (notify_command)
  ↓
Logger 记录日志
  ↓
返回结果给用户
```

### 3.2 文件加载流程

```
load命令
  ↓
Workspace.load_file()
  ↓
检查文件是否已打开
  ↓
读取文件内容 (如果存在)
  ↓
创建 TextEditor 实例
  ↓
检查是否需要自动启用日志 (#log)
  ↓
设置为活动文件
  ↓
发送 FILE_LOADED 事件
```

### 3.3 撤销/重做流程

```
undo命令
  ↓
Editor.undo()
  ↓
CommandHistory.undo()
  ↓
从undo栈弹出命令
  ↓
执行 Command.undo()
  ↓
命令移到redo栈
  ↓
标记文件为已修改
```

## 4. 接口设计

### 4.1 主要接口

#### Command接口
```python
class Command(ABC):
    def execute(self) -> bool
    def undo(self) -> bool
    def redo(self) -> bool
```

#### Observer接口
```python
class Observer(ABC):
    def update(self, event_type: EventType, data: Dict[str, Any])
```

### 4.2 公共API

#### TextEditor
```python
def append(text: str) -> bool
def insert(line: int, col: int, text: str) -> Tuple[bool, str]
def delete(line: int, col: int, length: int) -> Tuple[bool, str]
def replace(line: int, col: int, length: int, text: str) -> Tuple[bool, str]
def show(start_line: int = None, end_line: int = None) -> str
def undo() -> bool
def redo() -> bool
```

#### Workspace
```python
def load_file(filepath: str) -> Tuple[bool, str]
def save_file(filepath: str = None) -> Tuple[bool, str]
def init_file(filepath: str, with_log: bool) -> Tuple[bool, str]
def close_file(filepath: str = None, force: bool) -> Tuple[bool, str, bool]
def switch_editor(filepath: str) -> Tuple[bool, str]
```

## 5. 测试策略

### 5.1 测试分层

1. **单元测试**: 测试各个模块的独立功能
   - test_command.py: 命令模式测试
   - test_observer.py: 观察者模式测试
   - test_text_editor.py: 编辑器功能测试
   - test_workspace.py: 工作区功能测试
   - test_logger.py: 日志功能测试

2. **集成测试**: 测试模块间协作
   - test_integration.py: 完整工作流测试

### 5.2 测试覆盖

- 命令模式: 11个测试用例
- 观察者模式: 5个测试用例
- 文本编辑器: 21个测试用例
- 工作区: 13个测试用例
- 日志: 9个测试用例
- 集成测试: 8个测试用例

**总计**: 67个测试用例，100%通过

## 6. 扩展性考虑

### 6.1 支持新的编辑器类型

通过继承基类或接口，可以轻松添加新的编辑器类型（如XMLEditor）:

```python
class XMLEditor(TextEditor):
    # 重写或扩展方法
    pass
```

### 6.2 添加新的命令

通过实现Command接口添加新命令:

```python
class NewCommand(Command):
    def execute(self): ...
    def undo(self): ...
    def redo(self): ...
```

### 6.3 添加新的观察者

通过实现Observer接口添加新的事件监听器:

```python
class NewObserver(Observer):
    def update(self, event_type, data):
        # 处理事件
        pass
```

## 7. 总结

本系统采用了清晰的分层架构和多种设计模式，实现了：

- **高内聚低耦合**: 各模块职责明确，依赖关系清晰
- **易于测试**: 分层测试，单元测试和集成测试完善
- **易于扩展**: 通过设计模式支持功能扩展
- **符合SOLID原则**: 单一职责、开闭原则、依赖倒置等

设计模式应用总结：
- ✅ 命令模式: 实现undo/redo
- ✅ 观察者模式: 实现事件通知和日志记录
- ✅ 备忘录模式: 实现状态持久化
