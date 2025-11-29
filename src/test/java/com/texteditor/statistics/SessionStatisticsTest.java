package com.texteditor.statistics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SessionStatistics 的单元测试。
 * 测试编辑时长统计功能。
 */
class SessionStatisticsTest {

    private SessionStatistics statistics;

    @BeforeEach
    void setUp() {
        statistics = new SessionStatistics();
    }

    @Test
    void testInitialState() {
        // 新文件应该没有统计记录
        assertFalse(statistics.hasStatistics("file.txt"));
        assertEquals(0, statistics.getEditingTimeMs("file.txt"));
    }

    @Test
    void testFileActivated() throws InterruptedException {
        statistics.onFileActivated("file.txt");
        
        assertTrue(statistics.hasStatistics("file.txt"));
        
        // 等待一小段时间
        Thread.sleep(50);
        
        long time = statistics.getEditingTimeMs("file.txt");
        assertTrue(time >= 50, "时间应该至少为 50ms，实际: " + time);
    }

    @Test
    void testFileSwitching() throws InterruptedException {
        // 激活文件1
        statistics.onFileActivated("file1.txt");
        Thread.sleep(50);
        
        // 切换到文件2
        statistics.onFileActivated("file2.txt");
        
        // 文件1 的时间应该被累计并停止计时
        long time1 = statistics.getEditingTimeMs("file1.txt");
        assertTrue(time1 >= 50, "file1 时间应该至少为 50ms");
        
        // 等待并验证文件1时间不再增加
        Thread.sleep(30);
        long time1After = statistics.getEditingTimeMs("file1.txt");
        assertEquals(time1, time1After, 5); // 允许小误差
        
        // 文件2 应该在计时
        long time2 = statistics.getEditingTimeMs("file2.txt");
        assertTrue(time2 >= 30, "file2 时间应该至少为 30ms");
    }

    @Test
    void testFileClosed() throws InterruptedException {
        statistics.onFileActivated("file.txt");
        Thread.sleep(50);
        
        statistics.onFileClosed("file.txt");
        
        // 关闭后，统计记录被移除
        assertFalse(statistics.hasStatistics("file.txt"));
        assertEquals(0, statistics.getEditingTimeMs("file.txt"));
    }

    @Test
    void testCumulativeTime() throws InterruptedException {
        // 第一次激活
        statistics.onFileActivated("file.txt");
        Thread.sleep(50);
        
        // 切换走
        statistics.onFileActivated("other.txt");
        long time1 = statistics.getEditingTimeMs("file.txt");
        
        Thread.sleep(30);
        
        // 再次激活
        statistics.onFileActivated("file.txt");
        Thread.sleep(50);
        
        // 时间应该累加
        long totalTime = statistics.getEditingTimeMs("file.txt");
        assertTrue(totalTime >= time1 + 50, "总时间应该累加");
    }

    @Test
    void testOnExit() throws InterruptedException {
        statistics.onFileActivated("file.txt");
        Thread.sleep(50);
        
        statistics.onExit();
        
        // 退出后时间被累计，但不再增加
        long time1 = statistics.getEditingTimeMs("file.txt");
        Thread.sleep(30);
        long time2 = statistics.getEditingTimeMs("file.txt");
        
        assertEquals(time1, time2, 5);
    }

    @Test
    void testFormatDurationSeconds() {
        // < 1分钟 显示秒
        assertEquals("0秒", SessionStatistics.formatDuration(0));
        assertEquals("30秒", SessionStatistics.formatDuration(30 * 1000));
        assertEquals("59秒", SessionStatistics.formatDuration(59 * 1000));
    }

    @Test
    void testFormatDurationMinutes() {
        // 1-59分钟 显示分钟
        assertEquals("1分钟", SessionStatistics.formatDuration(60 * 1000));
        assertEquals("30分钟", SessionStatistics.formatDuration(30 * 60 * 1000));
        assertEquals("59分钟", SessionStatistics.formatDuration(59 * 60 * 1000));
    }

    @Test
    void testFormatDurationHours() {
        // 1-23小时 显示小时和分钟
        assertEquals("1小时0分钟", SessionStatistics.formatDuration(60 * 60 * 1000));
        assertEquals("2小时15分钟", SessionStatistics.formatDuration((2 * 60 + 15) * 60 * 1000));
        assertEquals("23小时59分钟", SessionStatistics.formatDuration((23 * 60 + 59) * 60 * 1000));
    }

    @Test
    void testFormatDurationDays() {
        // >= 24小时 显示天和小时
        assertEquals("1天0小时", SessionStatistics.formatDuration(24 * 60 * 60 * 1000L));
        assertEquals("1天3小时", SessionStatistics.formatDuration(27 * 60 * 60 * 1000L));
        assertEquals("7天12小时", SessionStatistics.formatDuration((7 * 24 + 12) * 60 * 60 * 1000L));
    }

    @Test
    void testInitFileStatistics() {
        statistics.initFileStatistics("file.txt");
        
        assertTrue(statistics.hasStatistics("file.txt"));
        assertEquals(0, statistics.getEditingTimeMs("file.txt"));
    }

    @Test
    void testGetFormattedEditingTime() throws InterruptedException {
        statistics.onFileActivated("file.txt");
        Thread.sleep(50);
        
        String formatted = statistics.getFormattedEditingTime("file.txt");
        
        assertNotNull(formatted);
        assertTrue(formatted.contains("秒") || formatted.contains("分钟"));
    }
}
