package com.cloudmeal.takeout.constant;

/**
 * Redis Key 统一命名。
 *
 * <p>统一前缀 {@code cloudmeal:}，便于运维排查与按前缀清理。</p>
 */
public final class CacheKeyConstant {

    private CacheKeyConstant() {
    }

    /** 店铺营业状态：1 营业，0 打烊 */
    public static final String SHOP_STATUS = "cloudmeal:shop:status";

    /** 分类列表缓存 */
    public static final String CATEGORY_LIST_BY_TYPE = "cloudmeal:cache:category:type:";

    /** 菜品详情缓存前缀，后接菜品 id */
    public static final String DISH_DETAIL = "cloudmeal:cache:dish:detail:";

    /** 按分类查询的起售菜品缓存前缀，后接分类 id */
    public static final String DISH_LIST_BY_CATEGORY = "cloudmeal:cache:dish:category:";

    /** 按分类查询的起售套餐缓存前缀，后接分类 id */
    public static final String SETMEAL_LIST_BY_CATEGORY = "cloudmeal:cache:setmeal:category:";

    /** 套餐详情缓存前缀，后接套餐 id */
    public static final String SETMEAL_DETAIL = "cloudmeal:cache:setmeal:detail:";

    /** 套餐下的菜品缓存前缀 */
    public static final String SETMEAL_DISHES = "cloudmeal:cache:setmeal:dishes:";

    /** 用户下单幂等标记前缀 */
    public static final String ORDER_IDEMPOTENT = "cloudmeal:order:idempotent:";
}
