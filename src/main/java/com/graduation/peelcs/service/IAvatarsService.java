package com.graduation.peelcs.service;

import com.graduation.peelcs.domain.po.Avatars;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;
import com.graduation.peelcs.domain.vo.AvatarVO;

import java.util.List;

/**
 * <p>
 * 用户头像资源表，包含可选头像及解锁条件 服务类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
public interface IAvatarsService extends IService<Avatars> {
    
    /**
     * 上传新头像
     * 
     * @param file 头像图片文件
     * @param name 头像名称
     * @param pointsRequired 兑换所需积分
     * @param isDefault 是否默认头像
     * @return 上传后的头像信息
     */
    Avatars uploadAvatar(MultipartFile file, String name, Integer pointsRequired, Boolean isDefault);
    
    /**
     * 获取所有可用的头像列表
     * 
     * @return 头像列表
     */
    List<Avatars> getAllAvatars();
    
    /**
     * 获取用户可兑换的头像列表（根据用户积分）
     * 
     * @param userPoints 用户当前积分
     * @return 可兑换的头像列表
     */
    List<Avatars> getAvailableAvatars(Integer userPoints);
    
    /**
     * 获取用户可用的头像（包含解锁状态）
     *
     * @param userId 用户ID
     * @return 可用头像列表（包含解锁状态）
     */
    List<AvatarVO> getAvatarsWithUnlockStatus(Long userId);
    
    /**
     * 根据ID获取头像信息
     * 
     * @param id 头像ID
     * @return 头像信息
     */
    Avatars getAvatarById(Long id);
    
    /**
     * 删除头像
     * 
     * @param id 头像ID
     * @return 是否删除成功
     */
    boolean deleteAvatar(Long id);
}
