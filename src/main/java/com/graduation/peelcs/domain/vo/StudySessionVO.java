package com.graduation.peelcs.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 学习记录VO
 */
@Data
@Accessors(chain = true)
public class StudySessionVO {
    
    /**
     * 学习记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 任务ID
     */
    private Long taskId;
    
    /**
     * 科目
     */
    private String subject;
    
    /**
     * 任务名称（从任务中获取）
     */
    private String taskName;
    
    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    /**
     * 持续时间(分钟)
     */
    private Integer durationMinutes;
    
    /**
     * 会话类型:work/break
     */
    private String sessionType;
    
    /**
     * 状态:running/paused/completed
     */
    private String state;
    
    /**
     * 暂停时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime pauseTime;
    
    /**
     * 实际工作时间(分钟)
     */
    private Integer actualWorkTime;
    
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
} 