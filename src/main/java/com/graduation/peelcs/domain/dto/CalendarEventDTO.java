package com.graduation.peelcs.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日历事件数据传输对象
 */
@Data
public class CalendarEventDTO {
    
    /**
     * 事件ID（新增时为null）
     */
    private Long id;
    
    /**
     * 事件标题
     */
    private String title;
    
    /**
     * 事件描述
     */
    private String description;
    
    /**
     * 地点
     */
    private String location;
    
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
     * 事件类型:class(课程)/schedule(日程)
     */
    private String eventType;
    
    /**
     * 重复周数(课程)
     */
    private Integer repeatWeeks;
} 