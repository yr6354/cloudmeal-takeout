package com.cloudmeal.takeout.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类新增 / 修改请求。
 */
@Data
public class CategoryDTO implements Serializable {

    private Long id;

    @NotNull(message = "分类类型不能为空")
    private Integer type;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 32, message = "分类名称最长 32 个字符")
    private String name;

    @NotNull(message = "排序值不能为空")
    private Integer sort;
}
