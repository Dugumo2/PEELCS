package com.graduation.peelcs.service.impl;

import com.graduation.peelcs.domain.po.PostComments;
import com.graduation.peelcs.mapper.PostCommentsMapper;
import com.graduation.peelcs.service.IPostCommentsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 帖子评论表 服务实现类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Service
public class PostCommentsServiceImpl extends ServiceImpl<PostCommentsMapper, PostComments> implements IPostCommentsService {

}
