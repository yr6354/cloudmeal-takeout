package com.cloudmeal.takeout.service.impl;

import com.cloudmeal.takeout.exception.BusinessException;
import com.cloudmeal.takeout.result.ErrorCode;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cloudmeal.takeout.dto.UserLoginDTO;
import com.cloudmeal.takeout.entity.User;
import com.cloudmeal.takeout.mapper.UserMapper;
import com.cloudmeal.takeout.properties.WeChatProperties;
import com.cloudmeal.takeout.service.UserService;
import com.cloudmeal.takeout.utils.HttpClientUtil;

@Service
public class UserServiceImpl implements UserService {

    // 微信登录接口
    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     *
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        // 调用微信用户接口，获取openid
        Map<String, String> reqParams = new HashMap<>();
        reqParams.put("appid", weChatProperties.getAppid());
        reqParams.put("secret", weChatProperties.getSecret());
        reqParams.put("js_code", userLoginDTO.getCode());
        reqParams.put("grant_type", "authorization_code");
        String json = HttpClientUtil.doGet(WX_LOGIN_URL, reqParams);

        JSONObject parseJson = JSON.parseObject(json);
        String openid = parseJson.getString("openid");

        // 判断 openid 是否为空，如果为空，抛出异常
        if (openid == null) {
            throw new BusinessException(ErrorCode.LOGIN_FAILED);
        }

        // 是否在用户表中存在，如果不存在为新用户，插入用户表
        User user = userMapper.getByOpenid(openid);
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();

            userMapper.insert(user);
        }

        // 返回用户信息
        return user;
    }

}
