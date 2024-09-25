package com.sky.service.impl;

import com.sky.config.RedisConfiguration;
import com.sky.service.ShopStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @Author：zhangkaixiang
 * @Package：com.sky.service.impl
 * @Project：sky-take-out
 * @name：ShopStatusServiceImpl
 * @Date：2024/7/2 23:31
 * @Filename：ShopStatusServiceImpl
 */
@Service
public class ShopStatusServiceImpl implements ShopStatusService {

    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Override
    public void setStatus(Integer status) {

        redisTemplate.opsForValue().set("shop_status",status);


    }

    @Override
    public Integer getStatus() {
        Object shopStatus = redisTemplate.opsForValue().get("shop_status");
        return (Integer) shopStatus;
    }
}
