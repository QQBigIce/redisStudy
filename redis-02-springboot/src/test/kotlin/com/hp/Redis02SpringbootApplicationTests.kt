package com.hp

import com.hp.pojo.User
import com.hp.utils.RedisUtil
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate

@SpringBootTest
class Redis02SpringbootApplicationTests {

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    @Autowired
    private lateinit var redisUtil: RedisUtil

    @Test
    fun test1(): Unit {
        redisUtil.set("name", "hp")
        println(redisUtil.get("name"))
    }

    @Test
    fun contextLoads() {
        redisTemplate.connectionFactory?.connection?.flushDb()
        redisTemplate.opsForValue().set("username", "你好再见！")
        println("${redisTemplate.opsForValue().get("username")}============================")
    }

    @Test
    fun test(): Unit {
        redisTemplate.connectionFactory?.connection?.flushDb()
        val user = User("黄平", 3)
        // val jsonUser = ObjectMapper().writeValueAsString(user)
        redisTemplate.opsForValue().set("user", user)
        println(redisTemplate.opsForValue().get("user"))
    }

}
