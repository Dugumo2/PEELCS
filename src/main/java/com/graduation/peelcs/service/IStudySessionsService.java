package com.graduation.peelcs.service;

import com.graduation.peelcs.domain.po.StudySessions;
import com.baomidou.mybatisplus.extension.service.IService;
import com.graduation.peelcs.domain.vo.StudySessionVO;

import java.util.List;

/**
 * <p>
 * 学习记录表 服务类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
public interface IStudySessionsService extends IService<StudySessions> {

    /**
     * 开始番茄钟
     *
     * @param userId 用户ID
     * @param taskId 任务ID
     * @param sessionType 会话类型 (work/break)
     * @param durationMinutes 持续时间(分钟)
     * @return 创建的学习记录
     */
    StudySessionVO startPomodoro(Long userId, Long taskId, String sessionType, Integer durationMinutes);
    
    /**
     * 暂停番茄钟
     *
     * @param id 学习记录ID
     * @param userId 用户ID
     * @return 更新后的学习记录
     */
    StudySessionVO pausePomodoro(Long id, Long userId);
    
    /**
     * 继续番茄钟
     *
     * @param id 学习记录ID
     * @param userId 用户ID
     * @return 更新后的学习记录
     */
    StudySessionVO resumePomodoro(Long id, Long userId);
    
    /**
     * 完成番茄钟
     *
     * @param id 学习记录ID
     * @param userId 用户ID
     * @return 更新后的学习记录
     */
    StudySessionVO completePomodoro(Long id, Long userId);
    
    /**
     * 获取当前进行中的学习记录
     *
     * @param userId 用户ID
     * @return 进行中的学习记录，如果没有则返回null
     */
    StudySessionVO getCurrentSession(Long userId);
    
    /**
     * 获取用户的学习记录列表
     *
     * @param userId 用户ID
     * @param taskId 任务ID，可为null
     * @return 学习记录列表
     */
    List<StudySessionVO> getUserSessions(Long userId, Long taskId);
    
    /**
     * 获取任务的学习记录列表
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 学习记录列表
     */
    List<StudySessionVO> getTaskSessions(Long taskId, Long userId);
    
    /**
     * 获取学习记录详情
     *
     * @param id 学习记录ID
     * @param userId 用户ID
     * @return 学习记录详情
     */
    StudySessionVO getSessionDetail(Long id, Long userId);
}
