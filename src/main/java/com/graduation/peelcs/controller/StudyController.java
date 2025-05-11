package com.graduation.peelcs.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.graduation.peelcs.commen.Result;
import com.graduation.peelcs.domain.dto.StudySessionDTO;
import com.graduation.peelcs.domain.dto.StudyTaskDTO;
import com.graduation.peelcs.domain.po.StudyTasks;
import com.graduation.peelcs.domain.vo.StudySessionVO;
import com.graduation.peelcs.domain.vo.StudyStatsVO;
import com.graduation.peelcs.domain.vo.StudyTaskVO;
import com.graduation.peelcs.service.IStudySessionsService;
import com.graduation.peelcs.service.IStudyTasksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 学习记录和任务控制类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Slf4j
@RestController
@RequestMapping("/study")
@RequiredArgsConstructor
@SaCheckLogin
public class StudyController {

    private final IStudyTasksService studyTasksService;
    private final IStudySessionsService studySessionsService;

    /**
     * 创建学习任务
     */
    @PostMapping("/tasks")
    public Result<StudyTasks> createTask(@RequestBody StudyTaskDTO taskDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        StudyTasks task = studyTasksService.createTask(userId, taskDTO.getSubject(), taskDTO.getName(), taskDTO.getDurationMinutes());
        return Result.success("创建任务成功", task);
    }

    /**
     * 更新学习任务
     */
    @PutMapping("/tasks/{id}")
    public Result<StudyTasks> updateTask(
            @PathVariable Long id,
            @RequestBody StudyTaskDTO taskDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        StudyTasks task = studyTasksService.updateTask(id, userId, taskDTO.getSubject(), taskDTO.getName(), taskDTO.getDurationMinutes());
        return Result.success("更新任务成功", task);
    }

    /**
     * 删除学习任务
     */
    @DeleteMapping("/tasks/{id}")
    public Result<Void> deleteTask(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = studyTasksService.deleteTask(id, userId);
        if (result) {
            return Result.success();
        } else {
            return Result.error("删除失败");
        }
    }

    /**
     * 获取任务列表
     */
    @GetMapping("/tasks")
    public Result<List<StudyTaskVO>> getTasks() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<StudyTaskVO> tasks = studyTasksService.getUserTasks(userId);
        return Result.success(tasks);
    }

    /**
     * 获取任务详情
     */
    @Deprecated
    @GetMapping("/tasks/{id}")
    public Result<StudyTaskVO> getTaskDetail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        StudyTaskVO task = studyTasksService.getTaskDetail(id, userId);
        if (task != null) {
            return Result.success(task);
        } else {
            return Result.error("任务不存在");
        }
    }
    
    /**
     * 获取学习统计信息
     */
    @GetMapping("/stats")
    public Result<StudyStatsVO> getStudyStats() {
        Long userId = StpUtil.getLoginIdAsLong();
        StudyStatsVO stats = studyTasksService.getUserStudyStats(userId);
        return Result.success(stats);
    }
    
    /**
     * 开始番茄钟
     */
    @PostMapping("/sessions/start")
    public Result<StudySessionVO> startPomodoro(@RequestBody StudySessionDTO sessionDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        StudySessionVO session = studySessionsService.startPomodoro(
                userId, 
                sessionDTO.getTaskId(), 
                sessionDTO.getSessionType(), 
                sessionDTO.getDurationMinutes());
        return Result.success("番茄钟开始计时", session);
    }
    
    /**
     * 暂停番茄钟
     */
    @PostMapping("/sessions/{id}/pause")
    public Result<StudySessionVO> pausePomodoro(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        StudySessionVO session = studySessionsService.pausePomodoro(id, userId);
        return Result.success("番茄钟已暂停", session);
    }
    
    /**
     * 继续番茄钟
     */
    @PostMapping("/sessions/{id}/resume")
    public Result<StudySessionVO> resumePomodoro(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        StudySessionVO session = studySessionsService.resumePomodoro(id, userId);
        return Result.success("番茄钟已继续", session);
    }
    
    /**
     * 完成番茄钟
     */
    @PostMapping("/sessions/{id}/complete")
    public Result<StudySessionVO> completePomodoro(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        StudySessionVO session = studySessionsService.completePomodoro(id, userId);
        return Result.success("番茄钟已完成", session);
    }
    
    /**
     * 获取当前进行中的番茄钟
     */
    @GetMapping("/sessions/current")
    public Result<StudySessionVO> getCurrentSession() {
        Long userId = StpUtil.getLoginIdAsLong();
        StudySessionVO session = studySessionsService.getCurrentSession(userId);
        if (session != null) {
            return Result.success(session);
        } else {
            return Result.error("当前没有进行中的番茄钟");
        }
    }
    
    /**
     * 获取用户的学习记录列表
     */
    @GetMapping("/sessions")
    public Result<List<StudySessionVO>> getUserSessions(@RequestParam(required = false) Long taskId) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<StudySessionVO> sessions = studySessionsService.getUserSessions(userId, taskId);
        return Result.success(sessions);
    }
    
    /**
     * 获取任务的学习记录列表
     */
    @GetMapping("/tasks/{taskId}/sessions")
    public Result<List<StudySessionVO>> getTaskSessions(@PathVariable Long taskId) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<StudySessionVO> sessions = studySessionsService.getTaskSessions(taskId, userId);
        return Result.success(sessions);
    }
    
    /**
     * 获取学习记录详情
     */
    @GetMapping("/sessions/{id}")
    public Result<StudySessionVO> getSessionDetail(@PathVariable Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        StudySessionVO session = studySessionsService.getSessionDetail(id, userId);
        if (session != null) {
            return Result.success(session);
        } else {
            return Result.error("学习记录不存在");
        }
    }
}
