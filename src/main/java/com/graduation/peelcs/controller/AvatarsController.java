package com.graduation.peelcs.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.graduation.peelcs.commen.Result;
import com.graduation.peelcs.domain.po.Avatars;
import com.graduation.peelcs.domain.po.Users;
import com.graduation.peelcs.domain.vo.AvatarVO;
import com.graduation.peelcs.service.IAvatarsService;
import com.graduation.peelcs.service.IUsersService;
import com.graduation.peelcs.service.IUserAvatarUnlocksService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 用户头像资源表，包含可选头像及解锁条件 前端控制器
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Slf4j
@RestController
@RequestMapping("/avatars")
@RequiredArgsConstructor
public class AvatarsController {

    private final IAvatarsService avatarsService;
    private final IUsersService usersService;
    private final IUserAvatarUnlocksService userAvatarUnlocksService;
    
    /**
     * 上传新头像（管理员接口）
     * 
     * @param file 头像文件
     * @param name 头像名称
     * @param pointsRequired 所需积分
     * @param isDefault 是否默认头像
     * @return 上传结果
     */
    @PostMapping("/upload")
    @SaCheckRole("admin")
    public Result<Avatars> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam("pointsRequired") Integer pointsRequired,
            @RequestParam(value = "isDefault", required = false) Boolean isDefault) {
        try {
            Avatars avatar = avatarsService.uploadAvatar(file, name, pointsRequired, isDefault);
            return Result.success("头像上传成功", avatar);
        } catch (Exception e) {
            log.error("头像上传失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有头像列表（管理员接口）
     * 
     * @return 头像列表
     */
    @GetMapping("/list")
    @SaCheckRole("admin")
    public Result<List<Avatars>> getAllAvatars() {
        try {
            List<Avatars> avatars = avatarsService.getAllAvatars();
            return Result.success(avatars);
        } catch (Exception e) {
            log.error("获取头像列表失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户可用的头像列表（包含解锁状态）
     * 
     * @return 可用头像列表（包含解锁状态）
     */
    @GetMapping("/user-avatars")
    public Result<List<AvatarVO>> getUserAvatars() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            List<AvatarVO> avatars = avatarsService.getAvatarsWithUnlockStatus(userId);
            return Result.success(avatars);
        } catch (Exception e) {
            log.error("获取用户头像列表失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除头像（管理员接口）
     * 
     * @param id 头像ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @SaCheckRole("admin")
    public Result<Void> deleteAvatar(@PathVariable Long id) {
        try {
            boolean success = avatarsService.deleteAvatar(id);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除失败，头像不存在");
            }
        } catch (Exception e) {
            log.error("删除头像失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户兑换头像
     * 
     * @param avatarId 头像ID
     * @return 兑换结果
     */
    @PostMapping("/exchange/{avatarId}")
    public Result<Users> exchangeAvatar(@PathVariable Long avatarId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 查询头像信息
            Avatars avatar = avatarsService.getAvatarById(avatarId);
            if (avatar == null) {
                return Result.error("头像不存在");
            }

            // 获取用户信息
            Users user = usersService.getUserById(userId);
            
            // 检查积分是否足够
            if (user.getPoints() < avatar.getPointsRequired()) {
                return Result.error("积分不足，无法兑换该头像");
            }
            
            // 记录用户解锁头像
            userAvatarUnlocksService.recordAvatarUnlock(userId, avatarId);
            
            // 执行兑换
            Users updatedUser = usersService.exchangeAvatar(userId, avatarId);
            
            return Result.success("头像兑换成功", updatedUser);
        } catch (Exception e) {
            log.error("头像兑换失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}
