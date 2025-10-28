package com.texteditor.util;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility class for displaying directory tree structure.
 */
public class DirectoryTree {
    
    /**
     * Generate a tree view of the directory structure.
     */
    public static String generateTree(String dirPath) {
        Path path = Paths.get(dirPath);
        if (!Files.exists(path)) {
            return "目录不存在: " + dirPath;
        }

        if (!Files.isDirectory(path)) {
            return "路径不是目录: " + dirPath;
        }

        StringBuilder result = new StringBuilder();
        buildTree(path.toFile(), "", result, true);
        return result.toString();
    }

    /**
     * Recursively build the tree structure.
     */
    private static void buildTree(File file, String prefix, StringBuilder result, boolean isRoot) {
        if (file.isDirectory()) {
            result.append(prefix);
            if (!isRoot) {
                result.append("├── ");
            }
            result.append(file.getName()).append("\n");

            File[] files = file.listFiles();
            if (files != null) {
                // Sort files: directories first, then by name
                Arrays.sort(files, (f1, f2) -> {
                    if (f1.isDirectory() && !f2.isDirectory()) {
                        return -1;
                    } else if (!f1.isDirectory() && f2.isDirectory()) {
                        return 1;
                    }
                    return f1.getName().compareTo(f2.getName());
                });

                for (int i = 0; i < files.length; i++) {
                    boolean isLast = (i == files.length - 1);
                    String newPrefix = prefix + (isRoot ? "" : (isLast ? "    " : "│   "));
                    buildTree(files[i], newPrefix, result, false);
                }
            }
        } else {
            result.append(prefix);
            result.append("├── ");
            result.append(file.getName()).append("\n");
        }
    }
}
