package com.cloudmeal.takeout.service;

import com.cloudmeal.takeout.dto.UserLoginDTO;
import com.cloudmeal.takeout.entity.User;

public interface UserService {

    /**
     * 微信登录
     *
     * @param userLoginDTO
     * @return
     */
    public User wxLogin(UserLoginDTO userLoginDTO);
}
