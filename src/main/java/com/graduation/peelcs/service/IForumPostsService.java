package com.graduation.peelcs.service;

import com.graduation.peelcs.domain.po.ForumPosts;
import com.baomidou.mybatisplus.extension.service.IService;
import com.graduation.peelcs.domain.query.PostQuery;
import com.graduation.peelcs.domain.vo.PostDetailVO;
import com.graduation.peelcs.domain.vo.PostVO;

import java.util.List;

/**
 * <p>
 * 论坛帖子表 服务类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
public interface IForumPostsService extends IService<ForumPosts> {

    /**
     * 创建帖子
     *
     * @param userId 用户ID
     * @param title 标题
     * @param content 内容
     * @param isAnonymous 是否匿名
     * @return 创建的帖子
     */
    ForumPosts createPost(Long userId, String title, String content, Boolean isAnonymous);
    
    /**
     * 创建官方帖子
     *
     * @param userId 用户ID（管理员）
     * @param title 标题
     * @param content 内容
     * @return 创建的官方帖子
     */
    ForumPosts createOfficialPost(Long userId, String title, String content);
    
    /**
     * 审核帖子
     *
     * @param userId 用户ID（管理员）
     * @param postId 帖子ID
     * @param status 状态 (approved/rejected)
     * @return 更新后的帖子
     */
    ForumPosts reviewPost(Long userId, Long postId, String status);
    
    /**
     * 删除帖子
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否删除成功
     */
    boolean deletePost(Long userId, Long postId);
    
    /**
     * 获取帖子列表
     *
     * @param query 查询条件
     * @return 帖子列表
     */
    List<PostVO> getPosts(PostQuery query);
    
    /**
     * 获取帖子详情
     *
     * @param postId 帖子ID
     * @return 帖子详情
     */
    PostDetailVO getPostDetail(Long postId);

}
