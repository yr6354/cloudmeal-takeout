package com.cloudmeal.takeout.service.impl;

import com.cloudmeal.takeout.constant.PasswordConstant;
import com.cloudmeal.takeout.constant.StatusConstant;
import com.cloudmeal.takeout.dto.EmployeeDTO;
import com.cloudmeal.takeout.dto.EmployeeLoginDTO;
import com.cloudmeal.takeout.dto.EmployeePageQueryDTO;
import com.cloudmeal.takeout.entity.Employee;
import com.cloudmeal.takeout.exception.BusinessException;
import com.cloudmeal.takeout.mapper.EmployeeMapper;
import com.cloudmeal.takeout.result.ErrorCode;
import com.cloudmeal.takeout.result.PageResult;
import com.cloudmeal.takeout.security.PasswordService;
import com.cloudmeal.takeout.service.EmployeeService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 员工管理。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final PasswordService passwordService;

    @Override
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        Employee employee = employeeMapper.getByUsername(employeeLoginDTO.getUsername());
        if (employee == null) {
            throw new BusinessException(ErrorCode.ACCOUNT_NOT_FOUND);
        }
        if (!passwordService.matches(employeeLoginDTO.getPassword(), employee.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        if (StatusConstant.DISABLE.equals(employee.getStatus())) {
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
        upgradeLegacyPassword(employeeLoginDTO.getPassword(), employee);
        return employee;
    }

    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employee.setStatus(StatusConstant.ENABLE);
        employee.setPassword(passwordService.encode(PasswordConstant.DEFAULT_PASSWORD));
        employeeMapper.insert(employee);
    }

    @Override
    public PageResult<Employee> pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);
        List<Employee> records = page.getResult();
        return new PageResult<>(page.getTotal(), records);
    }

    @Override
    public void startOrStop(Integer status, long id) {
        employeeMapper.update(Employee.builder().id(id).status(status).build());
    }

    @Override
    public Employee getById(long id) {
        return employeeMapper.getById(id);
    }

    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO, employee);
        employeeMapper.update(employee);
    }

    /**
     * 历史账号的密码是 MD5，登录成功后自动重新用 BCrypt 加密回写，逐步完成迁移。
     */
    private void upgradeLegacyPassword(String rawPassword, Employee employee) {
        if (!passwordService.isLegacy(employee.getPassword())) {
            return;
        }
        employeeMapper.update(Employee.builder()
                .id(employee.getId())
                .password(passwordService.encode(rawPassword))
                .build());
        log.info("员工 {} 的密码已从 MD5 升级为 BCrypt", employee.getUsername());
    }
}
