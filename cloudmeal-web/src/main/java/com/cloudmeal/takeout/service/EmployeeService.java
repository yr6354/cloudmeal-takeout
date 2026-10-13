package com.cloudmeal.takeout.service;

import com.cloudmeal.takeout.dto.EmployeeDTO;
import com.cloudmeal.takeout.dto.EmployeeLoginDTO;
import com.cloudmeal.takeout.dto.EmployeePageQueryDTO;
import com.cloudmeal.takeout.entity.Employee;
import com.cloudmeal.takeout.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * 
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 保存员工信息
     * 
     * @param employeeDTO
     */
    void save(EmployeeDTO employeeDTO);

    /**
     * 分页查询员工信息
     * 
     * @param employeePageQueryDTO
     */
    PageResult<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 启用/禁用
     * 
     * @param status
     * @param id
     * 
     * @return
     */
    void startOrStop(Integer status, long id);

    /**
     * 根据id查询员工
     * 
     * @param id
     * @return
     */
    Employee getById(long id);

    /**
     * 修改员工信息
     * 
     * @param employeeDTO
     */
    void update(EmployeeDTO employeeDTO);

}
