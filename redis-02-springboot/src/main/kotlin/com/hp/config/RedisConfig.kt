package com.hp.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.net.UnknownHostException

@Configuration
class RedisConfig {

    @Bean
    @Throws(UnknownHostException::class)
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory?): RedisTemplate<String, Any>? {
        val template = RedisTemplate<String, Any>()
        template.setConnectionFactory(redisConnectionFactory!!)

        val jackson2JsonRedisSerializer = Jackson2JsonRedisSerializer<Any>(Any::class.java)
        val stringRedisSerializer = StringRedisSerializer()

        template.keySerializer = stringRedisSerializer
        template.valueSerializer = jackson2JsonRedisSerializer
        template.stringSerializer = stringRedisSerializer
        template.hashKeySerializer = stringRedisSerializer
        template.hashValueSerializer = jackson2JsonRedisSerializer
        template.afterPropertiesSet()
        return template
    }

}