package com.hp.utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit

@Component
class RedisUtil {

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, Any>

    private val LOGGER: Logger = LoggerFactory.getLogger(this.javaClass)

    /**
     * 设定缓存失效时间
     * @param key 键
     * @param time 时间(秒)
     */
    fun expire(key: String, time: Long): Boolean {
        return try {
            if (time > 0) {
                redisTemplate.expire(key, time, TimeUnit.SECONDS);
            }
            true
        } catch (e: Exception) {
            LOGGER.error("设定过期时间失败")
            e.printStackTrace()
            false
        }
    }

    /**
     * 根据key 获取过期时间
     * @param key 键(不能为null)
     * @return 时间(秒), 返回0代表永久有效
     */
    fun getExpire(key: String): Long {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS)
    }

    /**
     * 判断key是否存在
     * @param key 键
     * @return true 存在 false 不存在
     */
    fun hasKey(key: String): Boolean {
        try {
            return redisTemplate.hasKey(key)
        } catch (e: Exception) {
            LOGGER.error("判断key存在方法: 发生异常")
            e.printStackTrace()
            return false
        }
    }

    /**
     * 删除指定的键
     * @param keys 要删除的键的数组
     */
    fun del(vararg keys: String): Boolean {
        if (keys.isEmpty())
            return false
        keys.takeIf { it.size == 1 }
                ?.also { redisTemplate.delete(it[0]) }
                ?: apply { redisTemplate.delete(keys.asList()) }
        return true
    }

    // **********************************String***************************************

    /**
     * 普通缓存获取
     * @param key 键
     */
    fun get(key: String): Any? {
        return if (key.isEmpty()) null else redisTemplate.opsForValue().get(key)
    }

    /**
     * 普通缓存存入, 可选过期时间
     * @param key 键
     * @param value 值
     * @param time 时间
     */
    fun set(key: String, value: Any, time: Long = 0): Boolean {
        if (time == 0.toLong()) {
            redisTemplate.opsForValue().set(key, value)
        } else {
            redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS)
        }
        return true
    }

    /**
     * 递增
     * @param key 键
     * @param delta 递增因子
     * @throws RuntimeException 递增因子必须要大于0
     */
    @Throws(RuntimeException::class)
    fun incr(key: String, delta: Long): Long? {
        if (delta <= 0)
            throw RuntimeException("递增因子必须大于0")
        return redisTemplate.opsForValue().increment(key, delta)
    }

    /**
     * 递减
     * @param key 键
     * @param delta 递减因子
     * @throws RuntimeException 递减因子必须要大于0
     */
    @Throws(RuntimeException::class)
    fun decr(key: String, delta: Long): Long? {
        if (delta <= 0)
            throw RuntimeException("递减因子必须大于0")
        return redisTemplate.opsForValue().decrement(key, delta)
    }

    // **********************************Map***************************************

    /**
     * 获取key 对应的Hash的某个键的值
     * @param key 键
     * @param item 项
     */
    fun hGet(key: String, item: String): Any? {
        return redisTemplate.opsForHash<String, Any>().get(key, item)
    }

    /**
     * 获取key对应的Hash实体
     * @param key 键
     */
    fun hmGet(key: String): Map<Any, Any>? {
        return redisTemplate.opsForHash<Any, Any>().entries(key)
    }

    /**
     * 存储HashMap的一对k-v到key中
     * @param key 键
     * @param item 项
     * @param value 值
     * @param time 可选的过期时间
     */
    fun hSet(key: String, item: String, value: Any, time: Long = 0): Boolean {
        redisTemplate.opsForHash<Any, Any>().put(key, item, value)
        if (time > 0)
            expire(key, time)
        return true

    }

    /**
     * 存储HashMap到key中
     * @param key 键
     * @param map 值的实体k-v
     * @param time 可选的过期时间
     */
    fun hmSet(key: String, map: Map<Any, Any>, time: Long = 0): Boolean {
        redisTemplate.opsForHash<Any, Any>().putAll(key, map)
        if (time > 0)
            expire(key, time)
        return true
    }

    /**
     * 删除指定键中的某些项
     * @param key 键
     * @param item 项 可以为多个
     */
    fun hDel(key: String, vararg item: Any): Unit {
        redisTemplate.opsForHash<Any, Any>().delete(key, item)
    }

    /**
     * 查看指定键中存储的hashMap的项是否存在
     * @param key 键
     * @param item 项
     */
    fun hHasKey(key: String, item: String): Boolean {
        return redisTemplate.opsForHash<Any, Any>().hasKey(key, item)
    }

    /**
     * Hash 自增(自减)
     * @param key 键
     * @param item 项
     * @param by 递减因子
     */
    fun hIncr(key: String, item: String, by: Double): Double {
        return redisTemplate.opsForHash<Any, Any>().increment(key, item, by)
    }

    // **********************************Set***************************************

    /**
     * 根据key获取Set中所有的值
     * @param key 键
     */
    fun sGet(key: String): MutableSet<Any>? {
        return redisTemplate.opsForSet().members(key)
    }

    /**
     * 查询key对应的Set中是否存在value
     * @param key 键
     * @param value 值
     */
    fun sHasKey(key: String, value: Any): Boolean? {
        return redisTemplate.opsForSet().isMember(key, value)
    }

    /**
     * 把数据存入Set缓存
     * @param key 键
     * @param values 值的数组
     * @return 存入的个数
     */
    fun sSet(key: String, vararg values: Any, time: Long): Long? {
        val count = redisTemplate.opsForSet().add(key, values)
        if (time > 0)
            expire(key, time)
        return count

    }

    /**
     * 获取set缓存的长度
     * @param key 键
     * @return 长度
     */
    fun sGetSetSize(key: String): Long? {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 移除set缓存里面的某些值
     * @param key 键
     * @param values 值的数组
     * @return 移除的值的个数
     */
    fun sRemove(key: String, vararg values: Any): Long? {
        return redisTemplate.opsForSet().remove(key, values)
    }

    // **********************************List***************************************

    /**
     * 获得List缓存里面的闭区间内的值
     * @param key 键
     * @param start 区间开始
     * @param end 区间结束
     */
    fun lGet(key: String, start: Long, end: Long): List<Any>? {
        return redisTemplate.opsForList().range(key, start, end)
    }

    /**
     * 根据index获取List中的某个值
     * @param key 键
     * @param index 下标
     */
    fun lGetByIndex(key: String, index: Long): Any? {
        return redisTemplate.opsForList().index(key, index)
    }

    /**
     * 如果不存在此key, 则新建此key和List, 把元素添加到List的尾部
     * 如果存在, 则直接添加到List的尾部
     * @param key 键
     * @param value 值
     * @param time 可选的过期时间
     */
    fun lSet(key: String, value: Any, time: Long = 0): Boolean {
        try {
            redisTemplate.opsForList().rightPush(key, value)
            if (time > 0)
                expire(key, time)
            return true
        } catch (e: Exception) {
            LOGGER.error("List 添加/入栈 错误!")
            e.printStackTrace()
            return false
        }
    }

    /**
     * 把整个List集合放入缓存
     * @param key 键
     * @param value List集合
     * @param time 可选的过期时间
     */
    fun lSet(key: String, value: List<Any>?, time: Long = 0): Boolean {
        try {
            redisTemplate.opsForList().rightPushAll(key, value)
            if (time > 0)
                expire(key, time)
            return true
        } catch (e: Exception) {
            LOGGER.error("List缓存 添加/入栈 错误")
            e.printStackTrace()
            return false
        }
    }

    /**
     * 根据下标修改List的对应值
     * @param key 键
     * @param index 下标
     * @param value 值
     */
    fun lUpdateByIndex(key: String, index: Long, value: Any): Boolean {
        try {
            redisTemplate.opsForList().set(key, index, value)
            return true
        } catch (e: Exception) {
            LOGGER.error("List UpdateByIndex 错误")
            e.printStackTrace()
            return false
        }
    }

    /**
     * 移除多个为value的值
     * @param key 键
     * @param count 个数
     * @param value 值
     * @return 成功移除的个数
     */
    fun lRemove(key: String, count: Long, value: Any): Long? {
        return redisTemplate.opsForList().remove(key, count, value)
    }

}