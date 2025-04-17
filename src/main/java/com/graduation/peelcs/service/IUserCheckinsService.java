package com.graduation.peelcs.service;

import com.graduation.peelcs.domain.po.UserCheckins;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;

/**
 * <p>
 * 用户签到记录表 服务类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
public interface IUserCheckinsService extends IService<UserCheckins> {
    
    /**
     * 用户签到
     * 
     * @param userId 用户ID
     * @return 签到记录，如果今日已签到则返回null
     */
    UserCheckins checkIn(Long userId);
    
    /**
     * 检查用户今日是否已签到
     * 
     * @param userId 用户ID
     * @return 是否已签到
     */
    boolean hasCheckedInToday(Long userId);
    
    /**
     * 获取用户指定日期的签到记录
     * 
     * @param userId 用户ID
     * @param date 日期
     * @return 签到记录，如果不存在则返回null
     */
    UserCheckins getCheckinByUserAndDate(Long userId, LocalDate date);
}
