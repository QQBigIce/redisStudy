package com.hp

import redis.clients.jedis.Jedis

fun main(args: Array<String>) {
    val jedis = Jedis("47.100.3.79", 6379)
    println(jedis.ping())
    val result = """
        |清空数据: ${jedis.flushDB()}
        |判断某个键是否存在: ${jedis.exists("username")}
        |新增<'username', 'hp'>的键值对: ${jedis.set("username", "hp")}
        |新增<'password', 'password'>的键值对: ${jedis.set("password", "password")}
        |系统的所有的键如下: 
        |${jedis.keys("*")}
        |删除键password: ${jedis.del("password")}
        |判断键password是否存在: ${jedis.exists("password")}
        |查看键username所存储的值的类型: ${jedis.type("username")}
        |随机返回key空间的一个: ${jedis.randomKey()}
        |重命名key: ${jedis.rename("username", "name")}
        |取出改后的name: ${jedis.get("name")}
        |按索引查询: ${jedis.select(0)}
        |删除当前选择数据库中的所有key: ${jedis.flushDB()}
        |返回当前数据库中key的数目: ${jedis.dbSize()}
        |删除所有数据库中的所有key: ${jedis.flushAll()}
    """.trimMargin()
    println(result)
}