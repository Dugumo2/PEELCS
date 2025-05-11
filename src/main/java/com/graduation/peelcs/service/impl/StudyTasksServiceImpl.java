package com.graduation.peelcs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.graduation.peelcs.commen.Constant;
import com.graduation.peelcs.domain.po.StudySessions;
import com.graduation.peelcs.domain.po.StudyTasks;
import com.graduation.peelcs.domain.vo.StudyStatsVO;
import com.graduation.peelcs.domain.vo.StudyTaskVO;
import com.graduation.peelcs.mapper.StudySessionsMapper;
import com.graduation.peelcs.mapper.StudyTasksMapper;
import com.graduation.peelcs.service.IStudyTasksService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 学习任务表 服务实现类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudyTasksServiceImpl extends ServiceImpl<StudyTasksMapper, StudyTasks> implements IStudyTasksService {

    private final StudySessionsMapper studySessionsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyTasks createTask(Long userId, String subject, String name, Integer durationMinutes) {
        // 参数验证
        if (userId == null || !StringUtils.hasText(subject) || !StringUtils.hasText(name)) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        // 设置默认时长
        if (durationMinutes == null || durationMinutes <= 0) {
            durationMinutes = Constant.PomodoroSettings.DEFAULT_WORK_MINUTES;
        }
        
        // 创建任务
        StudyTasks task = new StudyTasks();
        task.setUserId(userId);
        task.setSubject(subject);
        task.setName(name);
        task.setDurationMinutes(durationMinutes);
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        this.save(task);
        
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudyTasks updateTask(Long id, Long userId, String subject, String name, Integer durationMinutes) {
        // 参数验证
        if (id == null || userId == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        // 获取任务
        StudyTasks task = this.getById(id);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new IllegalArgumentException("任务不存在或无权限");
        }
        
        // 更新任务
        if (StringUtils.hasText(subject)) {
            task.setSubject(subject);
        }
        
        if (StringUtils.hasText(name)) {
            task.setName(name);
        }
        
        if (durationMinutes != null && durationMinutes > 0) {
            task.setDurationMinutes(durationMinutes);
        }
        
        task.setUpdatedAt(LocalDateTime.now());
        
        this.updateById(task);
        
        return task;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTask(Long id, Long userId) {
        if (id == null || userId == null) {
            throw new IllegalArgumentException("参数不完整");
        }
        
        // 获取任务
        StudyTasks task = this.getById(id);
        if (task == null || !task.getUserId().equals(userId)) {
            throw new IllegalArgumentException("任务不存在或无权限");
        }
        
        // 删除任务
        return this.removeById(id);
    }

    @Override
    public List<StudyTaskVO> getUserTasks(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }

        // 查询用户的所有任务
        LambdaQueryWrapper<StudyTasks> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyTasks::getUserId, userId)
                .orderByDesc(StudyTasks::getCreatedAt);

        List<StudyTasks> tasks = this.list(wrapper);

        // 创建一个Map来存储每个任务的统计数据
        Map<Long, TaskStats> taskStatsMap = new HashMap<>();

        // 为每个任务计算统计数据
        for (StudyTasks task : tasks) {
            // 查询任务的学习记录统计
            LambdaQueryWrapper<StudySessions> sessionWrapper = new LambdaQueryWrapper<>();
            sessionWrapper.eq(StudySessions::getTaskId, task.getId())
                    .eq(StudySessions::getUserId, userId)
                    .eq(StudySessions::getState, Constant.SessionState.COMPLETED)
                    .eq(StudySessions::getSessionType, Constant.SessionType.WORK);

            // 计算总学习时间和番茄数
            List<StudySessions> sessions = studySessionsMapper.selectList(sessionWrapper);
            int totalStudyMinutes = sessions.stream()
                    .mapToInt(StudySessions::getActualWorkTime)
                    .sum();

            int completedPomodoros = (int) sessions.stream()
                    .filter(s -> s.getActualWorkTime() >= Constant.PomodoroSettings.DEFAULT_WORK_MINUTES * 0.9) // 完成90%以上视为完成一个番茄
                    .count();

            // 将统计数据存入Map
            taskStatsMap.put(task.getId(), new TaskStats(totalStudyMinutes, completedPomodoros));
        }

        // 转换为VO并设置统计数据
        return tasks.stream()
                .map(task -> {
                    StudyTaskVO vo = convertToVO(task);
                    TaskStats stats = taskStatsMap.get(task.getId());
                    if (stats != null) {
                        vo.setTotalStudyMinutes(stats.getTotalStudyMinutes());
                        vo.setCompletedPomodoros(stats.getCompletedPomodoros());
                    }
                    vo.setIsCompleted(stats.getTotalStudyMinutes() > task.getDurationMinutes());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    // 辅助类来存储任务统计数据
    private static class TaskStats {
        private final int totalStudyMinutes;
        private final int completedPomodoros;

        public TaskStats(int totalStudyMinutes, int completedPomodoros) {
            this.totalStudyMinutes = totalStudyMinutes;
            this.completedPomodoros = completedPomodoros;
        }

        public int getTotalStudyMinutes() {
            return totalStudyMinutes;
        }

        public int getCompletedPomodoros() {
            return completedPomodoros;
        }
    }

    @Deprecated
    @Override
    public StudyTaskVO getTaskDetail(Long id, Long userId) {
        if (id == null || userId == null) {
            return null;
        }
        
        // 查询任务
        LambdaQueryWrapper<StudyTasks> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyTasks::getId, id)
               .eq(StudyTasks::getUserId, userId);
        
        StudyTasks task = this.getOne(wrapper);
        if (task == null) {
            return null;
        }
        
        // 查询任务的学习记录统计
        LambdaQueryWrapper<StudySessions> sessionWrapper = new LambdaQueryWrapper<>();
        sessionWrapper.eq(StudySessions::getTaskId, id)
                      .eq(StudySessions::getUserId, userId)
                      .eq(StudySessions::getState, Constant.SessionState.COMPLETED)
                      .eq(StudySessions::getSessionType, Constant.SessionType.WORK);
        
        // 计算总学习时间和番茄数
        List<StudySessions> sessions = studySessionsMapper.selectList(sessionWrapper);
        int totalStudyMinutes = sessions.stream()
                .mapToInt(StudySessions::getActualWorkTime)
                .sum();
        
        int completedPomodoros = (int) sessions.stream()
                .filter(s -> s.getActualWorkTime() >= Constant.PomodoroSettings.DEFAULT_WORK_MINUTES * 0.9) // 完成90%以上视为完成一个番茄
                .count();
        
        // 转换为VO
        StudyTaskVO vo = convertToVO(task);
        vo.setTotalStudyMinutes(totalStudyMinutes);
        vo.setCompletedPomodoros(completedPomodoros);
        
        return vo;
    }

    @Override
    public StudyStatsVO getUserStudyStats(Long userId) {
        if (userId == null) {
            return new StudyStatsVO();
        }
        
        StudyStatsVO stats = new StudyStatsVO();
        
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime weekStartTime = weekStart.atStartOfDay();
        
        // 查询已完成的工作学习记录
        LambdaQueryWrapper<StudySessions> sessionWrapper = new LambdaQueryWrapper<>();
        sessionWrapper.eq(StudySessions::getUserId, userId)
                      .eq(StudySessions::getState, Constant.SessionState.COMPLETED)
                      .eq(StudySessions::getSessionType, Constant.SessionType.WORK);
        
        List<StudySessions> allSessions = studySessionsMapper.selectList(sessionWrapper);
        
        // 计算总学习时间和番茄数
        int totalStudyMinutes = allSessions.stream()
                .mapToInt(StudySessions::getActualWorkTime)
                .sum();
        
        int completedPomodoros = (int) allSessions.stream()
                .filter(s -> s.getActualWorkTime() >= Constant.PomodoroSettings.DEFAULT_WORK_MINUTES * 0.9)
                .count();
        
        stats.setTotalStudyMinutes(totalStudyMinutes);
        stats.setTotalSessions(allSessions.size());
        stats.setCompletedPomodoros(completedPomodoros);
        
        // 今日学习数据
        List<StudySessions> todaySessions = allSessions.stream()
                .filter(s -> s.getStartTime().isAfter(todayStart))
                .collect(Collectors.toList());
        
        int todayStudyMinutes = todaySessions.stream()
                .mapToInt(StudySessions::getActualWorkTime)
                .sum();
        
        stats.setTodayStudyMinutes(todayStudyMinutes);
        stats.setTodaySessions(todaySessions.size());
        
        // 本周学习数据
        List<StudySessions> weekSessions = allSessions.stream()
                .filter(s -> s.getStartTime().isAfter(weekStartTime))
                .collect(Collectors.toList());
        
        int weekStudyMinutes = weekSessions.stream()
                .mapToInt(StudySessions::getActualWorkTime)
                .sum();
        
        stats.setWeekStudyMinutes(weekStudyMinutes);
        stats.setWeekSessions(weekSessions.size());
        
        // 各科目学习时间分布
        Map<String, Integer> subjectDistribution = new HashMap<>();
        allSessions.forEach(session -> {
            subjectDistribution.put(
                session.getSubject(),
                subjectDistribution.getOrDefault(session.getSubject(), 0) + session.getActualWorkTime()
            );
        });
        
        stats.setSubjectDistribution(subjectDistribution);
        
        // 近7天每日学习时间
        List<StudyStatsVO.DailyStudyVO> dailyStudy = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);
            
            final int day = i;
            int studyMinutes = allSessions.stream()
                    .filter(s -> ChronoUnit.DAYS.between(s.getStartTime().toLocalDate(), today) == day)
                    .mapToInt(StudySessions::getActualWorkTime)
                    .sum();
            
            StudyStatsVO.DailyStudyVO dailyStudyVO = new StudyStatsVO.DailyStudyVO();
            dailyStudyVO.setDate(dateStr);
            dailyStudyVO.setStudyMinutes(studyMinutes);
            
            dailyStudy.add(dailyStudyVO);
        }
        
        stats.setDailyStudy(dailyStudy);
        
        return stats;
    }
    
    /**
     * 将任务转换为VO
     */
    private StudyTaskVO convertToVO(StudyTasks task) {
        if (task == null) {
            return null;
        }
        
        StudyTaskVO vo = new StudyTaskVO();
        BeanUtils.copyProperties(task, vo);
        return vo;
    }
}
