package com.graduation.peelcs.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户头像资源表，包含可选头像及解锁条件
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("avatars")
public class Avatars implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 头像ID
     */
    @TableId(value = "id", type = IdType.AUTO)
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
     * 是否默认头像:0否/1是
     */
    private Boolean isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;


}
