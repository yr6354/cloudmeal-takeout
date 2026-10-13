package com.cloudmeal.takeout.result;

import lombok.Getter;

/**
 * 统一业务错误码。
 *
 * <p>约定：1 表示成功；其余为业务失败码，按领域分段。
 * 前端只需要判断 {@code code == 1} 即可区分成功与失败，同时可依据具体失败码做差异化提示。</p>
 *
 * <ul>
 *   <li>1xxxx 通用</li>
 *   <li>2xxxx 账号与认证</li>
 *   <li>3xxxx 商品（分类 / 菜品 / 套餐）</li>
 *   <li>4xxxx 订单</li>
 *   <li>5xxxx 基础设施</li>
 * </ul>
 */
@Getter
public enum ErrorCode {

    /* ---------------- 通用 ---------------- */
    UNKNOWN_ERROR(10000, "未知错误"),
    PARAM_ERROR(10001, "请求参数不合法"),
    USER_NOT_LOGIN(10002, "用户未登录"),
    NO_PERMISSION(10003, "没有操作权限"),

    /* ---------------- 账号与认证 ---------------- */
    LOGIN_FAILED(20001, "登录失败"),
    ACCOUNT_NOT_FOUND(20002, "账号不存在"),
    PASSWORD_ERROR(20003, "密码错误"),
    ACCOUNT_LOCKED(20004, "账号已被锁定"),
    ACCOUNT_ALREADY_EXIST(20005, "账号已存在"),
    PASSWORD_EDIT_FAILED(20006, "原密码错误，密码修改失败"),
    TOKEN_INVALID(20007, "登录状态已失效，请重新登录"),

    /* ---------------- 商品 ---------------- */
    CATEGORY_BE_RELATED_BY_DISH(30001, "当前分类关联了菜品，不能删除"),
    CATEGORY_BE_RELATED_BY_SETMEAL(30002, "当前分类关联了套餐，不能删除"),
    DISH_ON_SALE(30003, "起售中的菜品不能删除"),
    DISH_BE_RELATED_BY_SETMEAL(30004, "当前菜品关联了套餐，不能删除"),
    SETMEAL_ON_SALE(30005, "起售中的套餐不能删除"),
    SETMEAL_ENABLE_FAILED(30006, "套餐内包含未启售菜品，无法启售"),

    /* ---------------- 订单 ---------------- */
    ADDRESS_BOOK_IS_NULL(40001, "收货地址不存在，请重新选择"),
    SHOPPING_CART_IS_NULL(40002, "购物车为空，不能下单"),
    ORDER_NOT_FOUND(40003, "订单不存在"),
    ORDER_STATUS_ERROR(40004, "订单状态不支持当前操作"),
    ORDER_ALREADY_PAID(40005, "该订单已支付"),
    ORDER_DUPLICATE_SUBMIT(40006, "请勿重复提交订单"),

    /* ---------------- 基础设施 ---------------- */
    UPLOAD_FAILED(50001, "文件上传失败");

    /** 业务码 */
    private final int code;
    /** 默认提示文案，可被业务侧覆盖 */
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
