package com.hp

import com.alibaba.fastjson.JSONObject
import redis.clients.jedis.Jedis

fun main(args: Array<String>) {
    val jedis = Jedis("47.100.3.79", 6379)
    jedis.flushDB()

    val result = JSONObject(mapOf("username" to "hp", "password" to "hhh")).toJSONString()
    val multi = jedis.multi()
    try {
        multi.set("user1", result)
        multi.set("user2", result)
        multi.set("r", (1/0).toString())
        multi.exec()
    } catch (e: Exception) {
        multi.discard()
        e.printStackTrace()
    } finally {
        println(jedis.get("user1"))
        println(jedis.get("user2"))
        jedis.close()
    }
}