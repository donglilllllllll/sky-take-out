package com.sky.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.object.UpdatableSqlQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employeeLoginDTO.getUsername());
        Employee employee = employeeMapper.selectOne(queryWrapper);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // 前端传过来的铭文需要进行md5加密，然后再进行比对

        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     *
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        //对象属性拷贝
        BeanUtils.copyProperties(employeeDTO, employee);
        //设置账号状态为正常，默认正常为1，关闭为0
        employee.setStatus(StatusConstant.ENABLE);
        //设置账号密码，密码初始化为123456，MD5加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));
        //设置当前记录时间和最后一次修改时间
        employee.setUpdateTime(LocalDateTime.now());
        employee.setCreateTime(LocalDateTime.now());
        // 记录当前创建人和修改人的ID 从当前线程获取
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());
        employeeMapper.insert(employee);

    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {

        IPage<Employee> page = new Page(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(employeePageQueryDTO.getName() != null, Employee::getName, employeePageQueryDTO.getName())
                .orderByDesc(Employee::getUpdateTime);
        IPage page1 = employeeMapper.selectPage(page, queryWrapper);
        return new PageResult(page1.getTotal(), page1.getRecords());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        LambdaUpdateWrapper<Employee> updateWrapper = new LambdaUpdateWrapper<>();
        //启用/禁用

        updateWrapper.set(Employee::getStatus, status)

                .eq(Employee::getId, id);

        employeeMapper.update(null, updateWrapper);
    }

    @Override
    public void updateById1(EmployeeDTO employeeDTO) {

        Employee employee = employeeMapper.selectById(employeeDTO.getId());
        log.info("开始修改...");
        if (employee != null) {
           /* LambdaUpdateWrapper<Employee> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper.set(employeeDTO.getIdNumber() != null, Employee::getIdNumber, employeeDTO.getIdNumber())
                    .set(employeeDTO.getName() != null, Employee::getName, employeeDTO.getName())
                    .set(employeeDTO.getPhone() != null, Employee::getPhone, employeeDTO.getPhone())
                    .set(employeeDTO.getSex() != null, Employee::getSex, employeeDTO.getSex())
                    .set(employeeDTO.getUsername() != null, Employee::getUsername, employeeDTO.getUsername())
                    .eq(Employee::getId, employeeDTO.getId());*/
            Employee employee1 = new Employee();
            BeanUtils.copyProperties(employeeDTO, employee1);
            employee1.setUpdateUser(BaseContext.getCurrentId());
            employee1.setUpdateTime(LocalDateTime.now());
            employeeMapper.updateById(employee1);
            log.info("修改成功...");

        }
    }


}
