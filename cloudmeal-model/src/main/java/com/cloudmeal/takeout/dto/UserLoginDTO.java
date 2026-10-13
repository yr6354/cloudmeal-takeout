package com.cloudmeal.takeout.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * C 端用户微信登录请求。
 */
@Data
public class UserLoginDTO implements Serializable {

    @NotBlank(message = "微信登录凭证 code 不能为空")
    private String code;
}
