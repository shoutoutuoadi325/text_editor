package com.texteditor;

import com.texteditor.logging.LogManager;
import com.texteditor.workspace.Workspace;

import java.util.Scanner;

/**
 * Main entry point for the text editor application.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("欢迎使用文本编辑器");
        System.out.println("输入 'exit' 退出程序");
        System.out.println();

        // Initialize workspace and log manager
        LogManager logManager = new LogManager();
        Workspace workspace = new Workspace(logManager);

        // Try to restore previous workspace state
        workspace.loadWorkspaceState();

        // Create command processor
        CommandProcessor processor = new CommandProcessor(workspace);

        // Main loop
        Scanner scanner = new Scanner(System.in);
        while (processor.isRunning()) {
            System.out.print(processor.getPrompt());
            if (!scanner.hasNextLine()) {
                break;
            }
            String commandLine = scanner.nextLine();
            processor.processCommand(commandLine);
        }

        scanner.close();
    }
}
