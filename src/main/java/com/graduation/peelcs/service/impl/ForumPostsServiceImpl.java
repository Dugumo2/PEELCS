package com.graduation.peelcs.service.impl;

import com.graduation.peelcs.domain.po.ForumPosts;
import com.graduation.peelcs.mapper.ForumPostsMapper;
import com.graduation.peelcs.service.IForumPostsService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 论坛帖子表 服务实现类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Service
public class ForumPostsServiceImpl extends ServiceImpl<ForumPostsMapper, ForumPosts> implements IForumPostsService {

}
