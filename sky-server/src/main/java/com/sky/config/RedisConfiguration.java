package com.sky.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;

/**
 * @Author：zhangkaixiang
 * @Package：com.sky.config
 * @Project：sky-take-out
 * @name：RedisConfiguration
 * @Date：2024/6/30 14:20
 * @Filename：RedisConfiguration
 */
@Configuration
@Slf4j
public class RedisConfiguration {
    private final Environment environment;
    public RedisConfiguration(Environment environment){
        this.environment=environment;
    }

    @Bean
    public RedisConnectionFactory myLettuceConnectionFactory() throws Exception {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(Objects.requireNonNull(environment.getProperty("spring.redis.host")),Integer.parseInt(environment.getProperty("spring.redis.port")));
        redisStandaloneConfiguration.setDatabase(Integer.parseInt(Objects.requireNonNull(environment.getProperty("spring.redis.database"))));
        //获取application.yml 中的密码（密文）
        String password = environment.getProperty("spring.redis.password");
        log.info("redis password 配置文件中密码密文：====={}",password);
        String decrypt = AESUtil.decrypt(password);
        log.info("redis password 解密后密码：====={}",decrypt);
        redisStandaloneConfiguration.setPassword(decrypt);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }


    @Bean
    public RedisTemplate<?,?> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始配置RedisTemplate");
        RedisTemplate<?,?> redisTemplate = new RedisTemplate();
        //设置RedisTemplate的连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        // 设置redis key的序列化器
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        //redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues();
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .transactionAware()
                .build();
    }

}
