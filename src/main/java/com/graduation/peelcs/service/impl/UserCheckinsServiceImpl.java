package com.graduation.peelcs.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.graduation.peelcs.commen.Constant;
import com.graduation.peelcs.domain.po.UserCheckins;
import com.graduation.peelcs.domain.po.Users;
import com.graduation.peelcs.mapper.UserCheckinsMapper;
import com.graduation.peelcs.service.IUserCheckinsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户签到记录表 服务实现类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Service
@RequiredArgsConstructor
public class UserCheckinsServiceImpl extends ServiceImpl<UserCheckinsMapper, UserCheckins> implements IUserCheckinsService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserCheckins checkIn(Long userId) {
        // 参数验证
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        // 检查今日是否已签到
        if (hasCheckedInToday(userId)) {
            throw new IllegalArgumentException("今日已签到，无需重复签到");
        }
        
        // 创建签到记录
        LocalDate today = LocalDate.now();
        UserCheckins checkin = new UserCheckins()
                .setUserId(userId)
                .setCheckInDate(today)
                .setPointsEarned(Constant.PointsSettings.CHECKIN_POINTS)
                .setCreatedAt(LocalDateTime.now());
        
        // 保存签到记录
        save(checkin);
        
        // 增加用户积分
        Users user = Db.lambdaQuery(Users.class).eq(Users::getId, userId).one();
        if (user != null) {
            user.setPoints(user.getPoints() + Constant.PointsSettings.CHECKIN_POINTS);
            user.setUpdatedAt(LocalDateTime.now());
            Db.lambdaUpdate(Users.class).eq(Users::getId, userId).update(user);
        }
        
        return checkin;
    }

    @Override
    public boolean hasCheckedInToday(Long userId) {
        if (userId == null) {
            return false;
        }
        
        LocalDate today = LocalDate.now();
        return getCheckinByUserAndDate(userId, today) != null;
    }

    @Override
    public UserCheckins getCheckinByUserAndDate(Long userId, LocalDate date) {
        if (userId == null || date == null) {
            return null;
        }
        
        LambdaQueryWrapper<UserCheckins> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserCheckins::getUserId, userId)
                .eq(UserCheckins::getCheckInDate, date);
        
        return getOne(wrapper);
    }
}
