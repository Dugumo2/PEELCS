package com.graduation.peelcs.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 用户信息视图对象
 */
@Data
@Accessors(chain = true)
public class UserVO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像ID
     */
    private Long avatarId;
    
    /**
     * 用户角色
     */
    private String role;
    
    /**
     * 账号状态
     */
    private String status;
    
    /**
     * 积分
     */
    private Integer points;
    
    /**
     * 登录令牌
     */
    private String token;
} 