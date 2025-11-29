package com.texteditor;

import com.texteditor.command.*;
import com.texteditor.editor.Editor;
import com.texteditor.model.Position;
import com.texteditor.util.DirectoryTree;
import com.texteditor.workspace.Workspace;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command processor that parses and executes user commands.
 */
public class CommandProcessor {
    private final Workspace workspace;
    private boolean running;

    public CommandProcessor(Workspace workspace) {
        this.workspace = workspace;
        this.running = true;
    }

    /**
     * Process a single command line.
     */
    public void processCommand(String commandLine) {
        String trimmed = commandLine.trim();
        if (trimmed.isEmpty()) {
            return;
        }

        try {
            String[] parts = parseCommand(trimmed);
            String command = parts[0].toLowerCase();

            switch (command) {
                case "load":
                    handleLoad(parts);
                    break;
                case "save":
                    handleSave(parts);
                    break;
                case "init":
                    handleInit(parts);
                    break;
                case "close":
                    handleClose(parts);
                    break;
                case "edit":
                    handleEdit(parts);
                    break;
                case "editor-list":
                    handleEditorList();
                    break;
                case "dir-tree":
                    handleDirTree(parts);
                    break;
                case "undo":
                    handleUndo();
                    break;
                case "redo":
                    handleRedo();
                    break;
                case "append":
                    handleAppend(parts);
                    break;
                case "insert":
                    handleInsert(parts);
                    break;
                case "delete":
                    handleDelete(parts);
                    break;
                case "replace":
                    handleReplace(parts);
                    break;
                case "show":
                    handleShow(parts);
                    break;
                case "log-on":
                    handleLogOn(parts);
                    break;
                case "log-off":
                    handleLogOff(parts);
                    break;
                case "log-show":
                    handleLogShow(parts);
                    break;
                case "exit":
                    handleExit();
                    break;
                default:
                    System.out.println("未知命令: " + command);
            }
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    /**
     * Parse command line into parts, respecting quoted strings.
     */
    private String[] parseCommand(String commandLine) {
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|(\\S+)");
        Matcher matcher = pattern.matcher(commandLine);
        
        java.util.List<String> parts = new java.util.ArrayList<>();
        while (matcher.find()) {
            if (matcher.group(1) != null) {
                parts.add(matcher.group(1)); // Quoted string
            } else {
                parts.add(matcher.group(2)); // Unquoted word
            }
        }
        
        return parts.toArray(new String[0]);
    }

    private void handleLoad(String[] parts) throws IOException {
        if (parts.length < 2) {
            System.out.println("用法: load <file>");
            return;
        }

        String filePath = parts[1];
        workspace.loadFile(filePath);
        workspace.getLogManager().onCommandExecuted(filePath, "load " + filePath);
        System.out.println("已加载: " + filePath);
    }

    private void handleSave(String[] parts) throws IOException {
        if (parts.length == 1) {
            // Save current file
            Editor editor = workspace.getActiveEditor();
            if (editor == null) {
                System.out.println("没有活动文件");
                return;
            }
            workspace.saveFile(workspace.getActiveFilePath());
            System.out.println("已保存: " + workspace.getActiveFilePath());
        } else if (parts[1].equals("all")) {
            workspace.saveAll();
            System.out.println("已保存所有文件");
        } else {
            // Save specific file
            workspace.saveFile(parts[1]);
            System.out.println("已保存: " + parts[1]);
        }
    }

    private void handleInit(String[] parts) throws IOException {
        if (parts.length < 2) {
            System.out.println("用法: init <file> [with-log]");
            return;
        }

        String filePath = parts[1];
        boolean withLog = parts.length > 2 && parts[2].equals("with-log");
        
        workspace.initFile(filePath, withLog);
        workspace.getLogManager().onCommandExecuted(filePath, 
            "init " + filePath + (withLog ? " with-log" : ""));
        System.out.println("已创建: " + filePath);
    }

    private void handleClose(String[] parts) throws IOException {
        String filePath;
        if (parts.length == 1) {
            filePath = workspace.getActiveFilePath();
            if (filePath == null) {
                System.out.println("没有活动文件");
                return;
            }
        } else {
            filePath = parts[1];
        }

        Editor editor = workspace.getEditor(filePath);
        if (editor == null) {
            System.out.println("文件未打开: " + filePath);
            return;
        }

        if (editor.isModified()) {
            System.out.print("文件已修改，是否保存？(y/n): ");
            Scanner scanner = new Scanner(System.in);
            String response = scanner.nextLine().trim().toLowerCase();
            
            if (response.equals("y")) {
                workspace.saveFile(filePath);
            }
        }

        workspace.closeFile(filePath, false);
        System.out.println("已关闭: " + filePath);
    }

    private void handleEdit(String[] parts) {
        if (parts.length < 2) {
            System.out.println("用法: edit <file>");
            return;
        }

        String filePath = parts[1];
        try {
            workspace.switchToFile(filePath);
            System.out.println("切换到: " + filePath);
        } catch (IllegalArgumentException e) {
            System.out.println("文件未打开: " + filePath);
        }
    }

    private void handleEditorList() {
        if (workspace.getOpenFiles().isEmpty()) {
            System.out.println("没有打开的文件");
            return;
        }

        for (String filePath : workspace.getOpenFiles()) {
            Editor editor = workspace.getEditor(filePath);
            boolean isActive = filePath.equals(workspace.getActiveFilePath());
            boolean isModified = editor.isModified();

            System.out.print(isActive ? ">" : " ");
            System.out.print(filePath);
            System.out.println(isModified ? "*" : "");
        }
    }

    private void handleDirTree(String[] parts) {
        String dirPath = parts.length > 1 ? parts[1] : ".";
        String tree = DirectoryTree.generateTree(dirPath);
        System.out.println(tree);
    }

    private void handleUndo() {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动文件");
            return;
        }

        if (editor.undo()) {
            System.out.println("已撤销");
            workspace.getLogManager().onCommandExecuted(
                workspace.getActiveFilePath(), "undo");
        } else {
            System.out.println("没有可撤销的操作");
        }
    }

    private void handleRedo() {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动文件");
            return;
        }

        if (editor.redo()) {
            System.out.println("已重做");
            workspace.getLogManager().onCommandExecuted(
                workspace.getActiveFilePath(), "redo");
        } else {
            System.out.println("没有可重做的操作");
        }
    }

    private void handleAppend(String[] parts) {
        if (parts.length < 2) {
            System.out.println("用法: append \"text\"");
            return;
        }

        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动文件");
            return;
        }

        String text = parts[1];
        Command command = new AppendCommand(editor, text);
        editor.executeCommand(command);
        
        workspace.getLogManager().onCommandExecuted(
            workspace.getActiveFilePath(), command.getDescription());
        System.out.println("已追加");
    }

    private void handleInsert(String[] parts) {
        if (parts.length < 3) {
            System.out.println("用法: insert <line:col> \"text\"");
            return;
        }

        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动文件");
            return;
        }

        Position pos = parsePosition(parts[1]);
        String text = parts[2];
        
        Command command = new InsertCommand(editor, pos, text);
        editor.executeCommand(command);
        
        workspace.getLogManager().onCommandExecuted(
            workspace.getActiveFilePath(), command.getDescription());
        System.out.println("已插入");
    }

    private void handleDelete(String[] parts) {
        if (parts.length < 3) {
            System.out.println("用法: delete <line:col> <len>");
            return;
        }

        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动文件");
            return;
        }

        Position pos = parsePosition(parts[1]);
        int length = Integer.parseInt(parts[2]);
        
        Command command = new DeleteCommand(editor, pos, length);
        editor.executeCommand(command);
        
        workspace.getLogManager().onCommandExecuted(
            workspace.getActiveFilePath(), command.getDescription());
        System.out.println("已删除");
    }

    private void handleReplace(String[] parts) {
        if (parts.length < 4) {
            System.out.println("用法: replace <line:col> <len> \"text\"");
            return;
        }

        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动文件");
            return;
        }

        Position pos = parsePosition(parts[1]);
        int length = Integer.parseInt(parts[2]);
        String text = parts[3];
        
        Command command = new ReplaceCommand(editor, pos, length, text);
        editor.executeCommand(command);
        
        workspace.getLogManager().onCommandExecuted(
            workspace.getActiveFilePath(), command.getDescription());
        System.out.println("已替换");
    }

    private void handleShow(String[] parts) {
        Editor editor = workspace.getActiveEditor();
        if (editor == null) {
            System.out.println("没有活动文件");
            return;
        }

        if (parts.length == 1) {
            // Show all
            System.out.println(editor.show(-1, -1));
        } else {
            // Parse range
            String[] range = parts[1].split(":");
            int start = Integer.parseInt(range[0]);
            int end = Integer.parseInt(range[1]);
            System.out.println(editor.show(start, end));
        }
    }

    private void handleLogOn(String[] parts) {
        String filePath = parts.length > 1 ? parts[1] : workspace.getActiveFilePath();
        if (filePath == null) {
            System.out.println("没有活动文件");
            return;
        }

        workspace.getLogManager().enableLogging(filePath);
        workspace.getLogManager().logSessionStart(filePath);
        workspace.getLogManager().onCommandExecuted(filePath, 
            "log-on" + (parts.length > 1 ? " " + filePath : ""));
        System.out.println("已启用日志: " + filePath);
    }

    private void handleLogOff(String[] parts) {
        String filePath = parts.length > 1 ? parts[1] : workspace.getActiveFilePath();
        if (filePath == null) {
            System.out.println("没有活动文件");
            return;
        }

        workspace.getLogManager().onCommandExecuted(filePath, 
            "log-off" + (parts.length > 1 ? " " + filePath : ""));
        workspace.getLogManager().disableLogging(filePath);
        System.out.println("已关闭日志: " + filePath);
    }

    private void handleLogShow(String[] parts) {
        String filePath = parts.length > 1 ? parts[1] : workspace.getActiveFilePath();
        if (filePath == null) {
            System.out.println("没有活动文件");
            return;
        }

        String log = workspace.getLogManager().showLog(filePath);
        System.out.println(log);
    }

    private void handleExit() throws IOException {
        // Check for unsaved changes
        if (workspace.hasUnsavedChanges()) {
            Scanner scanner = new Scanner(System.in);
            for (String filePath : workspace.getModifiedFiles()) {
                System.out.print("文件 " + filePath + " 已修改，是否保存？(y/n): ");
                String response = scanner.nextLine().trim().toLowerCase();
                
                if (response.equals("y")) {
                    workspace.saveFile(filePath);
                }
            }
        }

        // Save workspace state
        workspace.saveWorkspaceState();
        running = false;
        System.out.println("再见！");
    }

    /**
     * Parse position string like "1:5" into Position object.
     */
    private Position parsePosition(String posStr) {
        String[] parts = posStr.split(":");
        int line = Integer.parseInt(parts[0]);
        int col = Integer.parseInt(parts[1]);
        return new Position(line, col);
    }

    public boolean isRunning() {
        return running;
    }

    public String getPrompt() {
        String activeFile = workspace.getActiveFilePath();
        if (activeFile != null) {
            return "[" + activeFile + "]> ";
        }
        return "> ";
    }
}
