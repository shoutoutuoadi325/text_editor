package com.texteditor.statistics;

/**
 * 会话统计模块，跟踪每个文件的编辑时长。
 * 
 * 时长计算规则：
 * - 开始计时：当文件成为活动文件时（通过 load 或 edit 命令）
 * - 停止计时：当切换到其他文件、关闭文件或退出程序时
 * - 累计时长：一个会话中，文件每次成为活动文件都会累计时长
 * - 重置时长：文件关闭后，如果再次打开，时长重置为 0
 */

import java.util.HashMap;
import java.util.Map;

/**
 * 会话统计管理器，使用观察者模式监听文件切换事件。
 */
public class SessionStatistics {
    
    // 每个文件的累计编辑时长（毫秒）
    private final Map<String, Long> editingTime;
    
    // 当前活动文件的开始计时时间点
    private String currentFile;
    private long startTime;

    public SessionStatistics() {
        this.editingTime = new HashMap<>();
        this.currentFile = null;
        this.startTime = 0;
    }

    /**
     * 当文件成为活动文件时调用。
     * @param filePath 文件路径
     */
    public void onFileActivated(String filePath) {
        // 先停止当前文件的计时
        stopCurrentTiming();
        
        // 开始新文件的计时
        currentFile = filePath;
        startTime = System.currentTimeMillis();
        
        // 确保文件在统计中存在
        editingTime.putIfAbsent(filePath, 0L);
    }

    /**
     * 当文件被关闭时调用。
     * @param filePath 文件路径
     */
    public void onFileClosed(String filePath) {
        if (filePath.equals(currentFile)) {
            stopCurrentTiming();
            currentFile = null;
        }
        // 从统计中移除（下次打开时重置为 0）
        editingTime.remove(filePath);
    }

    /**
     * 当程序退出时调用，停止所有计时。
     */
    public void onExit() {
        stopCurrentTiming();
    }

    /**
     * 停止当前文件的计时，累加到总时长。
     */
    private void stopCurrentTiming() {
        if (currentFile != null && startTime > 0) {
            long elapsed = System.currentTimeMillis() - startTime;
            long total = editingTime.getOrDefault(currentFile, 0L);
            editingTime.put(currentFile, total + elapsed);
            startTime = 0;
        }
    }

    /**
     * 获取文件的编辑时长（毫秒）。
     * @param filePath 文件路径
     * @return 编辑时长（毫秒）
     */
    public long getEditingTimeMs(String filePath) {
        // 如果是当前活动文件，需要加上当前正在计时的时间
        long total = editingTime.getOrDefault(filePath, 0L);
        if (filePath.equals(currentFile) && startTime > 0) {
            total += System.currentTimeMillis() - startTime;
        }
        return total;
    }

    /**
     * 获取格式化的编辑时长字符串。
     * 
     * 时长格式规范：
     * - < 1分钟：X秒
     * - 1-59分钟：X分钟
     * - 1-23小时：X小时Y分钟
     * - >= 24小时：X天Y小时
     * 
     * @param filePath 文件路径
     * @return 格式化的时长字符串
     */
    public String getFormattedEditingTime(String filePath) {
        long ms = getEditingTimeMs(filePath);
        return formatDuration(ms);
    }

    /**
     * 格式化时长。
     * @param milliseconds 毫秒数
     * @return 格式化字符串
     */
    public static String formatDuration(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days >= 1) {
            // >= 24 小时：X天Y小时
            long remainingHours = hours % 24;
            return days + "天" + remainingHours + "小时";
        } else if (hours >= 1) {
            // 1-23小时：X小时Y分钟
            long remainingMinutes = minutes % 60;
            return hours + "小时" + remainingMinutes + "分钟";
        } else if (minutes >= 1) {
            // 1-59分钟：X分钟
            return minutes + "分钟";
        } else {
            // < 1分钟：X秒
            return seconds + "秒";
        }
    }

    /**
     * 检查文件是否有统计记录。
     */
    public boolean hasStatistics(String filePath) {
        return editingTime.containsKey(filePath) || filePath.equals(currentFile);
    }

    /**
     * 初始化文件的统计（用于新打开的文件）。
     */
    public void initFileStatistics(String filePath) {
        editingTime.putIfAbsent(filePath, 0L);
    }
}
