package com.graduation.peelcs.controller;

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
import org.springframework.http.ResponseEntity;
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
public class StudyController {

    private final IStudyTasksService studyTasksService;
    private final IStudySessionsService studySessionsService;

    /**
     * 创建学习任务
     */
    @PostMapping("/tasks")
    public ResponseEntity<StudyTasks> createTask(@RequestAttribute("userId") Long userId, @RequestBody StudyTaskDTO taskDTO) {
        StudyTasks task = studyTasksService.createTask(userId, taskDTO.getSubject(), taskDTO.getName(), taskDTO.getDurationMinutes());
        return ResponseEntity.ok(task);
    }

    /**
     * 更新学习任务
     */
    @PutMapping("/tasks/{id}")
    public ResponseEntity<StudyTasks> updateTask(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id,
            @RequestBody StudyTaskDTO taskDTO) {
        StudyTasks task = studyTasksService.updateTask(id, userId, taskDTO.getSubject(), taskDTO.getName(), taskDTO.getDurationMinutes());
        return ResponseEntity.ok(task);
    }

    /**
     * 删除学习任务
     */
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<String> deleteTask(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        boolean result = studyTasksService.deleteTask(id, userId);
        if (result) {
            return ResponseEntity.ok("删除成功");
        } else {
            return ResponseEntity.badRequest().body("删除失败");
        }
    }

    /**
     * 获取任务列表
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<StudyTaskVO>> getTasks(@RequestAttribute("userId") Long userId) {
        List<StudyTaskVO> tasks = studyTasksService.getUserTasks(userId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/tasks/{id}")
    public ResponseEntity<StudyTaskVO> getTaskDetail(@RequestAttribute("userId") Long userId, @PathVariable Long id) {
        StudyTaskVO task = studyTasksService.getTaskDetail(id, userId);
        if (task != null) {
            return ResponseEntity.ok(task);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取学习统计信息
     */
    @GetMapping("/stats")
    public ResponseEntity<StudyStatsVO> getStudyStats(@RequestAttribute("userId") Long userId) {
        StudyStatsVO stats = studyTasksService.getUserStudyStats(userId);
        return ResponseEntity.ok(stats);
    }
    
    /**
     * 开始番茄钟
     */
    @PostMapping("/sessions/start")
    public ResponseEntity<StudySessionVO> startPomodoro(
            @RequestAttribute("userId") Long userId,
            @RequestBody StudySessionDTO sessionDTO) {
        StudySessionVO session = studySessionsService.startPomodoro(
                userId, 
                sessionDTO.getTaskId(), 
                sessionDTO.getSessionType(), 
                sessionDTO.getDurationMinutes());
        return ResponseEntity.ok(session);
    }
    
    /**
     * 暂停番茄钟
     */
    @PostMapping("/sessions/{id}/pause")
    public ResponseEntity<StudySessionVO> pausePomodoro(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        StudySessionVO session = studySessionsService.pausePomodoro(id, userId);
        return ResponseEntity.ok(session);
    }
    
    /**
     * 继续番茄钟
     */
    @PostMapping("/sessions/{id}/resume")
    public ResponseEntity<StudySessionVO> resumePomodoro(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        StudySessionVO session = studySessionsService.resumePomodoro(id, userId);
        return ResponseEntity.ok(session);
    }
    
    /**
     * 完成番茄钟
     */
    @PostMapping("/sessions/{id}/complete")
    public ResponseEntity<StudySessionVO> completePomodoro(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        StudySessionVO session = studySessionsService.completePomodoro(id, userId);
        return ResponseEntity.ok(session);
    }
    
    /**
     * 获取当前进行中的番茄钟
     */
    @GetMapping("/sessions/current")
    public ResponseEntity<StudySessionVO> getCurrentSession(@RequestAttribute("userId") Long userId) {
        StudySessionVO session = studySessionsService.getCurrentSession(userId);
        if (session != null) {
            return ResponseEntity.ok(session);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * 获取用户的学习记录列表
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<StudySessionVO>> getUserSessions(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) Long taskId) {
        List<StudySessionVO> sessions = studySessionsService.getUserSessions(userId, taskId);
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * 获取任务的学习记录列表
     */
    @GetMapping("/tasks/{taskId}/sessions")
    public ResponseEntity<List<StudySessionVO>> getTaskSessions(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long taskId) {
        List<StudySessionVO> sessions = studySessionsService.getTaskSessions(taskId, userId);
        return ResponseEntity.ok(sessions);
    }
    
    /**
     * 获取学习记录详情
     */
    @GetMapping("/sessions/{id}")
    public ResponseEntity<StudySessionVO> getSessionDetail(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long id) {
        StudySessionVO session = studySessionsService.getSessionDetail(id, userId);
        if (session != null) {
            return ResponseEntity.ok(session);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
