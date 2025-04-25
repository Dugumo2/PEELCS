package com.graduation.peelcs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.graduation.peelcs.domain.po.Avatars;
import com.graduation.peelcs.domain.po.UserAvatarUnlocks;
import com.graduation.peelcs.domain.po.Users;
import com.graduation.peelcs.domain.vo.AvatarVO;
import com.graduation.peelcs.mapper.AvatarsMapper;
import com.graduation.peelcs.service.IAvatarsService;
import com.graduation.peelcs.service.IUserAvatarUnlocksService;
import com.graduation.peelcs.utils.oss.AliyunOSSUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户头像资源表，包含可选头像及解锁条件 服务实现类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AvatarsServiceImpl extends ServiceImpl<AvatarsMapper, Avatars> implements IAvatarsService {

    private final IUserAvatarUnlocksService userAvatarUnlocksService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Avatars uploadAvatar(MultipartFile file, String name, Integer pointsRequired, Boolean isDefault) {
        try {
            // 参数验证
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("头像文件不能为空");
            }
            
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("头像名称不能为空");
            }
            
            if (pointsRequired == null || pointsRequired < 0) {
                throw new IllegalArgumentException("所需积分不能为负数");
            }
            
            // 使用AliyunOSSUtil上传文件
            String imageUrl = AliyunOSSUtil.uploadFile(file);
            
            // 创建头像记录
            Avatars avatar = new Avatars();
            avatar.setName(name);
            avatar.setImageUrl(imageUrl);
            avatar.setPointsRequired(pointsRequired);
            avatar.setIsDefault(isDefault != null && isDefault);
            avatar.setCreatedAt(LocalDateTime.now());
            
            // 保存到数据库
            save(avatar);
            
            return avatar;
        } catch (Exception e) {
            log.error("上传头像失败: {}", e.getMessage(), e);
            throw new RuntimeException("上传头像失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Avatars> getAllAvatars() {
        LambdaQueryWrapper<Avatars> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Avatars::getPointsRequired);
        return list(wrapper);
    }

    @Override
    public List<Avatars> getAvailableAvatars(Integer userPoints) {
        if (userPoints == null) {
            userPoints = 0;
        }
        
        LambdaQueryWrapper<Avatars> wrapper = new LambdaQueryWrapper<>();
        wrapper.le(Avatars::getPointsRequired, userPoints)
               .orderByAsc(Avatars::getPointsRequired);
        
        return list(wrapper);
    }

    @Override
    public Avatars getAvatarById(Long id) {
        if (id == null) {
            return null;
        }
        return getById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAvatar(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("头像ID不能为空");
        }
        
        // 检查是否是默认头像
        Avatars avatar = getById(id);
        if (avatar == null) {
            return false;
        }
        
        if (avatar.getIsDefault()) {
            throw new IllegalArgumentException("默认头像不能删除");
        }
        
        // 删除OSS中的头像文件
        try {
            String imageUrl = avatar.getImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                boolean deleteSuccess = AliyunOSSUtil.deleteFile(imageUrl);
                if (!deleteSuccess) {
                    log.warn("OSS中的头像文件删除失败: {}", imageUrl);
                }
            }
        } catch (Exception e) {
            log.error("删除OSS中的头像文件时发生异常: {}", e.getMessage(), e);
        }
        
        // 删除数据库中的记录
        return removeById(id);
    }

    @Override
    public List<AvatarVO> getAvatarsWithUnlockStatus(Long userId) {
        // 获取所有头像
        List<Avatars> allAvatars = getAllAvatars();
        
        // 获取用户已解锁的头像ID集合
        List<UserAvatarUnlocks> userUnlocks = userAvatarUnlocksService.getUserUnlockedAvatars(userId);
        Set<Long> unlockedAvatarIds = userUnlocks.stream()
            .map(UserAvatarUnlocks::getAvatarId)
            .collect(Collectors.toSet());
        
        // 转换为VO对象，并设置解锁状态
        return allAvatars.stream().map(avatar -> {
            AvatarVO avatarVO = new AvatarVO()
                .setId(avatar.getId())
                .setName(avatar.getName())
                .setImageUrl(avatar.getImageUrl())
                .setPointsRequired(avatar.getPointsRequired())
                .setIsDefault(avatar.getIsDefault());
            
            // 只标记已解锁的头像
            boolean unlocked = unlockedAvatarIds.contains(avatar.getId()) || 
                              avatar.getIsDefault(); // 默认头像也算作已解锁
            
            // 设置是否解锁
            avatarVO.setIsUnlocked(unlocked);
            
            return avatarVO;
        }).collect(Collectors.toList());
    }
}
