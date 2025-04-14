package com.graduation.peelcs.mapper;

import com.graduation.peelcs.domain.po.Avatars;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户头像资源表，包含可选头像及解锁条件 Mapper 接口
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Mapper
public interface AvatarsMapper extends BaseMapper<Avatars> {

}
