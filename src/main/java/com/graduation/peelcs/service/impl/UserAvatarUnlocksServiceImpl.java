package com.graduation.peelcs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graduation.peelcs.domain.po.UserAvatarUnlocks;
import com.graduation.peelcs.mapper.UserAvatarUnlocksMapper;
import com.graduation.peelcs.service.IUserAvatarUnlocksService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 用户解锁头像记录表 服务实现类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Service
public class UserAvatarUnlocksServiceImpl extends ServiceImpl<UserAvatarUnlocksMapper, UserAvatarUnlocks> implements IUserAvatarUnlocksService {

    @Override
    public List<UserAvatarUnlocks> getUserUnlockedAvatars(Long userId) {
        if (userId == null) {
            return List.of();
        }
        
        LambdaQueryWrapper<UserAvatarUnlocks> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAvatarUnlocks::getUserId, userId);
        return this.list(queryWrapper);
    }

    @Override
    public boolean recordAvatarUnlock(Long userId, Long avatarId) {
        if (userId == null || avatarId == null) {
            return false;
        }
        
        // 检查是否已经解锁
        LambdaQueryWrapper<UserAvatarUnlocks> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAvatarUnlocks::getUserId, userId)
                  .eq(UserAvatarUnlocks::getAvatarId, avatarId);
        if (this.count(queryWrapper) > 0) {
            return true; // 已经解锁过了
        }
        
        // 创建新的解锁记录
        UserAvatarUnlocks unlockRecord = new UserAvatarUnlocks();
        unlockRecord.setUserId(userId);
        unlockRecord.setAvatarId(avatarId);
        unlockRecord.setUnlockedAt(LocalDateTime.now());
        
        return this.save(unlockRecord);
    }
}
