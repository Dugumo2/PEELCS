package com.graduation.peelcs.domain.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 冯
 * @description
 * @create 2025-04-16 16:35
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommentQuery extends PageQuery{
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 根评论ID
     */
    private Long rootCommentId;
    
    /**
     * 用户ID
     */
    private Long userId;
}
