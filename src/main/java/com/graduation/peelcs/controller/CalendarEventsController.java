package com.graduation.peelcs.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.graduation.peelcs.commen.Constant;
import com.graduation.peelcs.commen.Result;
import com.graduation.peelcs.domain.dto.CalendarEventDTO;
import com.graduation.peelcs.domain.po.CalendarEvents;
import com.graduation.peelcs.domain.vo.CalendarEventVO;
import com.graduation.peelcs.service.ICalendarEventsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 日历事件表 前端控制器
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Slf4j
@RestController
@RequestMapping("/calendar-events")
@RequiredArgsConstructor
public class CalendarEventsController {

    private final ICalendarEventsService calendarEventsService;
    
    /**
     * 添加课程
     * @param eventDTO 课程信息
     * @return 添加结果
     */
    @PostMapping("/class")
    @SaCheckLogin
    public Result<CalendarEventVO> addClass(@RequestBody @Validated CalendarEventDTO eventDTO) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 检查时间冲突（传递课程类型和重复周数）
            List<CalendarEventVO> conflicts = calendarEventsService.checkTimeConflict(
                userId,
                eventDTO.getStartTime(),
                eventDTO.getEndTime(),
                eventDTO.getId(),
                eventDTO.getRepeatWeeks(),
                Constant.EventType.CLASS
            );
            
            if (!conflicts.isEmpty()) {
                return Result.error("存在时间冲突，请调整时间");
            }
            
            CalendarEvents event = calendarEventsService.addClass(
                userId,
                eventDTO.getTitle(),
                eventDTO.getDescription(),
                eventDTO.getLocation(),
                eventDTO.getStartTime(),
                eventDTO.getEndTime(),
                eventDTO.getRepeatWeeks()
            );
            
            CalendarEventVO vo = new CalendarEventVO()
                .setId(event.getId())
                .setUserId(event.getUserId())
                .setTitle(event.getTitle())
                .setDescription(event.getDescription())
                .setLocation(event.getLocation())
                .setStartTime(event.getStartTime())
                .setEndTime(event.getEndTime())
                .setEventType(event.getEventType())
                .setRepeatWeeks(event.getRepeatWeeks())
                .setCreatedAt(event.getCreatedAt())
                .setUpdatedAt(event.getUpdatedAt());
            
            return Result.success("添加课程成功", vo);
        } catch (IllegalArgumentException e) {
            log.error("添加课程失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("添加课程异常: ", e);
            return Result.error("系统错误，请稍后重试");
        }
    }
    
    /**
     * 添加日程
     * @param eventDTO 日程信息
     * @return 添加结果
     */
    @PostMapping("/schedule")
    public Result<CalendarEventVO> addSchedule(@RequestBody @Validated CalendarEventDTO eventDTO) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 检查时间冲突（传递日程类型，不重复）
            List<CalendarEventVO> conflicts = calendarEventsService.checkTimeConflict(
                userId,
                eventDTO.getStartTime(),
                eventDTO.getEndTime(),
                eventDTO.getId(),
                1,  // 日程不重复
                Constant.EventType.SCHEDULE
            );
            
            if (!conflicts.isEmpty()) {
                return Result.error("存在时间冲突，请调整时间");
            }
            
            CalendarEvents event = calendarEventsService.addSchedule(
                userId,
                eventDTO.getTitle(),
                eventDTO.getDescription(),
                eventDTO.getLocation(),
                eventDTO.getStartTime(),
                eventDTO.getEndTime()
            );
            
            CalendarEventVO vo = new CalendarEventVO()
                .setId(event.getId())
                .setUserId(event.getUserId())
                .setTitle(event.getTitle())
                .setDescription(event.getDescription())
                .setLocation(event.getLocation())
                .setStartTime(event.getStartTime())
                .setEndTime(event.getEndTime())
                .setEventType(event.getEventType())
                .setRepeatWeeks(event.getRepeatWeeks())
                .setCreatedAt(event.getCreatedAt())
                .setUpdatedAt(event.getUpdatedAt());
            
            return Result.success("添加日程成功", vo);
        } catch (IllegalArgumentException e) {
            log.error("添加日程失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("添加日程异常: ", e);
            return Result.error("系统错误，请稍后重试");
        }
    }
    
    /**
     * 更新事件
     * @param id 事件ID
     * @param eventDTO 事件信息
     * @return 更新结果
     */
    @PutMapping("/{id}")
    public Result<CalendarEventVO> updateEvent(@PathVariable Long id, @RequestBody @Validated CalendarEventDTO eventDTO) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 获取事件类型
            CalendarEventVO existingEvent = calendarEventsService.getEventDetail(id, userId);
            if (existingEvent == null) {
                return Result.error("事件不存在或已被删除");
            }
            
            String eventType = existingEvent.getEventType();
            Integer repeatWeeks = Constant.EventType.CLASS.equals(eventType) ? eventDTO.getRepeatWeeks() : 1;
            
            // 检查时间冲突（根据事件类型决定重复周数）
            List<CalendarEventVO> conflicts = calendarEventsService.checkTimeConflict(
                userId,
                eventDTO.getStartTime(),
                eventDTO.getEndTime(),
                id,  // 编辑时排除当前事件
                repeatWeeks,
                eventType
            );
            
            if (!conflicts.isEmpty()) {
                return Result.error("存在时间冲突，请调整时间");
            }
            
            CalendarEvents event = calendarEventsService.updateEvent(
                id,
                userId,
                eventDTO.getTitle(),
                eventDTO.getDescription(),
                eventDTO.getLocation(),
                eventDTO.getStartTime(),
                eventDTO.getEndTime(),
                eventDTO.getRepeatWeeks()
            );
            
            CalendarEventVO vo = new CalendarEventVO()
                .setId(event.getId())
                .setUserId(event.getUserId())
                .setTitle(event.getTitle())
                .setDescription(event.getDescription())
                .setLocation(event.getLocation())
                .setStartTime(event.getStartTime())
                .setEndTime(event.getEndTime())
                .setEventType(event.getEventType())
                .setRepeatWeeks(event.getRepeatWeeks())
                .setCreatedAt(event.getCreatedAt())
                .setUpdatedAt(event.getUpdatedAt());
            
            return Result.success("更新事件成功", vo);
        } catch (IllegalArgumentException e) {
            log.error("更新事件失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新事件异常: ", e);
            return Result.error("系统错误，请稍后重试");
        }
    }
    
    /**
     * 删除事件
     * @param id 事件ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteEvent(@PathVariable Long id) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            boolean success = calendarEventsService.deleteEvent(id, userId);
            
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除事件失败");
            }
        } catch (IllegalArgumentException e) {
            log.error("删除事件失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除事件异常: ", e);
            return Result.error("系统错误，请稍后重试");
        }
    }
    
    /**
     * 获取事件详情
     * @param id 事件ID
     * @return 事件详情
     */
    @GetMapping("/{id}")
    public Result<CalendarEventVO> getEventDetail(@PathVariable Long id) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            CalendarEventVO event = calendarEventsService.getEventDetail(id, userId);
            
            if (event != null) {
                return Result.success(event);
            } else {
                return Result.error("事件不存在");
            }
        } catch (Exception e) {
            log.error("获取事件详情异常: ", e);
            return Result.error("系统错误，请稍后重试");
        }
    }
    
    /**
     * 获取指定时间段内的事件列表
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 事件列表
     */
    @GetMapping("/list")
    public Result<List<CalendarEventVO>> getEvents(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            List<CalendarEventVO> events = calendarEventsService.getUserEvents(userId, startTime, endTime);
            
            return Result.success(events);
        } catch (Exception e) {
            log.error("获取事件列表异常: ", e);
            return Result.error("系统错误，请稍后重试");
        }
    }
    
    /**
     * 获取所有课程
     * @return 课程列表
     */
    @GetMapping("/classes")
    public Result<List<CalendarEventVO>> getClasses() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            List<CalendarEventVO> events = calendarEventsService.getUserClasses(userId);
            
            return Result.success(events);
        } catch (Exception e) {
            log.error("获取课程列表异常: ", e);
            return Result.error("系统错误，请稍后重试");
        }
    }
    
    /**
     * 获取所有日程
     * @return 日程列表
     */
    @GetMapping("/schedules")
    public Result<List<CalendarEventVO>> getSchedules() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            List<CalendarEventVO> events = calendarEventsService.getUserSchedules(userId);
            
            return Result.success(events);
        } catch (Exception e) {
            log.error("获取日程列表异常: ", e);
            return Result.error("系统错误，请稍后重试");
        }
    }
}
