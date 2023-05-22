package com.starQeem.woha.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.starQeem.woha.dto.userDto;
import com.starQeem.woha.pojo.user;
import com.starQeem.woha.service.userService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;

import static com.starQeem.woha.util.constant.CODE_SIZE;
import static com.starQeem.woha.util.constant.USER_CODE;

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
        return null;
    }
    //认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        UsernamePasswordToken userToken = (UsernamePasswordToken) token;
        if (userToken.getPassword().length > CODE_SIZE){
            //用户名,密码  数据库中取
            QueryWrapper<user> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id","username","password").eq("username",userToken.getUsername());
            user user = userService.getBaseMapper().selectOne(queryWrapper);
            if (user == null){
                return null;  //抛出异常 UnknownAccountException
            }
            userDto userDto = new userDto();
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
            //密码认证(shiro做~)
            SimpleAuthenticationInfo authenticationInfo = new SimpleAuthenticationInfo(userDto, user.getPassword(), "");
            System.out.println();
            return authenticationInfo;  //用户名密码登录
        }else {
            //验证码登录
            String getCode = stringRedisTemplate.opsForValue().get(USER_CODE + userToken.getUsername());
            if (getCode == null){
                return null;  //抛出异常 UnknownAccountException
            }
            QueryWrapper<user> queryWrapper = new QueryWrapper<>();
            queryWrapper.select("id","username").eq("username", userToken.getUsername());
            user user = userService.getBaseMapper().selectOne(queryWrapper);
            userDto userDto = new userDto();
            userDto.setId(user.getId());
            userDto.setUsername(user.getUsername());
            //密码认证(shiro做~)
            return new SimpleAuthenticationInfo(userDto,getCode,"");  //邮箱验证码登录
        }
    }
}
