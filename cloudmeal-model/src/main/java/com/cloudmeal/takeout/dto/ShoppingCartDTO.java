package com.cloudmeal.takeout.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 购物车操作请求。
 */
@Data
public class ShoppingCartDTO implements Serializable {

    private Long dishId;
    private Long setmealId;
    private String dishFlavor;
}
