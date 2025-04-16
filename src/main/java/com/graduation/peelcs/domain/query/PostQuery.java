package com.graduation.peelcs.domain.query;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帖子查询参数
 * @author 冯
 * @description
 * @create 2025-04-16 16:35
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PostQuery extends PageQuery {
    /**
     * 是否官方帖子
     */
    private Boolean isOfficial;

    /**
     * 用户ID（查询指定用户的帖子）
     */
    private Long userId;

    /**
     * 帖子状态
     */
    private String status;

    /**
     * 标题关键字
     */
    private String titleKeyword;
}
