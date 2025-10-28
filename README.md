# 文本编辑器项目

本仓库包含文本编辑器的两个实现版本：

## Java版本 (推荐)

位于 `java/` 目录，这是完整的Lab1实现，包含所有18个命令和完整的测试套件。

**特性：**
- ✅ 18个命令全部实现
- ✅ 36个单元测试，100%通过
- ✅ 应用命令、观察者、备忘录设计模式
- ✅ 完整的文档
- ✅ Java 11, Maven构建

**快速开始：**
```bash
cd java
mvn clean package
java -jar target/text-editor-1.0.0.jar
```

详细信息请查看 [java/README.md](java/README.md)

## Python版本

位于 `python/` 目录，这是早期的Python实现。

**注意：** Python版本不会再进行后续的迭代更新，建议使用Java版本。

详细信息请查看 [python/README.md](python/README.md)

## 项目文档

- [Lab1.md](Lab1.md) - 实验要求文档
- [java/docs/architecture.md](java/docs/architecture.md) - Java版本架构设计文档

## 功能对比

| 功能 | Java版本 | Python版本 |
|------|---------|-----------|
| 工作区命令 | ✅ 10个 | ✅ 10个 |
| 文本编辑命令 | ✅ 5个 | ✅ 5个 |
| 日志命令 | ✅ 3个 | ✅ 3个 |
| 单元测试 | ✅ 36个 | ✅ 67个 |
| 设计模式 | ✅ 完整 | ✅ 完整 |
| 文档 | ✅ 完整 | ✅ 完整 |

## 技术栈

**Java版本：**
- Java 11
- Maven 3.6+
- JUnit 5

**Python版本：**
- Python 3.8+
- pytest

## 开发者

本项目为软件工程课程Lab1实验的完整实现。
