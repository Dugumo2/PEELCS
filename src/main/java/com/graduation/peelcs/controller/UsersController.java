package com.graduation.peelcs.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.graduation.peelcs.commen.Result;
import com.graduation.peelcs.domain.dto.LoginDTO;
import com.graduation.peelcs.domain.dto.UserDTO;
import com.graduation.peelcs.domain.po.Avatars;
import com.graduation.peelcs.domain.po.UserCheckins;
import com.graduation.peelcs.domain.po.Users;
import com.graduation.peelcs.domain.vo.UserVO;
import com.graduation.peelcs.service.IAvatarsService;
import com.graduation.peelcs.service.IUserCheckinsService;
import com.graduation.peelcs.service.IUsersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * 用户信息表 前端控制器
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final IUsersService usersService;
    private final IUserCheckinsService userCheckinsService;
    
    /**
     * 检查用户名或邮箱是否已存在
     * @param nickname 用户名
     * @param email 邮箱
     * @return 是否存在
     */
    @GetMapping("/check-exists")
    public Result<Boolean> checkUserExists(
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String email) {
        boolean exists = usersService.checkUserExists(nickname, email);
        return Result.success(exists);
    }
    
    /**
     * 发送邮箱验证码
     * @param email 邮箱
     * @return 结果
     */
    @GetMapping("/send-code")
    public Result<Void> sendVerificationCode(@RequestParam String email) {
        try {
            usersService.sendVerificationCode(email);
            return Result.success();
        } catch (Exception e) {
            log.error("发送验证码失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户注册
     * @param userDTO 注册请求
     * @return 结果
     */
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody @Validated UserDTO userDTO) {
        try {
            Users user = usersService.register(
                userDTO.getEmail(),
                userDTO.getPassword(),
                userDTO.getNickname(),
                userDTO.getCode()
            );
            
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            
            // 设置头像URL
            if (user.getAvatarId() != null) {
                Avatars avatar = Db.lambdaQuery(Avatars.class).eq(Avatars::getId,user.getAvatarId()).one();
                if (avatar != null) {
                    userVO.setAvatarUrl(avatar.getImageUrl());
                }
            }
            
            return Result.success("注册成功", userVO);
        } catch (Exception e) {
            log.error("注册失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户登录
     * @param loginDTO 登录信息（包含账号和密码）
     * @return 结果
     */
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody LoginDTO loginDTO) {
        try {
            Users user = usersService.login(loginDTO.getAccount(), loginDTO.getPassword());
            
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            userVO.setToken(StpUtil.getTokenValue());
            
            // 设置头像URL
            if (user.getAvatarId() != null) {
                Avatars avatar = Db.lambdaQuery(Avatars.class).eq(Avatars::getId,user.getAvatarId()).one();
                if (avatar != null) {
                    userVO.setAvatarUrl(avatar.getImageUrl());
                }
            }
            
            return Result.success("登录成功", userVO);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取当前用户信息
     * @return 结果
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Users user = usersService.getUserById(userId);
            if (user != null) {
                UserVO userVO = new UserVO();
                BeanUtils.copyProperties(user, userVO);
                
                // 设置头像URL
                if (user.getAvatarId() != null) {
                    Avatars avatar = Db.lambdaQuery(Avatars.class).eq(Avatars::getId,user.getAvatarId()).one();
                    if (avatar != null) {
                        userVO.setAvatarUrl(avatar.getImageUrl());
                    }
                }
                
                return Result.success(userVO);
            } else {
                return Result.error("用户不存在");
            }
        } catch (Exception e) {
            log.error("获取用户信息失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 修改密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 结果
     */
    @PostMapping("/update-password")
    public Result<Void> updatePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean success = usersService.updatePassword(userId, oldPassword, newPassword);
            if (success) {
                return Result.success();
            } else {
                return Result.error("密码修改失败");
            }
        } catch (Exception e) {
            log.error("修改密码失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 退出登录
     * @return 结果
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        try {
            StpUtil.logout();
            return Result.success();
        } catch (Exception e) {
            log.error("退出登录失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户签到
     * @return 签到结果
     */
    @PostMapping("/checkin")
    public Result<Map<String, Object>> checkIn() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 检查今日是否已签到
            if (userCheckinsService.hasCheckedInToday(userId)) {
                return Result.error("今日已签到，无需重复签到");
            }
            
            // 执行签到
            UserCheckins checkin = userCheckinsService.checkIn(userId);
            
            // 获取最新的用户信息（包含更新后的积分）
            Users user = usersService.getUserById(userId);
            
            // 组装返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("points", checkin.getPointsEarned());
            result.put("totalPoints", user.getPoints());
            result.put("checkInDate", checkin.getCheckInDate());
            
            return Result.success("签到成功", result);
        } catch (Exception e) {
            log.error("签到失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 检查今日是否已签到
     * @return 是否已签到
     */
    @GetMapping("/check-today-checkin")
    public Result<Boolean> checkTodayCheckin() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean hasCheckedIn = userCheckinsService.hasCheckedInToday(userId);
            return Result.success(hasCheckedIn);
        } catch (Exception e) {
            log.error("检查签到状态失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更换头像
     * @param avatarId 头像ID
     * @return 更新后的用户信息
     */
    @PostMapping("/change-avatar/{avatarId}")
    public Result<UserVO> changeAvatar(@PathVariable Long avatarId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 调用服务更换头像
            Users updatedUser = usersService.exchangeAvatar(userId, avatarId);
            
            // 转换为VO对象
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(updatedUser, userVO);
            
            // 设置头像URL
            if (updatedUser.getAvatarId() != null) {
                Avatars avatar = Db.lambdaQuery(Avatars.class).eq(Avatars::getId,updatedUser.getAvatarId()).one();
                if (avatar != null) {
                    userVO.setAvatarUrl(avatar.getImageUrl());
                }
            }
            
            return Result.success("头像更换成功", userVO);
        } catch (Exception e) {
            log.error("更换头像失败: {}", e.getMessage(), e);
            return Result.error(e.getMessage());
        }
    }
}
