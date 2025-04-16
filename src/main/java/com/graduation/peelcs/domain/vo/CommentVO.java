package com.graduation.peelcs.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论VO
 */
@Data
public class CommentVO {
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 顶级评论ID
     */
    private Long rootCommentId;
    
    /**
     * 目标评论ID
     */
    private Long toCommentId;
    
    /**
     * 回复目标用户昵称
     */
    private String toUserNickname;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 是否匿名
     */
    private Boolean isAnonymous;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    /**
     * 回复列表（子评论）
     */
    private List<CommentVO> replies;
} 