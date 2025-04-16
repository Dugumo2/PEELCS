package com.graduation.peelcs.service;

import com.graduation.peelcs.domain.po.PostComments;
import com.baomidou.mybatisplus.extension.service.IService;
import com.graduation.peelcs.domain.query.CommentQuery;
import com.graduation.peelcs.domain.vo.CommentVO;

import java.util.List;

/**
 * <p>
 * 帖子评论表 服务类
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
public interface IPostCommentsService extends IService<PostComments> {

    /**
     * 创建评论（顶级评论）
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     * @param content 评论内容
     * @param isAnonymous 是否匿名
     * @return 创建的评论
     */
    PostComments createComment(Long userId, Long postId, String content, Boolean isAnonymous);
    
    /**
     * 回复评论（子评论）
     *
     * @param userId 用户ID
     * @param commentId 评论ID
     * @param content 评论内容
     * @param isAnonymous 是否匿名
     * @return 创建的评论回复
     */
    PostComments replyComment(Long userId, Long commentId, String content, Boolean isAnonymous);
    
    /**
     * 删除评论
     *
     * @param userId 用户ID
     * @param commentId 评论ID
     * @return 是否删除成功
     */
    boolean deleteComment(Long userId, Long commentId);
    
    /**
     * 审核评论
     *
     * @param userId 用户ID（管理员）
     * @param commentId 评论ID
     * @param status 状态 (approved/rejected)
     * @return 更新后的评论
     */
    PostComments reviewComment(Long userId, Long commentId, String status);
    
    /**
     * 获取帖子的评论列表（仅顶级评论）
     *
     * @param postId 帖子ID
     * @param page 页码
     * @param size 每页大小
     * @return 评论列表
     */
    List<CommentVO> getPostComments(Long postId, Integer page, Integer size);
    
    /**
     * 获取评论的回复列表（子评论）
     *
     * @param commentId 评论ID（顶级评论）
     * @param page 页码
     * @param size 每页大小
     * @return 回复列表
     */
    List<CommentVO> getCommentReplies(Long commentId, Integer page, Integer size);
    
    /**
     * 根据查询条件获取评论列表
     *
     * @param query 评论查询条件
     * @return 评论列表
     */
    List<CommentVO> getComments(CommentQuery query);

}
