package com.atguigu.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.constant.RedisConst;
import com.atguigu.entity.UserInfo;
import com.atguigu.result.RetVal;
import com.atguigu.service.UserInfoService;
import com.atguigu.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("login")
    public RetVal login(@RequestBody UserInfo userInfo, HttpServletRequest request){
        UserInfo dbUserInfo = userInfoService.login(userInfo);
        Map<String, String> retMap = new HashMap<>();
        if (dbUserInfo != null){
            String token = UUID.randomUUID().toString();
            retMap.put("token",token);

            String nickName = dbUserInfo.getNickName();
            retMap.put("nickName",nickName);

            /**c.将用户信息存放到redis当中
             * 用户信息的key 一个前缀+token
             * 存储的信息
             *  用户的id
             *  用户使用机器的ip地址
             */
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId",dbUserInfo.getId());
            jsonObject.put("loginIp", IpUtil.getIpAddress(request));
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX+token,jsonObject,RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            return RetVal.ok(retMap);
        }else{
            return RetVal.fail().message("登陆失败");
        }
    }

    @GetMapping("logout")
    public RetVal logout(HttpServletRequest request){
        String token = request.getHeader("token");
        String userKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
        redisTemplate.delete(userKey);
        return RetVal.ok();
    }
}
