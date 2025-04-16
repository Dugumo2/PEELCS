package com.graduation.peelcs.domain.dto;

import lombok.Data;

/**
 * 论坛帖子DTO
 */
@Data
public class PostDTO {
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
} 