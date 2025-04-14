package com.graduation.peelcs.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * 用户相关请求参数
 */
@Data
@Builder
@AllArgsConstructor
public class UserDTO {

        /**
         * 邮箱
         */
        private String email;
        
        /**
         * 密码
         */
        private String password;
        
        /**
         * 用户名
         */
        private String nickname;
        
        /**
         * 验证码
         */
        private String code;
} 