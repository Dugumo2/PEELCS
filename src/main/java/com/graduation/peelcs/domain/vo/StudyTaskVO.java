package com.graduation.peelcs.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 学习任务VO
 */
@Data
@Accessors(chain = true)
public class StudyTaskVO {
    
    /**
     * 任务ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 科目
     */
    private String subject;
    
    /**
     * 任务名称
     */
    private String name;
    
    /**
     * 计划时长(分钟)
     */
    private Integer durationMinutes;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * 总学习时间（分钟）
     */
    private Integer totalStudyMinutes;
    
    /**
     * 完成的番茄数
     */
    private Integer completedPomodoros;
} 