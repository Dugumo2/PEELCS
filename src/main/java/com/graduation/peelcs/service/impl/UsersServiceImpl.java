package com.graduation.peelcs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.graduation.peelcs.commen.Constant;
import com.graduation.peelcs.domain.po.UserCheckins;
import com.graduation.peelcs.domain.po.Users;
import com.graduation.peelcs.domain.po.Avatars;
import com.graduation.peelcs.domain.po.UserAvatarUnlocks;
import com.graduation.peelcs.domain.vo.UserVO;
import com.graduation.peelcs.mapper.UsersMapper;
import com.graduation.peelcs.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.graduation.peelcs.utils.email.EmailUtils;
import com.graduation.peelcs.utils.redis.RedissonService;
import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

    private final EmailUtils emailUtils;
    private final RedissonService redissonService;

    /**
     * 默认头像ID
     */
    private static final Long DEFAULT_AVATAR_ID = 1L;

    /**
     * 默认用户角色
     */
    private static final String DEFAULT_ROLE = "user";

    /**
     * 默认用户状态（激活）
     */
    private static final String DEFAULT_STATUS = "active";

    @Override
    public boolean checkUserExists(String nickname, String email) {
        // 如果两者都为空，则返回false
        if (!StringUtils.hasText(nickname) && !StringUtils.hasText(email)) {
            return false;
        }

        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();

        // 构建查询条件：昵称或邮箱存在即为存在
        if (StringUtils.hasText(nickname)) {
            wrapper.eq(Users::getNickname, nickname);
        }

        if (StringUtils.hasText(email)) {
            // 如果昵称有值，用or，否则直接用eq
            if (StringUtils.hasText(nickname)) {
                wrapper.or().eq(Users::getEmail, email);
            } else {
                wrapper.eq(Users::getEmail, email);
            }
        }

        return this.count(wrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Users register(String email, String password, String nickname, String code) {
        // 验证参数
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password) ||
                !StringUtils.hasText(nickname) || !StringUtils.hasText(code)) {
            throw new IllegalArgumentException("注册信息不完整");
        }

        // 检查用户是否已存在
        if (checkUserExists(nickname, email)) {
            throw new IllegalArgumentException("该用户名或邮箱已被注册");
        }

        // 验证邮箱验证码
        String cachedCode = redissonService.getValue(Constant.RedisKey.HIS_MAIL_CODE + email);
        if (!Objects.equals(cachedCode, code)) {
            throw new IllegalArgumentException("验证码错误或已过期");
        }

        // 创建新用户
        Users user = new Users();
        user.setEmail(email);
        // 密码加密存储
        user.setPassword(BCrypt.hashpw(password));
        user.setNickname(nickname);
        user.setAvatarId(DEFAULT_AVATAR_ID);
        user.setRole(DEFAULT_ROLE);
        user.setStatus(DEFAULT_STATUS);
        user.setPoints(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        // 保存用户
        this.save(user);

        // 清除验证码
        redissonService.remove(Constant.RedisKey.HIS_MAIL_CODE + email);

        return user;
    }

    @Override
    public Users login(String account, String password) {
        if (!StringUtils.hasText(account) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("账号或密码不能为空");
        }

        // 查询用户（支持邮箱或用户名登录）
        LambdaQueryWrapper<Users> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Users::getEmail, account).or().eq(Users::getNickname, account);
        Users user = this.getOne(wrapper);

        if (user == null) {
            throw new IllegalArgumentException("账号不存在");
        }

        // 检查账号状态
        if (!"active".equals(user.getStatus())) {
            throw new IllegalArgumentException("账号已被禁用");
        }

        // 验证密码
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new IllegalArgumentException("密码错误");
        }

        // 登录成功，生成token
        StpUtil.login(user.getId());

        return user;
    }

    @Override
    public void sendVerificationCode(String email) {
        if (!StringUtils.hasText(email)) {
            throw new IllegalArgumentException("邮箱不能为空");
        }

        // 发送验证码
        emailUtils.sendVerificationCode(email);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null || !StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            throw new IllegalArgumentException("参数不完整");
        }

        // 获取用户信息
        Users user = this.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 验证旧密码
        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("原密码错误");
        }

        // 更新密码
        user.setPassword(BCrypt.hashpw(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        return this.updateById(user);
    }

    @Override
    public Users getUserById(Long userId) {
        if (userId == null) {
            return null;
        }
        return this.getById(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Users exchangeAvatar(Long userId, Long avatarId) {
        if (userId == null || avatarId == null) {
            throw new IllegalArgumentException("用户ID或头像ID不能为空");
        }
        
        // 获取用户信息
        Users user = this.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        
        // 获取头像信息和所需积分
        Avatars avatar = Db.lambdaQuery(Avatars.class).eq(Avatars::getId, avatarId).one();
        if (avatar == null) {
            throw new IllegalArgumentException("头像不存在");
        }
        
        // 检查积分是否足够
        if (user.getPoints() < avatar.getPointsRequired()) {
            throw new IllegalArgumentException("积分不足，无法兑换该头像");
        }
        
        // 扣除积分
        user.setPoints(user.getPoints() - avatar.getPointsRequired());
        
        // 记录用户解锁头像（使用Db工具直接操作，避免循环依赖）
        UserAvatarUnlocks unlockRecord = new UserAvatarUnlocks();
        unlockRecord.setUserId(userId);
        unlockRecord.setAvatarId(avatarId);
        unlockRecord.setUnlockedAt(LocalDateTime.now());
        Db.save(unlockRecord);
        
        // 更新用户头像
        user.setAvatarId(avatarId);
        user.setUpdatedAt(LocalDateTime.now());
        
        // 保存更新
        this.updateById(user);
        
        return user;
    }

    @Override
    public UserVO changeAvatar(Long userId, Long avatarId) {
        if (userId == null || avatarId == null) {
            throw new IllegalArgumentException("用户ID或头像ID不能为空");
        }

        // 获取用户信息
        Users user = this.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 获取头像信息
        Avatars avatar = Db.lambdaQuery(Avatars.class).eq(Avatars::getId, avatarId).one();
        if (avatar == null) {
            throw new IllegalArgumentException("头像不存在");
        }

        Long count = Db.lambdaQuery(UserAvatarUnlocks.class).eq(UserAvatarUnlocks::getUserId, userId).eq(UserAvatarUnlocks::getAvatarId, avatarId).count();
        if (count == 0){
            throw new IllegalArgumentException("头像未解锁");
        }

        // 更改用户头像
        user.setAvatarId(avatarId);
        this.updateById(user);

        // 设置返回信息
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setAvatarUrl(avatar.getImageUrl());


        return userVO;
    }
}
