package com.graduation.peelcs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.graduation.peelcs.commen.Constant;
import com.graduation.peelcs.domain.po.CalendarEvents;
import com.graduation.peelcs.domain.vo.CalendarEventVO;
import com.graduation.peelcs.mapper.CalendarEventsMapper;
import com.graduation.peelcs.service.ICalendarEventsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 日历事件表 服务实现类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CalendarEventsServiceImpl extends ServiceImpl<CalendarEventsMapper, CalendarEvents> implements ICalendarEventsService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CalendarEvents addClass(Long userId, String title, String description, String location, 
                              LocalDateTime startTime, LocalDateTime endTime, Integer repeatWeeks) {
        // 参数验证
        if (userId == null || !StringUtils.hasText(title) || startTime == null || endTime == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        
        // 设置默认值
        if (repeatWeeks == null || repeatWeeks < 1) {
            repeatWeeks = 1;
        }
        
        // 创建课程事件
        CalendarEvents event = new CalendarEvents();
        event.setUserId(userId);
        event.setTitle(title);
        event.setDescription(description);
        event.setLocation(location);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setEventType(Constant.EventType.CLASS);
        event.setRepeatWeeks(repeatWeeks);
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        
        this.save(event);
        
        return event;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CalendarEvents addSchedule(Long userId, String title, String description, String location, 
                                 LocalDateTime startTime, LocalDateTime endTime) {
        // 参数验证
        if (userId == null || !StringUtils.hasText(title) || startTime == null || endTime == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        
        // 创建日程事件
        CalendarEvents event = new CalendarEvents();
        event.setUserId(userId);
        event.setTitle(title);
        event.setDescription(description);
        event.setLocation(location);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setEventType(Constant.EventType.SCHEDULE);
        event.setRepeatWeeks(1); // 日程默认不重复
        event.setCreatedAt(LocalDateTime.now());
        event.setUpdatedAt(LocalDateTime.now());
        
        this.save(event);
        
        return event;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CalendarEvents updateEvent(Long id, Long userId, String title, String description, String location, 
                                 LocalDateTime startTime, LocalDateTime endTime, Integer repeatWeeks) {
        // 参数验证
        if (id == null || userId == null || !StringUtils.hasText(title) || startTime == null || endTime == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        if (startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
        
        // 获取原事件
        CalendarEvents event = this.getById(id);
        if (event == null || !event.getUserId().equals(userId)) {
            throw new IllegalArgumentException("事件不存在或无权限");
        }
        
        // 如果是课程类型，设置重复周数
        if (Constant.EventType.CLASS.equals(event.getEventType())) {
            if (repeatWeeks == null || repeatWeeks < 1) {
                repeatWeeks = 1;
            }
            event.setRepeatWeeks(repeatWeeks);
        }
        
        // 更新事件信息
        event.setTitle(title);
        event.setDescription(description);
        event.setLocation(location);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        event.setUpdatedAt(LocalDateTime.now());
        
        this.updateById(event);
        
        return event;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEvent(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        // 获取事件
        CalendarEvents event = this.getById(id);
        if (event == null || !event.getUserId().equals(userId)) {
            throw new IllegalArgumentException("事件不存在或无权限");
        }
        
        return this.removeById(id);
    }

    @Override
    public List<CalendarEventVO> getUserEvents(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        if (startDate == null) {
            startDate = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth());
        }
        
        if (endDate == null) {
            endDate = startDate.plusMonths(1);
        }
        
        // 查询原始事件
        LambdaQueryWrapper<CalendarEvents> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CalendarEvents::getUserId, userId)
               .le(CalendarEvents::getStartTime, endDate)
               .orderByAsc(CalendarEvents::getStartTime);
        
        List<CalendarEvents> events = this.list(wrapper);
        
        // 扩展重复的课程事件
        List<CalendarEventVO> result = new ArrayList<>();
        for (CalendarEvents event : events) {
            List<CalendarEventVO> expandedEvents = expandRecurringEvents(event, startDate, endDate);
            result.addAll(expandedEvents);
        }
        
        return result;
    }

    @Override
    public List<CalendarEventVO> getUserClasses(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        LambdaQueryWrapper<CalendarEvents> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CalendarEvents::getUserId, userId)
               .eq(CalendarEvents::getEventType, Constant.EventType.CLASS)
               .orderByAsc(CalendarEvents::getStartTime);
        
        List<CalendarEvents> events = this.list(wrapper);
        
        return events.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<CalendarEventVO> getUserSchedules(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        LambdaQueryWrapper<CalendarEvents> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CalendarEvents::getUserId, userId)
               .eq(CalendarEvents::getEventType, Constant.EventType.SCHEDULE)
               .orderByAsc(CalendarEvents::getStartTime);
        
        List<CalendarEvents> events = this.list(wrapper);
        
        return events.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public CalendarEventVO getEventDetail(Long id, Long userId) {
        if (id == null || userId == null) {
            return null;
        }
        
        LambdaQueryWrapper<CalendarEvents> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CalendarEvents::getId, id)
               .eq(CalendarEvents::getUserId, userId);
        
        CalendarEvents event = this.getOne(wrapper);
        if (event == null) {
            return null;
        }
        
        return convertToVO(event);
    }

    @Override
    public List<CalendarEventVO> checkTimeConflict(Long userId, LocalDateTime startTime, LocalDateTime endTime, 
                                                  Long excludeEventId, Integer repeatWeeks, String eventType) {
        if (userId == null || startTime == null || endTime == null) {
            return Collections.emptyList();
        }
        
        List<CalendarEventVO> conflicts = new ArrayList<>();
        
        // 查询该用户的所有事件
        LambdaQueryWrapper<CalendarEvents> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CalendarEvents::getUserId, userId);
        
        // 排除指定事件
        if (excludeEventId != null) {
            wrapper.ne(CalendarEvents::getId, excludeEventId);
        }
        
        List<CalendarEvents> events = this.list(wrapper);
        
        // 如果目标事件是重复课程，先展开它的所有周次
        List<CalendarEventVO> targetEvents = new ArrayList<>();
        if (Constant.EventType.CLASS.equals(eventType) && repeatWeeks != null && repeatWeeks > 1) {
            // 创建一个临时事件用于展开
            CalendarEvents tempEvent = new CalendarEvents();
            tempEvent.setStartTime(startTime);
            tempEvent.setEndTime(endTime);
            tempEvent.setEventType(Constant.EventType.CLASS);
            tempEvent.setRepeatWeeks(repeatWeeks);
            
            targetEvents = expandRecurringEvents(tempEvent, startTime, startTime.plusWeeks(repeatWeeks));
        } else {
            // 单次事件，直接添加
            CalendarEventVO singleEvent = new CalendarEventVO();
            singleEvent.setStartTime(startTime);
            singleEvent.setEndTime(endTime);
            targetEvents.add(singleEvent);
        }
        
        // 检查每个目标事件周次与数据库中事件的冲突
        for (CalendarEventVO targetEvent : targetEvents) {
            for (CalendarEvents dbEvent : events) {
                // 对于非重复事件或只重复一周的事件，直接检查
                if (!Constant.EventType.CLASS.equals(dbEvent.getEventType()) || dbEvent.getRepeatWeeks() <= 1) {
                    if (isTimeConflict(dbEvent.getStartTime(), dbEvent.getEndTime(), 
                                      targetEvent.getStartTime(), targetEvent.getEndTime())) {
                        conflicts.add(convertToVO(dbEvent));
                    }
                } else {
                    // 对于重复课程，展开所有周次
                    List<CalendarEventVO> expandedEvents = expandRecurringEvents(dbEvent, 
                        targetEvent.getStartTime().minusWeeks(dbEvent.getRepeatWeeks()), 
                        targetEvent.getEndTime().plusWeeks(dbEvent.getRepeatWeeks()));
                    
                    // 检查每个展开的周次是否与目标事件冲突
                    for (CalendarEventVO expandedEvent : expandedEvents) {
                        if (isTimeConflict(expandedEvent.getStartTime(), expandedEvent.getEndTime(), 
                                          targetEvent.getStartTime(), targetEvent.getEndTime())) {
                            conflicts.add(expandedEvent);
                            break; // 找到一个冲突即可
                        }
                    }
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * 检查两个时间段是否冲突
     */
    private boolean isTimeConflict(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        // 时间段1结束时间在时间段2开始时间之前，或者时间段1开始时间在时间段2结束时间之后，则无冲突
        return !(end1.isBefore(start2) || start1.isAfter(end2));
    }
    
    /**
     * 扩展重复事件
     * 将每周重复的课程事件扩展成多个实际事件
     */
    private List<CalendarEventVO> expandRecurringEvents(CalendarEvents event, LocalDateTime startDate, LocalDateTime endDate) {
        List<CalendarEventVO> result = new ArrayList<>();
        
        // 如果不是课程或不重复，直接返回原事件
        if (!Constant.EventType.CLASS.equals(event.getEventType()) || event.getRepeatWeeks() <= 1) {
            if (event.getStartTime().isBefore(endDate) && event.getEndTime().isAfter(startDate)) {
                result.add(convertToVO(event));
            }
            return result;
        }
        
        // 获取事件的星期几
        DayOfWeek dayOfWeek = event.getStartTime().getDayOfWeek();
        
        // 计算每次重复的时间差（小时和分钟）
        int startHour = event.getStartTime().getHour();
        int startMinute = event.getStartTime().getMinute();
        int endHour = event.getEndTime().getHour();
        int endMinute = event.getEndTime().getMinute();
        
        // 从原始开始日期开始，计算所有重复的日期
        LocalDateTime currentDate = event.getStartTime();
        for (int week = 0; week < event.getRepeatWeeks(); week++) {
            // 计算当前周的日期
            LocalDateTime currentStart = currentDate.plusWeeks(week);
            LocalDateTime currentEnd = event.getEndTime().plusWeeks(week);
            
            // 如果在查询范围内，则添加到结果中
            if (currentStart.isBefore(endDate) && currentEnd.isAfter(startDate)) {
                CalendarEventVO expandedEvent = convertToVO(event);
                expandedEvent.setStartTime(currentStart);
                expandedEvent.setEndTime(currentEnd);
                result.add(expandedEvent);
            }
        }
        
        return result;
    }
    
    /**
     * 将实体对象转换为视图对象
     */
    private CalendarEventVO convertToVO(CalendarEvents event) {
        if (event == null) {
            return null;
        }
        
        CalendarEventVO vo = new CalendarEventVO();
        BeanUtils.copyProperties(event, vo);
        return vo;
    }
}
