package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

/**
 * @Author：zhangkaixiang
 * @Package：com.sky.service
 * @Project：sky-take-out
 * @name：UserService
 * @Date：2024/7/11 16:13
 * @Filename：UserService
 */
public interface UserService {
    User login(UserLoginDTO userLoginDTO);
}
