package com.starQeem.woha.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.pojo.user;
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
        //拿到当前登录的对象
        Subject subject = SecurityUtils.getSubject();
        userDto userDto = (userDto) subject.getPrincipal();
        QueryWrapper<user> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","perms").eq("id", userDto.getId());
        user user = userService.getBaseMapper().selectOne(queryWrapper);
        if (user.getPerms() != null && user.getPerms().equals("admin")){
            SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
            System.out.println("执行了=>授权管理员权限");
            info.addStringPermission("user:admin");  //授权管理员权限
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
            QueryWrapper<user> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id","username","password","perms","status").eq("username",userToken.getUsername());
            user user = userService.getBaseMapper().selectOne(queryWrapper);
            if (user == null){
                return null;  //抛出异常 UnknownAccountException
            }
            userDto userDto = new userDto();
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
            //密码认证(shiro做~)
            SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(userDto, user.getPassword(), "");
            return authenticationInfo;  //用户名密码登录
        }else {
            //验证码登录
            String getCode = stringRedisTemplate.opsForValue().get(USER_CODE + userToken.getUsername());
            if (getCode == null){
                return null;  //抛出异常 UnknownAccountException
            }
            QueryWrapper<user> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id","username","perms").eq("username", userToken.getUsername());
            user user = userService.getBaseMapper().selectOne(queryWrapper);
            userDto userDto = new userDto();
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
            //密码认证(shiro做~)
            return new SimpleAuthenticationInfo(userDto,getCode,"");  //邮箱验证码登录
        }
    }
}
