package com.graduation.peelcs.domain.dto;

import lombok.Data;

/**
 * 学习任务DTO
 */
@Data
public class StudyTaskDTO {
    
    /**
     * 任务ID（新增时为null）
     */
    private Long id;
    
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
} 