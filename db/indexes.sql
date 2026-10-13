-- 云膳外卖：查询性能优化索引
--
-- 说明：教程原始脚本只有主键，缺少高频查询路径上的索引。
-- 下面的索引对应实际查询场景，可按需执行（MySQL 8.0 支持 IF NOT EXISTS 语法）。

USE `cloudmeal_takeout`;

-- 员工登录：按用户名查询，且要求唯一
CREATE UNIQUE INDEX `uk_employee_username` ON `employee` (`username`);

-- 用户端按分类查菜品：菜单页高频查询
CREATE INDEX `idx_dish_category_status` ON `dish` (`category_id`, `status`);

-- 用户端按分类查套餐
CREATE INDEX `idx_setmeal_category_status` ON `setmeal` (`category_id`, `status`);

-- 购物车：按用户聚合查询
CREATE INDEX `idx_shopping_cart_user` ON `shopping_cart` (`user_id`);

-- 订单列表：按用户 + 状态分页，避免全表扫描
CREATE INDEX `idx_orders_user_status` ON `orders` (`user_id`, `status`);

-- 订单列表：按状态筛选、按下单时间排序
CREATE INDEX `idx_orders_status_order_time` ON `orders` (`status`, `order_time`);

-- 定时任务：扫描超时订单
CREATE INDEX `idx_orders_pay_status_time` ON `orders` (`pay_status`, `order_time`);

-- 订单号查询（支付回调、对账）
CREATE UNIQUE INDEX `uk_orders_number` ON `orders` (`number`);

-- 订单明细：按订单查明细
CREATE INDEX `idx_order_detail_order` ON `order_detail` (`order_id`);

-- 套餐-菜品关系：按菜品反查套餐
CREATE INDEX `idx_setmeal_dish_dish` ON `setmeal_dish` (`dish_id`);

-- 报表：按完成状态统计销量
CREATE INDEX `idx_report_orders_status_time` ON `orders` (`status`, `order_time`, `amount`);
