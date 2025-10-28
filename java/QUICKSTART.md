# 快速开始指南

## 安装和运行

### 1. 克隆仓库

```bash
git clone https://github.com/shoutoutuoadi325/text_editor.git
cd text_editor/java
```

### 2. 编译项目

```bash
mvn clean compile
```

### 3. 运行测试

```bash
mvn test
```

### 4. 打包应用

```bash
mvn package
```

### 5. 运行应用

```bash
java -jar target/text-editor-1.0.0.jar
```

## 基本使用示例

### 创建和编辑文件

```
> init hello.txt with-log
已创建: hello.txt

[hello.txt]> append "Hello, World!"
已追加

[hello.txt]> append "This is line 2"
已追加

[hello.txt]> show
1: # log
2: Hello, World!
3: This is line 2
```

### 编辑文本

```
[hello.txt]> insert 2:8 "Beautiful "
已插入

[hello.txt]> show
1: # log
2: Hello, Beautiful World!
3: This is line 2

[hello.txt]> delete 3:9 7
已删除

[hello.txt]> show
1: # log
2: Hello, Beautiful World!
3: This is 2
```

### 撤销和重做

```
[hello.txt]> undo
已撤销

[hello.txt]> show
1: # log
2: Hello, Beautiful World!
3: This is line 2

[hello.txt]> redo
已重做

[hello.txt]> show
1: # log
2: Hello, Beautiful World!
3: This is 2
```

### 保存和查看日志

```
[hello.txt]> save
已保存: hello.txt

[hello.txt]> log-show
session start at 20251028 13:42:48
20251028 13:42:48 init hello.txt with-log
20251028 13:42:48 append "Hello, World!"
20251028 13:42:48 append "This is line 2"
20251028 13:42:48 insert 2:8 "Beautiful "
20251028 13:42:48 delete 3:9 7
20251028 13:42:48 undo
20251028 13:42:48 redo
20251028 13:42:48 save
```

### 多文件管理

```
[hello.txt]> load another.txt
已加载: another.txt

[another.txt]> append "Content for another file"
已追加

[another.txt]> editor-list
 hello.txt
>another.txt*

[another.txt]> edit hello.txt
切换到: hello.txt

[hello.txt]> save all
已保存所有文件
```

### 查看目录结构

```
> dir-tree
.
├── subdir
│   ├── file1.txt
    ├── file2.txt
├── hello.txt
├── another.txt
```

### 退出程序

```
[hello.txt]> exit
再见！
```

## 命令速查表

### 工作区命令

| 命令 | 说明 | 示例 |
|------|------|------|
| load <file> | 加载文件 | load test.txt |
| save [file\|all] | 保存文件 | save 或 save all |
| init <file> [with-log] | 创建新文件 | init test.txt with-log |
| close [file] | 关闭文件 | close test.txt |
| edit <file> | 切换文件 | edit test.txt |
| editor-list | 显示文件列表 | editor-list |
| dir-tree [path] | 显示目录树 | dir-tree |
| undo | 撤销 | undo |
| redo | 重做 | redo |
| exit | 退出 | exit |

### 文本编辑命令

| 命令 | 说明 | 示例 |
|------|------|------|
| append "text" | 追加文本 | append "新行" |
| insert <line:col> "text" | 插入文本 | insert 1:5 "插入" |
| delete <line:col> <len> | 删除字符 | delete 1:3 5 |
| replace <line:col> <len> "text" | 替换文本 | replace 1:1 4 "替换" |
| show [start:end] | 显示内容 | show 或 show 1:3 |

### 日志命令

| 命令 | 说明 | 示例 |
|------|------|------|
| log-on [file] | 启用日志 | log-on |
| log-off [file] | 关闭日志 | log-off |
| log-show [file] | 显示日志 | log-show |

## 注意事项

1. **行号和列号从1开始计数**
2. **带空格的文本需要用双引号包裹**
3. **文件首行为 `# log` 时自动启用日志**
4. **退出前会自动保存工作区状态**
5. **下次启动会恢复上次的工作区**

## 常见问题

### Q: 如何清除工作区状态？

A: 删除 `.editorWorkspace` 文件

### Q: 日志文件在哪里？

A: 日志文件存储在与源文件相同的目录，文件名为 `.文件名.log`

### Q: 如何运行测试？

A: 使用 `mvn test` 命令

### Q: 支持哪些文件编码？

A: 统一使用 UTF-8 编码

## 更多信息

- 架构设计：[java/docs/architecture.md](java/docs/architecture.md)
- 完整README：[java/README.md](java/README.md)
- 实验要求：[Lab1.md](Lab1.md)
