package com.sky.annotation;

/**
 * @Author：zhangkaixiang
 * @Package：com.sky.annotation
 * @Project：sky-take-out
 * @name：AutoFill
 * @Date：2024/6/22 19:31
 * @Filename：AutoFill
 */

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解AutoFill,实现公共字段填充
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {

    //数据库操作类型 insert 、update
    OperationType value();
}
