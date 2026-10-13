package com.cloudmeal.takeout.dto;

import com.cloudmeal.takeout.entity.SetmealDish;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 套餐新增 / 修改请求。
 */
@Data
public class SetmealDTO implements Serializable {

    private Long id;

    @NotNull(message = "套餐分类不能为空")
    private Long categoryId;

    @NotBlank(message = "套餐名称不能为空")
    @Size(max = 32, message = "套餐名称最长 32 个字符")
    private String name;

    @NotNull(message = "套餐价格不能为空")
    @DecimalMin(value = "0.01", message = "套餐价格必须大于 0")
    private BigDecimal price;

    /** 0 停用 1 启用 */
    private Integer status;

    private String description;

    private String image;

    @NotEmpty(message = "套餐必须包含菜品")
    private List<SetmealDish> setmealDishes = new ArrayList<>();
}
