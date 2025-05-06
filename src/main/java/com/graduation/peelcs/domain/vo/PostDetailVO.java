package com.graduation.peelcs.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子详情VO
 */
@Data
public class PostDetailVO {
    /**
     * 帖子ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 分区类别ID（官方通知0，考研资讯1，学习交流2）
     */
    private Long categoryId;
    
    /**
     * 分区名称
     */
    private String categoryName;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 内容
     */
    private String content;
    
    /**
     * 是否匿名
     */
    private Boolean isAnonymous;
    
    /**
     * 是否官方帖子
     */
    private Boolean isOfficial;
    
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
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    /**
     * 评论列表（顶级评论）
     */
    private List<CommentVO> comments;
} 