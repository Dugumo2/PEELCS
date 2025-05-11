package com.graduation.peelcs.service;

import com.graduation.peelcs.domain.po.CalendarEvents;
import com.baomidou.mybatisplus.extension.service.IService;
import com.graduation.peelcs.domain.vo.CalendarEventVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 日历事件表 服务类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
public interface ICalendarEventsService extends IService<CalendarEvents> {

    /**
     * 添加课程
     * @param userId 用户ID
     * @param title 课程名称
     * @param description 课程描述
     * @param location 地点
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param repeatWeeks 重复周数
     * @return 创建的课程
     */
    CalendarEvents addClass(Long userId, String title, String description, String location, 
                        LocalDateTime startTime, LocalDateTime endTime, Integer repeatWeeks);
    
    /**
     * 添加日程
     * @param userId 用户ID
     * @param title 日程标题
     * @param description 日程描述
     * @param location 地点
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 创建的日程
     */
    CalendarEvents addSchedule(Long userId, String title, String description, String location, 
                          LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 编辑日历事件
     * @param id 事件ID
     * @param userId 用户ID
     * @param title 事件标题
     * @param description 事件描述
     * @param location 地点
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param repeatWeeks 重复周数(仅课程有效)
     * @return 更新后的事件
     */
    CalendarEvents updateEvent(Long id, Long userId, String title, String description, String location, 
                          LocalDateTime startTime, LocalDateTime endTime, Integer repeatWeeks);
    
    /**
     * 删除日历事件
     * @param id 事件ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteEvent(Long id, Long userId);
    
    /**
     * 获取用户的日历事件列表
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 事件列表
     */
    List<CalendarEventVO> getUserEvents(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * 获取用户所有课程
     * @param userId 用户ID
     * @return 课程列表
     */
    List<CalendarEventVO> getUserClasses(Long userId);
    
    /**
     * 获取用户所有日程
     * @param userId 用户ID
     * @return 日程列表
     */
    List<CalendarEventVO> getUserSchedules(Long userId);
    
    /**
     * 获取事件详情
     * @param id 事件ID
     * @param userId 用户ID
     * @return 事件详情
     */
    CalendarEventVO getEventDetail(Long id, Long userId);
    
    /**
     * 检查时间冲突
     * @param userId 用户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param excludeEventId 排除的事件ID(编辑时需要)
     * @param repeatWeeks 重复周数
     * @param eventType 事件类型
     * @return 冲突的事件列表，空列表表示无冲突
     */
    List<CalendarEventVO> checkTimeConflict(Long userId, LocalDateTime startTime, LocalDateTime endTime, 
                                           Long excludeEventId, Integer repeatWeeks, String eventType);
}
