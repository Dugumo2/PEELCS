package com.graduation.peelcs.service;

import com.graduation.peelcs.domain.po.StudyTasks;
import com.baomidou.mybatisplus.extension.service.IService;
import com.graduation.peelcs.domain.vo.StudyTaskVO;
import com.graduation.peelcs.domain.vo.StudyStatsVO;

import java.util.List;

/**
 * <p>
 * 学习任务表 服务类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
public interface IStudyTasksService extends IService<StudyTasks> {

    /**
     * 创建学习任务
     *
     * @param userId 用户ID
     * @param subject 科目
     * @param name 任务名称
     * @param durationMinutes 计划时长(分钟)
     * @return 创建的任务
     */
    StudyTasks createTask(Long userId, String subject, String name, Integer durationMinutes);
    
    /**
     * 更新学习任务
     *
     * @param id 任务ID
     * @param userId 用户ID
     * @param subject 科目
     * @param name 任务名称
     * @param durationMinutes 计划时长(分钟)
     * @return 更新的任务
     */
    StudyTasks updateTask(Long id, Long userId, String subject, String name, Integer durationMinutes);
    
    /**
     * 删除学习任务
     *
     * @param id 任务ID
     * @param userId 用户ID
     * @return 是否删除成功
     */
    boolean deleteTask(Long id, Long userId);
    
    /**
     * 获取用户的学习任务列表
     *
     * @param userId 用户ID
     * @return 任务列表
     */
    List<StudyTaskVO> getUserTasks(Long userId);
    
    /**
     * 获取任务详情
     *
     * @param id 任务ID
     * @param userId 用户ID
     * @return 任务详情
     */
    @Deprecated
    StudyTaskVO getTaskDetail(Long id, Long userId);
    
    /**
     * 获取用户学习统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    StudyStatsVO getUserStudyStats(Long userId);
}
