package com.graduation.peelcs.domain.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户签到记录表
 * </p>
 *
 * @author feng
 * @since 2025-04-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user_checkins")
public class UserCheckins implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 签到ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 签到日期
     */
    private LocalDate checkInDate;

    /**
     * 获得积分
     */
    private Integer pointsEarned;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;


}
