package com.zjtelcom.cpct.shiro;

import com.zjtelcom.cpct.service.system.SysStaffService;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @Author:HuangHua
 * @Descirption: 自定义权限匹配以及账号密码验证
 * @Date: Created by huanghua on 2018/5/8.
 * @Modified By:
 */
public class MyShiroRealm extends AuthorizingRealm {

    private static final Logger logger = LoggerFactory.getLogger(MyShiroRealm.class);

    @Resource
    private SysStaffService sysStaffService;

    @Override
    public String getName() {
        return "myShiroRealm";
    }

    /**
     * 添加角色权限
     * @param principals
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        logger.info("打印PrincipalCollection的相关信息{}", principals.toString());
        String username = principals.toString();
        logger.info(username);
        //UserInfo userInfo = (UserInfo)principals.getPrimaryPrincipal();

        logger.info("打印PrincipalCollection的相关信息{}", principals.toString());
        authorizationInfo.addRole("Admin");
        authorizationInfo.addRole("SuperAdmin");
        return authorizationInfo;
    }

    /**
     * 用户认证
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken)
            throws AuthenticationException {

        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        logger.info("验证当前登录用户时获取到的token={}", ReflectionToStringBuilder.toString(token));
        Map<String, Object> userMap = sysStaffService.queryUserByName(token.getUsername());
//        SysStaff user = (SysStaff) userMap.get("data");
//        if (user == null) {
//            return null;
//        }
//        return new SimpleAuthenticationInfo(user, user.getPassword(), getName());
        return null;
    }
}
