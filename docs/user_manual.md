# 文本编辑器使用手册

## 运行环境

### 编程语言
- Python 3.8 或更高版本

### 依赖项
- 无第三方运行时依赖
- 测试依赖: pytest, pytest-cov

## 安装和运行

### 1. 安装依赖（开发环境）

```bash
# 安装测试依赖
pip install pytest pytest-cov

# 或者安装开发依赖
pip install -e .[dev]
```

### 2. 运行程序

#### 方法1: 直接运行
```bash
python -m text_editor.main
```

#### 方法2: 安装后运行
```bash
# 安装包
pip install -e .

# 运行
text-editor
```

#### 方法3: 直接运行源代码
```bash
cd src
python -m text_editor.main
```

### 3. 运行测试

```bash
# 运行所有测试
pytest tests/ -v

# 运行单元测试
pytest tests/unit/ -v

# 运行集成测试
pytest tests/integration/ -v

# 运行测试并生成覆盖率报告
pytest tests/ -v --cov=src/text_editor --cov-report=html
```

## 命令使用说明

### 工作区命令

#### 1. load - 加载文件
```
load <file>
```
**示例**:
```
> load test.txt
Loaded /path/to/test.txt (new file)
```

#### 2. save - 保存文件
```
save [file|all]
```
**示例**:
```
> save
Saved /path/to/test.txt

> save all
Saved 3 file(s)
```

#### 3. init - 创建新缓冲区
```
init <file> [with-log]
```
**示例**:
```
> init newfile.txt
Created new buffer: newfile.txt

> init logfile.txt with-log
Created new buffer: logfile.txt
```

#### 4. close - 关闭文件
```
close [file]
```
**示例**:
```
> close
Closed /path/to/test.txt

> close test.txt
文件已修改，是否保存？(y/n)
y
Saved /path/to/test.txt
Closed /path/to/test.txt
```

#### 5. edit - 切换活动文件
```
edit <file>
```
**示例**:
```
> edit test.txt
Switched to /path/to/test.txt
```

#### 6. editor-list - 显示文件列表
```
editor-list
```
**示例**:
```
> editor-list
>/path/to/test1.txt*
 /path/to/test2.txt
```
格式说明:
- `>` 表示当前活动文件
- `*` 表示文件已修改未保存

#### 7. dir-tree - 显示目录树
```
dir-tree [path]
```
**示例**:
```
> dir-tree
src
  text_editor
    main.py
    command_processor.py
tests
  unit
    test_command.py
```

#### 8. undo - 撤销
```
undo
```
**示例**:
```
> undo
Undo successful
```

#### 9. redo - 重做
```
redo
```
**示例**:
```
> redo
Redo successful
```

#### 10. exit - 退出程序
```
exit
```
**示例**:
```
> exit
Save test.txt? (y/n)
y
Goodbye!
```

### 文本编辑命令

#### 1. append - 追加文本
```
append "text"
```
**示例**:
```
> append "This is a new line"
Text appended
```

#### 2. insert - 插入文本
```
insert <line:col> "text"
```
**示例**:
```
> insert 1:7 "beautiful "
Text inserted
```
行号和列号都从1开始计数。

#### 3. delete - 删除字符
```
delete <line:col> <len>
```
**示例**:
```
> delete 1:7 5
Text deleted
```
从指定位置开始删除指定长度的字符。

#### 4. replace - 替换字符
```
replace <line:col> <len> "text"
```
**示例**:
```
> replace 1:1 5 "Hello"
Text replaced
```
用新文本替换指定位置的字符。

#### 5. show - 显示内容
```
show [start:end]
```
**示例**:
```
> show
1: Hello world
2: This is line 2

> show 1:1
1: Hello world
```

### 日志命令

#### 1. log-on - 启用日志
```
log-on [file]
```
**示例**:
```
> log-on
Logging enabled for /path/to/test.txt

> log-on test.txt
Logging enabled for /path/to/test.txt
```

#### 2. log-off - 关闭日志
```
log-off [file]
```
**示例**:
```
> log-off
Logging disabled for /path/to/test.txt
```

#### 3. log-show - 显示日志
```
log-show [file]
```
**示例**:
```
> log-show
session start at 20251028 12:00:00
20251028 12:00:05 append "test"
20251028 12:00:10 save
```

## 使用示例

### 示例1: 创建并编辑新文件

```
> load hello.txt
Loaded /path/to/hello.txt (new file)

[/path/to/hello.txt]> append "Hello, World!"
Text appended

[/path/to/hello.txt]> append "This is a test file."
Text appended

[/path/to/hello.txt]> show
1: Hello, World!
2: This is a test file.

[/path/to/hello.txt]> save
Saved /path/to/hello.txt
```

### 示例2: 编辑现有文件

```
> load existing.txt
Loaded /path/to/existing.txt

[/path/to/existing.txt]> show
1: The quick brown fox
2: jumps over the lazy dog

[/path/to/existing.txt]> insert 1:5 "very "
Text inserted

[/path/to/existing.txt]> show
1: The very quick brown fox
2: jumps over the lazy dog

[/path/to/existing.txt]> save
Saved /path/to/existing.txt
```

### 示例3: 使用撤销/重做

```
> load test.txt
Loaded /path/to/test.txt (new file)

[/path/to/test.txt]> append "Line 1"
Text appended

[/path/to/test.txt]> append "Line 2"
Text appended

[/path/to/test.txt]> show
1: Line 1
2: Line 2

[/path/to/test.txt]> undo
Undo successful

[/path/to/test.txt]> show
1: Line 1

[/path/to/test.txt]> redo
Redo successful

[/path/to/test.txt]> show
1: Line 1
2: Line 2
```

### 示例4: 多文件工作

```
> load file1.txt
Loaded /path/to/file1.txt (new file)

[/path/to/file1.txt]> append "Content for file 1"
Text appended

[/path/to/file1.txt]> load file2.txt
Loaded /path/to/file2.txt (new file)

[/path/to/file2.txt]> append "Content for file 2"
Text appended

[/path/to/file2.txt]> editor-list
 /path/to/file1.txt*
>/path/to/file2.txt*

[/path/to/file2.txt]> edit file1.txt
Switched to /path/to/file1.txt

[/path/to/file1.txt]> save all
Saved 2 file(s)
```

### 示例5: 使用日志功能

```
> init logfile.txt with-log
Created new buffer: logfile.txt

[/path/to/logfile.txt]> show
1: # log

[/path/to/logfile.txt]> append "Test content"
Text appended

[/path/to/logfile.txt]> save
Saved /path/to/logfile.txt

[/path/to/logfile.txt]> log-show
session start at 20251028 12:00:00
20251028 12:00:05 init logfile.txt with-log
20251028 12:00:10 append "Test content"
20251028 12:00:15 save
```

## 注意事项

1. **文件编码**: 所有文件使用UTF-8编码
2. **行号和列号**: 都从1开始计数
3. **引号**: 文本参数需要用双引号包裹
4. **日志文件**: 保存在与源文件相同的目录，文件名为 `.filename.log`
5. **工作区状态**: 保存在 `.editorWorkspace` 文件中
6. **自动日志**: 文件第一行为 `#log` 或 `# log` 时自动启用日志

## 故障排除

### 问题1: 无法保存文件
**原因**: 目录不存在或没有写权限
**解决**: 确保目录存在且有写权限

### 问题2: 日志未记录
**原因**: 日志未启用
**解决**: 使用 `log-on` 命令启用日志

### 问题3: 撤销/重做无效
**原因**: 没有可撤销/重做的操作
**解决**: 只有修改文件内容的操作才能撤销

### 问题4: 命令无法识别
**原因**: 命令拼写错误或格式不正确
**解决**: 检查命令拼写和参数格式

## 技术支持

如有问题，请查看：
1. 架构文档: `docs/architecture.md`
2. 测试文档: `docs/test_documentation.md`
3. 源代码注释
