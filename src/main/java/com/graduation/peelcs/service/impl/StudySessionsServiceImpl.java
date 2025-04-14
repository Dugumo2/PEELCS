package com.graduation.peelcs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.graduation.peelcs.commen.Constant;
import com.graduation.peelcs.domain.po.StudySessions;
import com.graduation.peelcs.domain.po.StudyTasks;
import com.graduation.peelcs.domain.vo.StudySessionVO;
import com.graduation.peelcs.mapper.StudySessionsMapper;
import com.graduation.peelcs.mapper.StudyTasksMapper;
import com.graduation.peelcs.service.IStudySessionsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graduation.peelcs.utils.redis.IRedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 学习记录表 服务实现类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudySessionsServiceImpl extends ServiceImpl<StudySessionsMapper, StudySessions> implements IStudySessionsService {

    private final StudyTasksMapper studyTasksMapper;
    private final IRedisService redissonService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudySessionVO startPomodoro(Long userId, Long taskId, String sessionType, Integer durationMinutes) {
        // 参数验证
        if (userId == null || taskId == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        // 校验任务是否存在
        StudyTasks task = studyTasksMapper.selectById(taskId);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new IllegalArgumentException("学习任务不存在或无权限");
        }
        
        // 检查是否已有进行中的会话
        StudySessionVO existingSession = getCurrentSession(userId);
        if (existingSession != null) {
            throw new IllegalArgumentException("已有进行中的番茄钟，请先完成或取消");
        }
        
        // 设置会话类型
        if (sessionType == null) {
            sessionType = Constant.SessionType.WORK;
        }
        
        // 设置时长
        if (durationMinutes == null || durationMinutes <= 0) {
            if (Constant.SessionType.WORK.equals(sessionType)) {
                durationMinutes = task.getDurationMinutes() != null ? task.getDurationMinutes() : Constant.PomodoroSettings.DEFAULT_WORK_MINUTES;
            } else {
                durationMinutes = Constant.PomodoroSettings.DEFAULT_SHORT_BREAK_MINUTES;
            }
        }
        
        // 创建会话
        StudySessions session = new StudySessions();
        session.setUserId(userId);
        session.setTaskId(taskId);
        session.setSubject(task.getSubject());
        session.setTaskName(task.getName());
        session.setSessionType(sessionType);
        session.setPlannedDurationMinutes(durationMinutes);
        session.setState(Constant.SessionState.RUNNING);
        session.setStartTime(LocalDateTime.now());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setActualWorkTime(0); // 初始实际工作时间为0
        
        this.save(session);
        
        // 将计时器状态存储到Redis
        String timerKey = Constant.RedisKey.POMODORO_TIMER + userId;
        redissonService.setValue(timerKey, session.getId(), Duration.ofHours(3).toMillis()); // 设置3小时过期
        
        return convertToVO(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudySessionVO pausePomodoro(Long id, Long userId) {
        // 参数验证
        if (id == null || userId == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        // 获取会话
        StudySessions session = getAndCheckSession(id, userId);
        
        // 检查状态
        if (!Constant.SessionState.RUNNING.equals(session.getState())) {
            throw new IllegalArgumentException("只有运行中的番茄钟可以暂停");
        }
        
        // 计算已工作时间
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(session.getStartTime(), now);
        int actualWorkTime = session.getActualWorkTime() + (int) duration.toMinutes();
        session.setActualWorkTime(actualWorkTime);
        
        // 更新状态
        session.setState(Constant.SessionState.PAUSED);
        session.setPauseTime(now);
        session.setUpdatedAt(now);
        
        this.updateById(session);
        
        return convertToVO(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudySessionVO resumePomodoro(Long id, Long userId) {
        // 参数验证
        if (id == null || userId == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        // 获取会话
        StudySessions session = getAndCheckSession(id, userId);
        
        // 检查状态
        if (!Constant.SessionState.PAUSED.equals(session.getState())) {
            throw new IllegalArgumentException("只有暂停中的番茄钟可以恢复");
        }
        
        // 更新状态
        session.setState(Constant.SessionState.RUNNING);
        session.setStartTime(LocalDateTime.now()); // 重置开始时间，方便后续计算
        session.setUpdatedAt(LocalDateTime.now());
        
        this.updateById(session);
        
        // 更新Redis中的计时器状态
        String timerKey = Constant.RedisKey.POMODORO_TIMER + userId;
        redissonService.setValue(timerKey, session.getId(), Duration.ofHours(3).toMillis());
        
        return convertToVO(session);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudySessionVO completePomodoro(Long id, Long userId) {
        // 参数验证
        if (id == null || userId == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        // 获取会话
        StudySessions session = getAndCheckSession(id, userId);
        
        // 检查状态，允许从运行中或暂停中完成
        if (Constant.SessionState.COMPLETED.equals(session.getState())) {
            throw new IllegalArgumentException("该番茄钟已完成");
        }
        
        // 计算实际工作时间
        LocalDateTime now = LocalDateTime.now();
        int actualWorkTime = session.getActualWorkTime();
        
        if (Constant.SessionState.RUNNING.equals(session.getState())) {
            Duration duration = Duration.between(session.getStartTime(), now);
            actualWorkTime += (int) duration.toMinutes();
        }
        
        // 更新状态
        session.setState(Constant.SessionState.COMPLETED);
        session.setEndTime(now);
        session.setActualWorkTime(actualWorkTime);
        session.setUpdatedAt(now);
        
        this.updateById(session);
        
        // 清除Redis中的计时器状态
        String timerKey = Constant.RedisKey.POMODORO_TIMER + userId;
        redissonService.remove(timerKey);
        
        return convertToVO(session);
    }

    @Override
    public StudySessionVO getCurrentSession(Long userId) {
        if (userId == null) {
            return null;
        }
        
        // 先从Redis中获取当前会话ID
        String timerKey = Constant.RedisKey.POMODORO_TIMER + userId;
        Long sessionId = redissonService.getValue(timerKey);
        
        // 如果Redis中有记录，直接查询对应会话
        if (sessionId != null) {
            StudySessions session = this.getById(sessionId);
            if (session != null && !Constant.SessionState.COMPLETED.equals(session.getState())) {
                return convertToVO(session);
            } else {
                // 清除无效的Redis记录
                redissonService.remove(timerKey);
            }
        }
        
        // Redis中没有记录或记录无效，从数据库查询未完成的会话
        LambdaQueryWrapper<StudySessions> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudySessions::getUserId, userId)
               .ne(StudySessions::getState, Constant.SessionState.COMPLETED)
               .orderByDesc(StudySessions::getStartTime)
               .last("LIMIT 1");
        
        StudySessions session = this.getOne(wrapper);
        if (session != null) {
            // 更新Redis记录
            redissonService.setValue(timerKey, session.getId(), Duration.ofHours(3).toMillis());
            return convertToVO(session);
        }
        
        return null;
    }

    @Override
    public List<StudySessionVO> getUserSessions(Long userId, Long taskId) {
        if (userId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<StudySessions> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudySessions::getUserId, userId);
        
        if (taskId != null) {
            wrapper.eq(StudySessions::getTaskId, taskId);
        }
        
        wrapper.orderByDesc(StudySessions::getStartTime);
        
        List<StudySessions> sessions = this.list(wrapper);
        
        return sessions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudySessionVO> getTaskSessions(Long taskId, Long userId) {
        if (taskId == null || userId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<StudySessions> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudySessions::getTaskId, taskId)
               .eq(StudySessions::getUserId, userId)
               .orderByDesc(StudySessions::getStartTime);
        
        List<StudySessions> sessions = this.list(wrapper);
        
        return sessions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public StudySessionVO getSessionDetail(Long id, Long userId) {
        if (id == null || userId == null) {
            return null;
        }
        
        LambdaQueryWrapper<StudySessions> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudySessions::getId, id)
               .eq(StudySessions::getUserId, userId);
        
        StudySessions session = this.getOne(wrapper);
        
        return convertToVO(session);
    }
    
    /**
     * 获取并检查会话
     */
    private StudySessions getAndCheckSession(Long id, Long userId) {
        LambdaQueryWrapper<StudySessions> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudySessions::getId, id)
               .eq(StudySessions::getUserId, userId);
        
        StudySessions session = this.getOne(wrapper);
        if (session == null) {
            throw new IllegalArgumentException("番茄钟记录不存在或无权限");
        }
        
        return session;
    }
    
    /**
     * 转换为VO
     */
    private StudySessionVO convertToVO(StudySessions session) {
        if (session == null) {
            return null;
        }
        
        StudySessionVO vo = new StudySessionVO();
        BeanUtils.copyProperties(session, vo);
        // 处理字段名称不一致的问题
        vo.setDurationMinutes(session.getPlannedDurationMinutes());
        return vo;
    }
}
