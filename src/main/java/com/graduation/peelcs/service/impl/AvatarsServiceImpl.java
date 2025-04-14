package com.graduation.peelcs.service.impl;

import com.graduation.peelcs.domain.po.Avatars;
import com.graduation.peelcs.mapper.AvatarsMapper;
import com.graduation.peelcs.service.IAvatarsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户头像资源表，包含可选头像及解锁条件 服务实现类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Service
public class AvatarsServiceImpl extends ServiceImpl<AvatarsMapper, Avatars> implements IAvatarsService {

}
