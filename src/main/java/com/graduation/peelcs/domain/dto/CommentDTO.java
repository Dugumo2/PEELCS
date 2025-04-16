package com.graduation.peelcs.domain.dto;

import lombok.Data;

/**
 * 评论DTO
 */
@Data
public class CommentDTO {


    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 是否匿名
     */
    private Boolean isAnonymous;
} 