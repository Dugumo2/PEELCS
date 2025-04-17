package com.graduation.peelcs.service.impl;


import cn.dev33.satoken.stp.StpInterface;
import com.baomidou.mybatisplus.extension.toolkit.Db;
import com.graduation.peelcs.domain.enums.PermissionStatus;
import com.graduation.peelcs.domain.po.Users;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {


    @Override
    public List<String> getPermissionList(Object o, String s) {
        return null;
    }

    /**
     *
     * @param loginId 用户ID
     * @param loginType 多用户验证需要，未使用
     * @return 权限角色列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<String> roleList = new ArrayList<>();

        String role = Db.lambdaQuery(Users.class)
                .eq(Users::getId, loginId)
                .one()
                .getRole();
        if(role == PermissionStatus.USER.getValue()) {
            roleList.add(PermissionStatus.USER.getValue());
        }else if(role == PermissionStatus.ADMIN.getValue()) {
            roleList.add(PermissionStatus.ADMIN.getValue());
        }
        return roleList;
    }
}
