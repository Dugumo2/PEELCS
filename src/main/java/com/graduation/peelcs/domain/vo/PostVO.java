package com.graduation.peelcs.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帖子列表VO
 */
@Data
public class PostVO {
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
     * 评论数量
     */
    private Integer commentCount;
    
    /**
     * 状态
     */
    private String status;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
} 