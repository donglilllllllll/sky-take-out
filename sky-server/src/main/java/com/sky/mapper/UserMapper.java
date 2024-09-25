package com.sky.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.Map;

/**
* @author 凯祥
* @description 针对表【user(用户信息)】的数据库操作Mapper
* @createDate 2024-07-11 17:32:34
* @Entity generator.domain.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {


    Integer countUserByMap(Map<String, Object> map);
}




