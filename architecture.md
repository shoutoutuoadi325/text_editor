# 架构设计文档

## 1. 系统架构

### 1.1 模块划分

本项目采用分层架构，Lab2 在 Lab1 基础上新增了 XML 编辑、统计和拼写检查模块：

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│              (CommandProcessor, Main)                        │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                   Business Logic Layer                       │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  Workspace   │  │   Command    │  │   SpellCheck     │  │
│  │              │  │   Pattern    │  │   (适配器模式)    │  │
│  └──────┬───────┘  └──────────────┘  └──────────────────┘  │
│         │                                                    │
│  ┌──────▼───────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │   Editor     │  │   Logging    │  │   Statistics     │  │
│  │ (多态实现)    │  │  (观察者)    │  │   (观察者/装饰器) │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                      Data Layer                              │
│     (Model: XmlElement, Position, EditorState)              │
│     (XML: XmlAdapter, XmlParseException)                    │
│     (Persistence: WorkspaceMemento)                         │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 模块职责

#### Presentation Layer（表示层）

- **Main**: 应用入口，初始化系统，启动主循环
- **CommandProcessor**: 命令解析器，负责解析用户输入并分发到相应的处理逻辑
  - Lab2 新增：支持 XML 编辑命令和拼写检查命令

#### Business Logic Layer（业务逻辑层）

- **Workspace**: 工作区管理器
  - 管理所有打开的编辑器（文本和 XML）
  - 维护活动编辑器
  - 协调各模块交互
  - 提供状态持久化功能
  - **Lab2 新增**：集成 SessionStatistics 进行编辑时长统计

- **Editor**: 编辑器接口及实现（**Lab2 扩展多态设计**）
  - **TextEditor**: 文本编辑器实现
    - 管理文本内容（使用List<String>存储行）
    - 提供编辑操作（append, insert, delete, replace, show）
    - 维护undo/redo历史
  - **XmlEditor** (Lab2 新增): XML 编辑器实现
    - 管理 XML DOM 树结构
    - 提供 XML 编辑操作（insert-before, append-child, edit-id, edit-text, delete-element）
    - 生成 XML 树形结构显示（xml-tree）
    - 维护 id -> element 快速查找映射
    - 支持 undo/redo 功能

- **Command**: 命令模式实现
  - Command接口：定义命令的执行和撤销
  - 文本命令：AppendCommand, InsertCommand, DeleteCommand, ReplaceCommand
  - **Lab2 新增** XML命令：XmlInsertBeforeCommand, XmlAppendChildCommand, XmlEditIdCommand, XmlEditTextCommand, XmlDeleteElementCommand
  - 支持undo/redo功能

- **Logging**: 日志模块
  - LogManager: 日志管理器，实现Observer模式
  - CommandObserver: 观察者接口
  - 记录命令执行历史
  - 提供日志查看功能

- **Statistics** (Lab2 新增): 统计模块
  - SessionStatistics: 会话统计管理器
    - 跟踪每个文件的编辑时长
    - 支持文件切换时的自动计时
    - 提供格式化的时长显示

- **SpellCheck** (Lab2 新增): 拼写检查模块
  - SpellChecker: 拼写检查接口（适配器模式）
  - SimpleSpellChecker: 简单的内置拼写检查实现
  - SpellCheckService: 拼写检查服务，处理文本和 XML 文件
  - SpellingError: 拼写错误信息类

#### Data Layer（数据层）

- **Model**: 数据模型
  - Position: 位置信息（行号、列号）
  - EditorState: 编辑器状态（用于持久化）
  - **XmlElement** (Lab2 新增): XML 元素模型，使用组合模式表示树形结构
  
- **XML** (Lab2 新增): XML 处理
  - XmlAdapter: XML 解析和序列化适配器（适配器模式）
  - XmlParseException: XML 解析异常类

- **Persistence**: 持久化
  - WorkspaceMemento: 工作区状态快照
  - 文件IO操作

### 1.3 模块依赖关系

```
Main
  └── CommandProcessor
        ├── Workspace
        │     ├── Editor (TextEditor / XmlEditor)
        │     │     └── Command (文本命令 / XML命令)
        │     ├── LogManager
        │     └── SessionStatistics (Lab2 新增)
        ├── SpellCheckService (Lab2 新增)
        │     └── SpellChecker (SimpleSpellChecker)
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

**Lab2 扩展：** 新增 XML 编辑命令
- XmlInsertBeforeCommand: 在目标元素前插入新元素
- XmlAppendChildCommand: 在父元素内追加子元素
- XmlEditIdCommand: 修改元素 ID
- XmlEditTextCommand: 修改元素文本内容
- XmlDeleteElementCommand: 删除元素及其子元素

#### 2.1.2 观察者模式 (Observer Pattern)

**目的：** 实现日志记录和统计功能的解耦

**实现：**
```java
public interface CommandObserver {
    void onCommandExecuted(String filePath, String commandDescription);
}
```

**Lab2 扩展：** SessionStatistics 作为观察者监听文件切换事件

#### 2.1.3 备忘录模式 (Memento Pattern)

**目的：** 实现工作区状态的持久化和恢复

#### 2.1.4 组合模式 (Composite Pattern) - Lab2 新增

**目的：** 表示 XML 树形结构

**实现：**
```java
public class XmlElement {
    private String tagName;
    private String id;
    private Map<String, String> attributes;
    private String textContent;
    private List<XmlElement> children;  // 子元素列表
    private XmlElement parent;          // 父元素引用
}
```

#### 2.1.5 适配器模式 (Adapter Pattern) - Lab2 新增

**目的：** 
1. 适配 XML 解析库（XmlAdapter）
2. 隔离第三方拼写检查库依赖（SpellChecker 接口）

**实现：**
```java
// XML 解析适配器
public class XmlAdapter {
    public XmlElement parse(String xmlContent);
    public String serialize(XmlElement root);
}

// 拼写检查接口（支持替换不同实现）
public interface SpellChecker {
    List<SpellingError> checkText(String text);
    boolean isCorrect(String word);
    List<String> getSuggestions(String word);
    boolean isAvailable();
}
```

#### 2.1.6 装饰器模式 (Decorator Pattern) - Lab2 新增

**目的：** 在显示文件列表时，为每个文件名添加编辑时长信息

**应用场景：** editor-list 命令输出增强

### 2.2 数据结构设计

#### 文本存储

使用 `List<String>` 存储文本，每个元素代表一行。

#### XML 存储 (Lab2 新增)

使用 `XmlElement` 树形结构存储 XML，维护 `id -> element` 映射支持快速查找。

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

# 或使用Maven直接运行（Linux/MacOS)
mvn exec:java -Dexec.mainClass="com.texteditor.Main"

# 如果使用Windows PowerShell:
mvn exec:java '-Dexec.mainClass=com.texteditor.Main'
```

### 3.4 运行测试的命令

```bash
# 运行所有测试
mvn test

# 查看测试报告（路径为./java/target/surefire-reports/）
mvn surefire-report:report
```

## 4. 测试文档

### 4.1 测试用例列表

#### Lab1 原有测试

##### TextEditor层测试（13个测试用例）
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

##### Command层测试（7个测试用例）
- testAppendCommand - 追加命令测试
- testInsertCommand - 插入命令测试
- testDeleteCommand - 删除命令测试
- testReplaceCommand - 替换命令测试
- testUndoRedoChain - Undo/Redo链测试
- testCommandIsModifying - 命令修改标记测试
- testCommandDescription - 命令描述测试

##### Workspace层测试（11个测试用例）
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

##### Logging层测试（4个测试用例）
- testEnableDisableLogging - 启用/禁用日志测试
- testOnCommandExecuted - 命令执行日志测试
- testShowLogNonexistent - 显示不存在日志测试
- testLoggingDisabled - 禁用日志测试

#### Lab2 新增测试

##### XmlEditor层测试（25个测试用例）
- testLoadFromContent - 加载 XML 内容测试
- testGetElementById - 根据 ID 获取元素测试
- testInsertBefore - 在元素前插入测试
- testInsertBeforeWithDuplicateId - 重复 ID 插入测试
- testInsertBeforeInvalidTarget - 无效目标元素插入测试
- testInsertBeforeRoot - 在根元素前插入测试
- testAppendChild - 追加子元素测试
- testAppendChildWithDuplicateId - 重复 ID 追加测试
- testAppendChildToInvalidParent - 无效父元素追加测试
- testAppendChildToElementWithText - 向文本元素追加子元素测试
- testEditId - 修改元素 ID 测试
- testEditIdNonexistent - 修改不存在元素 ID 测试
- testEditIdDuplicate - 修改为已存在 ID 测试
- testEditIdRoot - 修改根元素 ID 测试
- testEditText - 修改元素文本测试
- testEditTextNonexistent - 修改不存在元素文本测试
- testEditTextOnParent - 修改父元素文本测试
- testDeleteElement - 删除元素测试
- testDeleteElementNonexistent - 删除不存在元素测试
- testDeleteRoot - 删除根元素测试
- testDeleteElementWithChildren - 删除有子元素的元素测试
- testGenerateTree - 生成树形结构测试
- testGetContent - 获取 XML 内容测试
- testGetAllTextContents - 获取所有文本内容测试
- testLoggingEnabled - 日志开关测试

##### XmlAdapter层测试（10个测试用例）
- testParseSimpleXml - 解析简单 XML 测试
- testParseXmlWithChildren - 解析带子元素的 XML 测试
- testParseXmlWithAttributes - 解析带属性的 XML 测试
- testParseMissingId - 缺少 ID 属性解析测试
- testParseInvalidXml - 无效 XML 解析测试
- testSerializeSimple - 序列化简单元素测试
- testSerializeWithChildren - 序列化带子元素测试
- testSerializeWithAttributes - 序列化带属性测试
- testRoundTrip - 解析-序列化往返测试
- testEscapeSpecialCharacters - 特殊字符转义测试

##### SessionStatistics层测试（12个测试用例）
- testInitialState - 初始状态测试
- testFileActivated - 文件激活测试
- testFileSwitching - 文件切换测试
- testFileClosed - 文件关闭测试
- testCumulativeTime - 累计时间测试
- testOnExit - 退出时测试
- testFormatDurationSeconds - 秒格式化测试
- testFormatDurationMinutes - 分钟格式化测试
- testFormatDurationHours - 小时格式化测试
- testFormatDurationDays - 天格式化测试
- testInitFileStatistics - 初始化文件统计测试
- testGetFormattedEditingTime - 获取格式化时间测试

##### SpellCheck层测试（16个测试用例）
- testIsAvailable - 检查器可用性测试
- testCorrectWords - 正确单词测试
- testCorrectWordsCaseInsensitive - 大小写不敏感测试
- testIncorrectWords - 错误单词测试
- testGetSuggestions - 获取建议测试
- testCheckTextNoErrors - 无错误文本检查测试
- testCheckTextWithErrors - 有错误文本检查测试
- testCheckTextMultipleLines - 多行文本检查测试
- testCheckTextSingleLetter - 单字母忽略测试
- testCheckTextEditor - 文本编辑器检查测试
- testCheckTextEditorWithErrors - 文本编辑器错误检查测试
- testCheckXmlEditor - XML 编辑器检查测试
- testCheckXmlEditorWithErrors - XML 编辑器错误检查测试
- testCheckWithUnavailableChecker - 不可用检查器测试
- testSpellingErrorFormat - 错误格式化测试
- testSpellingErrorIsTextFileError - 文本文件错误判断测试

### 4.2 测试执行结果

全部 98 个测试通过：
- Lab1 原有测试：35 个
- Lab2 新增测试：63 个

## 5. 接口设计说明

本项目遵循**依赖倒置原则（Dependency Inversion Principle, DIP）**，高层模块不依赖于低层模块的具体实现，而是依赖于抽象接口。所有核心接口设计合理，抽象层次清晰，具有良好的可扩展性和可维护性。

### 5.1 核心接口一览

#### 5.1.1 Editor接口

**接口定义：**
```java
public interface Editor {
    // 文件管理
    String getFilePath();
    boolean isModified();
    void setModified(boolean modified);
    
    // 内容访问
    List<String> getLines();
    void setLines(List<String> lines);
    String getContent();
    
    // 编辑操作
    void append(String text);
    void insert(Position position, String text);
    void delete(Position position, int length);
    void replace(Position position, int length, String text);
    String show(int startLine, int endLine);
    
    // 命令历史管理
    void executeCommand(Command command);
    boolean undo();
    boolean redo();
    boolean canUndo();
    boolean canRedo();
}
```

**实现类：** `TextEditor`

**设计特点：**
- **高层抽象**：定义了文本编辑器的核心能力，不涉及具体实现细节
- **职责单一**：专注于文本编辑功能，不关心持久化、日志等其他职责
- **易于扩展**：可轻松实现不同类型的编辑器（如富文本编辑器、代码编辑器等）

**依赖倒置体现：**
- `Workspace`类依赖`Editor`接口而非`TextEditor`实现类
- `Command`系列类依赖`Editor`接口进行操作
- 支持运行时替换不同的编辑器实现

#### 5.1.2 Command接口

**接口定义：**
```java
public interface Command {
    void execute();              // 执行命令
    void undo();                 // 撤销命令
    String getDescription();     // 获取命令描述
    boolean isModifying();       // 判断是否修改内容
}
```

**实现类：** `AppendCommand`, `InsertCommand`, `DeleteCommand`, `ReplaceCommand`

**设计特点：**
- **命令模式核心**：封装了请求为对象，支持参数化和队列化
- **统一接口**：所有编辑操作都实现相同接口，便于统一管理
- **可撤销性**：通过`undo()`方法天然支持操作回退

**依赖倒置体现：**
- `Editor`接口的`executeCommand()`方法依赖`Command`接口
- 命令历史栈（undo/redo stack）存储`Command`接口类型
- 新增命令类型无需修改已有代码，符合开闭原则

#### 5.1.3 CommandObserver接口

**接口定义：**
```java
public interface CommandObserver {
    void onCommandExecuted(String filePath, String commandDescription);
}
```

**实现类：** `LogManager`

**设计特点：**
- **观察者模式核心**：定义了事件监听的标准接口
- **解耦设计**：命令执行者无需知道观察者的具体实现
- **可扩展性**：可轻松添加多个观察者（如统计分析、审计日志等）

**依赖倒置体现：**
- `Workspace`依赖`CommandObserver`接口而非`LogManager`实现
- 支持注入不同的观察者实现，实现不同的监听逻辑
- 日志模块可独立演化，不影响核心编辑功能

### 5.2 依赖倒置原则的应用

#### 5.2.1 高层模块与低层模块的分离

**传统依赖关系（违反DIP）：**
```
Workspace (高层) → TextEditor (低层)
CommandProcessor → AppendCommand, InsertCommand, ...
```

**本项目的依赖关系（符合DIP）：**
```
Workspace (高层) → Editor (抽象)
                      ↑
                 TextEditor (低层)

CommandProcessor → Command (抽象)
                      ↑
                 AppendCommand, InsertCommand, ... (低层)
```

#### 5.2.2 具体代码示例

**示例1：Workspace依赖Editor接口**
```java
public class Workspace {
    // 依赖接口而非实现类
    private final Map<String, Editor> editors;
    
    public Editor loadFile(String filePath) throws IOException {
        // 使用具体类只在创建对象时
        Editor editor = new TextEditor(filePath);  // 唯一的耦合点
        // 其他地方均使用Editor接口
        editors.put(filePath, editor);
        return editor;
    }
}
```

**示例2：Command依赖Editor接口**
```java
public class AppendCommand implements Command {
    // 依赖Editor接口，不关心具体实现
    private final Editor editor;
    
    public AppendCommand(Editor editor, String text) {
        this.editor = editor;  // 接受任何Editor实现
        this.text = text;
    }
    
    @Override
    public void execute() {
        editor.append(text);  // 通过接口调用方法
    }
}
```

**示例3：Workspace依赖CommandObserver接口**
```java
public class Workspace {
    // 依赖观察者接口
    private final CommandObserver logManager;
    
    public Workspace(CommandObserver logManager) {
        this.logManager = logManager;  // 接受任何观察者实现
    }
    
    // 可以轻松替换为其他观察者实现
    // 如：new Workspace(new AuditLogger())
}
```

### 5.3 接口设计的优势

#### 5.3.1 可测试性

通过依赖接口，可以轻松创建Mock对象进行单元测试：

```java
// 测试时可以使用模拟的Editor
Editor mockEditor = new MockEditor();
Command cmd = new AppendCommand(mockEditor, "test");
cmd.execute();
```

#### 5.3.2 可扩展性

**扩展场景1：** 添加新的编辑器类型
```java
// 无需修改现有代码，直接实现Editor接口
public class RichTextEditor implements Editor {
    // 支持富文本的编辑器实现
}

// Workspace可以无缝使用新编辑器
Editor editor = new RichTextEditor(filePath);
```

**扩展场景2：** 添加新的命令类型
```java
// 实现Command接口即可
public class FindCommand implements Command {
    // 查找命令实现
}

// Editor可以执行任何Command实现
editor.executeCommand(new FindCommand(...));
```

**扩展场景3：** 添加新的观察者
```java
// 实现CommandObserver接口
public class StatisticsCollector implements CommandObserver {
    // 统计命令执行频率
}

// 可以同时使用多个观察者
Workspace workspace = new Workspace(new LogManager());
workspace.addObserver(new StatisticsCollector());
```

#### 5.3.3 可维护性

- **职责清晰**：接口定义了明确的契约，各模块职责边界清晰
- **降低耦合**：高层模块不受低层实现变化的影响
- **易于重构**：可以安全地替换接口实现，不影响依赖方

### 5.4 接口抽象层次分析

#### 5.4.1 Editor接口抽象层次

| 方法类型 | 抽象层次 | 说明 |
|---------|---------|------|
| `getFilePath()`, `isModified()` | 高 | 元数据访问，与实现无关 |
| `getLines()`, `setLines()` | 中 | 定义数据结构但不限制实现 |
| `append()`, `insert()`, `delete()` | 高 | 定义操作语义，不涉及实现细节 |
| `undo()`, `redo()` | 高 | 定义能力，实现方式可多样化 |

#### 5.4.2 Command接口抽象层次

| 方法 | 抽象层次 | 说明 |
|-----|---------|------|
| `execute()` | 非常高 | 纯语义化，完全不涉及实现 |
| `undo()` | 非常高 | 逆操作的抽象定义 |
| `getDescription()` | 高 | 用于日志和调试的元信息 |
| `isModifying()` | 高 | 命令属性的抽象分类 |

**评价：** 接口方法均为高层抽象，不包含任何实现细节，符合接口隔离原则。

## 6. 设计模式应用说明

### 6.1 命令模式

**应用场景：** 文本编辑操作的undo/redo

**优势：**
- 将请求封装成对象
- 支持可撤销操作
- 易于扩展新命令

### 6.2 观察者模式

**应用场景：** 日志记录系统

**优势：**
- 日志模块与命令执行解耦
- 支持多个观察者
- 易于添加新的事件监听器

### 6.3 备忘录模式

**应用场景：** 工作区状态持久化

**优势：**
- 不破坏封装性
- 状态保存和恢复逻辑清晰
- 支持会话恢复

## 7. 其他设计相关说明

### 7.1 依赖管理

- 使用Maven管理依赖
- 无第三方运行时依赖
- 测试依赖仅限于JUnit 5

### 7.2 编码规范

- 遵循Java命名规范
- 使用UTF-8编码
- 符合Google Java Style Guide

### 7.3 错误处理

- 预期错误：友好提示
- 意外错误：记录并提示
- 日志失败：警告但不中断程序

### 7.4 接口设计总结

本项目的接口设计充分体现了**SOLID原则**中的依赖倒置原则：

1. **高层模块独立性**：`Workspace`、`CommandProcessor`等高层模块完全依赖抽象接口
2. **抽象稳定性**：接口定义稳定，扩展通过新增实现类完成
3. **实现可替换性**：任何实现类都可以无缝替换，不影响系统其他部分
4. **测试友好性**：接口使得Mock测试变得简单
5. **扩展开放性**：遵循开闭原则，对扩展开放，对修改封闭

这种设计使得系统具有良好的**可维护性**、**可扩展性**和**可测试性**。

