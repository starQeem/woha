package com.starQeem.woha.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.starQeem.woha.dto.userDto;

import com.starQeem.woha.pojo.User;
import com.starQeem.woha.service.userService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;


import static com.starQeem.woha.util.constant.*;

/**
 * @Date: 2023/5/18 10:55
 * @author: Qeem
 * 自定义的UserRealm
 */

public class UserRealm extends AuthorizingRealm {
    @Resource
    private userService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    //授权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        System.out.println("执行了=>授权");
        //获取当前登录用户
        Subject subject = SecurityUtils.getSubject();
        userDto userDto = (userDto) subject.getPrincipal();
        //从数据库中查询用户权限
        User user = userService.getBaseMapper().selectOne(Wrappers.<User>lambdaQuery()
                .select(User::getId,User::getPerms)
                .eq(User::getId,userDto.getId()));
        if (user.getPerms() != null && user.getPerms().equals("admin")){ //判断用户权限是否为admin
            //是,执行授权
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            System.out.println("执行了=>授权管理员权限");
            info.addStringPermission("User:admin");  //授权管理员权限
            return info;
        }
        return null;
    }
    //认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken userToken = (UsernamePasswordToken) token;
        if (userToken.getPassword().length > CODE_SIZE){
            //用户名,密码  数据库中取
            User user = userService.getBaseMapper().selectOne(Wrappers.<User>lambdaQuery()
                    .select(User::getId,User::getUsername,User::getPassword,User::getPerms)
                    .eq(User::getUsername,userToken.getUsername()));
            if (user == null){
                //用户不存在
                return null;  //抛出异常 UnknownAccountException
            }
            userDto userDto = new userDto();
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
            //密码认证(shiro做~)
            return new SimpleAuthenticationInfo(userDto, user.getPassword(), ""); //用户名密码登录
        }else {
            //验证码登录
            String getCode = stringRedisTemplate.opsForValue().get(USER_CODE + userToken.getUsername());
            if (getCode == null){
                //redis中验证码为空(过期了或压根没有)
                return null;  //抛出异常 UnknownAccountException
            }
            //根据用户名查询用户信息
            User user = userService.getBaseMapper().selectOne(Wrappers.<User>lambdaQuery()
                    .select(User::getId,User::getPerms,User::getUsername)
                    .eq(User::getUsername,userToken.getUsername()));
            userDto userDto = new userDto();
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
            //密码认证(shiro做~)
            return new SimpleAuthenticationInfo(userDto,getCode,"");  //邮箱验证码登录
        }
    }
}
