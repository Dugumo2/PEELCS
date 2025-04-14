package com.graduation.peelcs.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 学习统计VO
 */
@Data
@Accessors(chain = true)
public class StudyStatsVO {
    
    /**
     * 总学习时间（分钟）
     */
    private Integer totalStudyMinutes;
    
    /**
     * 总专注次数
     */
    private Integer totalSessions;
    
    /**
     * 今日学习时间（分钟）
     */
    private Integer todayStudyMinutes;
    
    /**
     * 今日专注次数
     */
    private Integer todaySessions;
    
    /**
     * 本周学习时间（分钟）
     */
    private Integer weekStudyMinutes;
    
    /**
     * 本周专注次数
     */
    private Integer weekSessions;
    
    /**
     * 完成的番茄数
     */
    private Integer completedPomodoros;
    
    /**
     * 各科目学习时间分布（分钟）
     */
    private Map<String, Integer> subjectDistribution;
    
    /**
     * 近7天每日学习时间（分钟）
     */
    private List<DailyStudyVO> dailyStudy;
    
    @Data
    public static class DailyStudyVO {
        /**
         * 日期(yyyy-MM-dd)
         */
        private String date;
        
        /**
         * 学习时间（分钟）
         */
        private Integer studyMinutes;
    }
} 