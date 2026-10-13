package com.cloudmeal.takeout.dto;

import com.cloudmeal.takeout.entity.DishFlavor;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜品新增 / 修改请求。
 */
@Data
public class DishDTO implements Serializable {

    private Long id;

    @NotBlank(message = "菜品名称不能为空")
    @Size(max = 32, message = "菜品名称最长 32 个字符")
    private String name;

    @NotNull(message = "菜品分类不能为空")
    private Long categoryId;

    @NotNull(message = "菜品价格不能为空")
    @DecimalMin(value = "0.01", message = "菜品价格必须大于 0")
    private BigDecimal price;

    private String image;

    private String description;

    /** 0 停售 1 起售 */
    private Integer status;

    /** 口味 */
    private List<DishFlavor> flavors = new ArrayList<>();
}
