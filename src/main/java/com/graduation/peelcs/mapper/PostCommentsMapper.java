package com.graduation.peelcs.mapper;

import com.graduation.peelcs.domain.po.PostComments;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 帖子评论表 Mapper 接口
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Mapper
public interface PostCommentsMapper extends BaseMapper<PostComments> {

}
