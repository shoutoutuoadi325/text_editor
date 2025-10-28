# 架构设计文档

## 1. 系统架构

### 1.1 模块划分

本项目采用分层架构，主要包含以下模块：

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│    (CommandProcessor, Main)             │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Business Logic Layer            │
│  ┌──────────┐  ┌──────────┐             │
│  │Workspace │  │ Command  │             │
│  │          │  │ Pattern  │             │
│  └─────┬────┘  └──────────┘             │
│        │                                 │
│  ┌─────▼────┐  ┌──────────┐             │
│  │ Editor   │  │ Logging  │             │
│  └──────────┘  └──────────┘             │
└─────────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Data Layer                      │
│    (Model, Persistence)                 │
└─────────────────────────────────────────┘
```

### 1.2 模块职责

#### Presentation Layer（表示层）

- **Main**: 应用入口，初始化系统，启动主循环
- **CommandProcessor**: 命令解析器，负责解析用户输入并分发到相应的处理逻辑

#### Business Logic Layer（业务逻辑层）

- **Workspace**: 工作区管理器
  - 管理所有打开的编辑器
  - 维护活动编辑器
  - 协调各模块交互
  - 提供状态持久化功能

- **Editor**: 编辑器接口及实现
  - TextEditor: 文本编辑器实现
  - 管理文本内容（使用List<String>存储行）
  - 提供编辑操作（append, insert, delete, replace, show）
  - 维护undo/redo历史

- **Command**: 命令模式实现
  - Command接口：定义命令的执行和撤销
  - 具体命令：AppendCommand, InsertCommand, DeleteCommand, ReplaceCommand
  - 支持undo/redo功能

- **Logging**: 日志模块
  - LogManager: 日志管理器，实现Observer模式
  - CommandObserver: 观察者接口
  - 记录命令执行历史
  - 提供日志查看功能

#### Data Layer（数据层）

- **Model**: 数据模型
  - Position: 位置信息（行号、列号）
  - EditorState: 编辑器状态（用于持久化）
  
- **Persistence**: 持久化
  - WorkspaceMemento: 工作区状态快照
  - 文件IO操作

### 1.3 模块依赖关系

```
Main
  └── CommandProcessor
        ├── Workspace
        │     ├── Editor (TextEditor)
        │     │     └── Command (各种具体命令)
        │     └── LogManager
        └── Util (DirectoryTree)
```

## 2. 核心设计

### 2.1 设计模式应用

#### 2.1.1 命令模式 (Command Pattern)

**目的：** 实现undo/redo功能

**实现：**
```java
public interface Command {
    void execute();
    void undo();
    String getDescription();
    boolean isModifying();
}
```

#### 2.1.2 观察者模式 (Observer Pattern)

**目的：** 实现日志记录的解耦

**实现：**
```java
public interface CommandObserver {
    void onCommandExecuted(String filePath, String commandDescription);
}
```

#### 2.1.3 备忘录模式 (Memento Pattern)

**目的：** 实现工作区状态的持久化和恢复

### 2.2 数据结构设计

#### 文本存储

使用 `List<String>` 存储文本，每个元素代表一行。

#### Undo/Redo栈

使用两个栈实现undo/redo。

## 3. 运行说明

### 3.1 使用的编程语言及版本

- Java 11

### 3.2 安装依赖的步骤

```bash
cd java
mvn clean install
```

### 3.3 运行程序的命令

```bash
# 打包后运行
mvn package
java -jar target/text-editor-1.0.0.jar

# 或使用Maven直接运行
mvn exec:java -Dexec.mainClass="com.texteditor.Main"
```

### 3.4 运行测试的命令

```bash
# 运行所有测试
mvn test

# 查看测试报告
mvn surefire-report:report
```

## 4. 测试文档

### 4.1 测试用例列表

#### Editor层测试（13个测试用例）
- testInitialState - 初始状态测试
- testAppend - 追加测试
- testInsertSingleLine - 单行插入测试
- testInsertMultiline - 多行插入测试
- testInsertInEmptyFile - 空文件插入测试
- testInsertInvalidPositionEmptyFile - 无效位置插入测试
- testDelete - 删除测试
- testDeleteInvalidLength - 无效删除长度测试
- testReplace - 替换测试
- testShow - 显示全部测试
- testShowRange - 显示范围测试
- testGetContent - 获取内容测试
- testSetLines - 设置行测试

#### Command层测试（8个测试用例）
- testAppendCommand - 追加命令测试
- testInsertCommand - 插入命令测试
- testDeleteCommand - 删除命令测试
- testReplaceCommand - 替换命令测试
- testUndoRedoChain - Undo/Redo链测试
- testCommandIsModifying - 命令修改标记测试
- testCommandDescription - 命令描述测试

#### Workspace层测试（11个测试用例）
- testLoadExistingFile - 加载已存在文件测试
- testLoadNonexistentFile - 加载不存在文件测试
- testInitFile - 初始化文件测试
- testInitFileWithLog - 带日志初始化测试
- testSaveFile - 保存文件测试
- testSaveAll - 保存所有文件测试
- testCloseFile - 关闭文件测试
- testSwitchToFile - 切换文件测试
- testGetOpenFiles - 获取打开文件测试
- testHasUnsavedChanges - 检查未保存更改测试
- testMemento - 备忘录测试

#### Logging层测试（4个测试用例）
- testEnableDisableLogging - 启用/禁用日志测试
- testOnCommandExecuted - 命令执行日志测试
- testShowLogNonexistent - 显示不存在日志测试
- testLoggingDisabled - 禁用日志测试

### 4.2 测试执行结果

**总计：36个测试用例**

```
[INFO] Tests run: 13, Failures: 0, Errors: 0, Skipped: 0 - TextEditorTest
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0 - CommandTest
[INFO] Tests run: 11, Failures: 0, Errors: 0, Skipped: 0 - WorkspaceTest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 - LogManagerTest

Results: 100% Pass Rate
```

**测试覆盖：**
- ✅ 所有核心功能已测试
- ✅ 边界条件已覆盖
- ✅ 异常处理已验证
- ✅ 集成场景已测试

## 5. 设计模式应用说明

### 5.1 命令模式

**应用场景：** 文本编辑操作的undo/redo

**优势：**
- 将请求封装成对象
- 支持可撤销操作
- 易于扩展新命令

### 5.2 观察者模式

**应用场景：** 日志记录系统

**优势：**
- 日志模块与命令执行解耦
- 支持多个观察者
- 易于添加新的事件监听器

### 5.3 备忘录模式

**应用场景：** 工作区状态持久化

**优势：**
- 不破坏封装性
- 状态保存和恢复逻辑清晰
- 支持会话恢复

## 6. 其他设计相关说明

### 6.1 依赖管理

- 使用Maven管理依赖
- 无第三方运行时依赖
- 测试依赖仅限于JUnit 5

### 6.2 编码规范

- 遵循Java命名规范
- 使用UTF-8编码
- 符合Google Java Style Guide

### 6.3 错误处理

- 预期错误：友好提示
- 意外错误：记录并提示
- 日志失败：警告但不中断程序
