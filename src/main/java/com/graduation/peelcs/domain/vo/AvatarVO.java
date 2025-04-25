package com.graduation.peelcs.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 头像信息视图对象（包含解锁状态）
 */
@Data
@Accessors(chain = true)
public class AvatarVO {
    /**
     * 头像ID
     */
    private Long id;

    /**
     * 头像名称
     */
    private String name;

    /**
     * 头像图片路径
     */
    private String imageUrl;

    /**
     * 所需积分
     */
    private Integer pointsRequired;

    /**
     * 是否默认头像
     */
    private Boolean isDefault;

    /**
     * 是否已解锁
     */
    private Boolean isUnlocked;
} 