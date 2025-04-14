package com.graduation.peelcs.mapper;

import com.graduation.peelcs.domain.po.UserCheckins;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户签到记录表 Mapper 接口
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Mapper
public interface UserCheckinsMapper extends BaseMapper<UserCheckins> {

}
