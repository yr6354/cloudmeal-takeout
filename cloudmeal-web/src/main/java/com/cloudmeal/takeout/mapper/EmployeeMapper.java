package com.cloudmeal.takeout.mapper;

import com.github.pagehelper.Page;
import com.cloudmeal.takeout.annotation.AutoFill;
import com.cloudmeal.takeout.dto.EmployeePageQueryDTO;
import com.cloudmeal.takeout.entity.Employee;
import com.cloudmeal.takeout.enumeration.OperationType;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * 
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 保存员工信息
     * 
     * @param employee
     */
    @Insert("INSERT INTO employee " +
            "(name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user) "
            +
            "VALUES(#{name},#{username},#{password},#{phone},#{sex},#{idNumber},#{status},#{createTime},#{updateTime},#{createUser},#{updateUser})")
    @AutoFill(value = OperationType.INSERT)
    void insert(Employee employee);

    /**
     * 分页查询员工信息
     * 
     * @param employeePageQueryDTO
     * @return
     */
    Page<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    /**
     * 根据主键修改员工信息
     * 
     * @param employee
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Employee employee);

    /**
     * 根据主键查询员工信息
     * 
     * @param id
     * @return
     */
    @Select("select * from employee where id = #{id}")
    Employee getById(long id);

}
