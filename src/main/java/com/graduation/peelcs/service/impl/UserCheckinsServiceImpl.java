package com.graduation.peelcs.service.impl;

import com.graduation.peelcs.domain.po.UserCheckins;
import com.graduation.peelcs.mapper.UserCheckinsMapper;
import com.graduation.peelcs.service.IUserCheckinsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户签到记录表 服务实现类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Service
public class UserCheckinsServiceImpl extends ServiceImpl<UserCheckinsMapper, UserCheckins> implements IUserCheckinsService {

}
