package com.graduation.peelcs.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    /**
     * 账号（用户名或邮箱）
     */
    private String account;
    
    /**
     * 密码
     */
    private String password;
} 