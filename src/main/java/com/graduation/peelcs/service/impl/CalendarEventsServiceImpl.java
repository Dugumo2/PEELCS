package com.graduation.peelcs.service.impl;

import com.graduation.peelcs.domain.po.CalendarEvents;
import com.graduation.peelcs.mapper.CalendarEventsMapper;
import com.graduation.peelcs.service.ICalendarEventsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 日历事件表 服务实现类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Service
public class CalendarEventsServiceImpl extends ServiceImpl<CalendarEventsMapper, CalendarEvents> implements ICalendarEventsService {

}
