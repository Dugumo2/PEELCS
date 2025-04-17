package com.graduation.peelcs.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.Getter;

@Getter
public enum PermissionStatus {
        USER("user","普通用户"),
        ADMIN("admin","管理员")
    ;

        @EnumValue
        private String value;
        private String desc;

        PermissionStatus(String value, String desc) {
            this.value = value;
            this.desc = desc;
        }


        // 静态方法：根据 value 获取枚举实例
        public static PermissionStatus of(String value) {
            for (PermissionStatus status : PermissionStatus.values()) {
                if (status.getValue() == value) {
                    return status;
                }
            }
            throw new IllegalArgumentException("账户状态错误: " + value);
        }
    }