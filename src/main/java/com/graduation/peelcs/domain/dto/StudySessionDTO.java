package com.graduation.peelcs.domain.dto;

import lombok.Data;

/**
 * 学习记录DTO
 */
@Data
public class StudySessionDTO {
    
    /**
     * 学习记录ID（新增时为null）
     */
    private Long id;
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 会话类型:work/break
     */
    private String sessionType;
    
    /**
     * 持续时间(分钟)
     */
    private Integer durationMinutes;
} 