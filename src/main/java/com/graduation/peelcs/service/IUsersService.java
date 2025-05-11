package com.graduation.peelcs.service;

import com.graduation.peelcs.domain.po.Users;
import com.baomidou.mybatisplus.extension.service.IService;
import com.graduation.peelcs.domain.vo.UserVO;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
public interface IUsersService extends IService<Users> {
    
    /**
     * 检查用户是否存在
     * @param nickname 用户名
     * @param email 邮箱
     * @return 是否存在
     */
    boolean checkUserExists(String nickname, String email);
    
    /**
     * 用户注册
     * @param email 邮箱
     * @param password 密码
     * @param nickname 用户名
     * @param code 验证码
     * @return 注册成功的用户对象
     */
    Users register(String email, String password, String nickname, String code);
    
    /**
     * 用户登录
     * @param account 账号（邮箱或用户名）
     * @param password 密码
     * @return 登录成功的用户对象
     */
    Users login(String account, String password);
    
    /**
     * 发送验证码
     * @param email 邮箱
     */
    void sendVerificationCode(String email);
    
    /**
     * 修改密码
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    boolean updatePassword(Long userId, String oldPassword, String newPassword);
    
    /**
     * 根据ID获取用户信息
     * @param userId 用户ID
     * @return 用户对象
     */
    Users getUserById(Long userId);
    
    /**
     * 用户兑换头像
     * @param userId 用户ID
     * @param avatarId 头像ID
     * @return 更新后的用户对象
     */
    Users exchangeAvatar(Long userId, Long avatarId);

    /**
     * 更换用户头像（仅限已解锁的头像，不消耗积分）
     *
     * @param userId 用户ID
     * @param avatarId 头像ID
     * @return 更新后的用户信息
     */
    UserVO changeAvatar(Long userId, Long avatarId);
}
