package com.graduation.peelcs.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.graduation.peelcs.domain.po.UserAvatarUnlocks;

import java.util.List;

/**
 * <p>
 * 用户解锁头像记录表 服务接口
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
public interface IUserAvatarUnlocksService extends IService<UserAvatarUnlocks> {
    
    /**
     * 获取用户已解锁的头像记录
     * @param userId 用户ID
     * @return 用户已解锁的头像记录列表
     */
    List<UserAvatarUnlocks> getUserUnlockedAvatars(Long userId);
    
    /**
     * 记录用户解锁头像
     * @param userId 用户ID
     * @param avatarId 头像ID
     * @return 是否成功
     */
    boolean recordAvatarUnlock(Long userId, Long avatarId);
}
