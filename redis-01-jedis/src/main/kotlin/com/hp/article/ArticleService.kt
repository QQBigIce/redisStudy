package com.hp.article

import redis.clients.jedis.Jedis
import redis.clients.jedis.ZParams

fun main(args: Array<String>) {
    val articleService = ArticleService()
    // 提交20篇文章
    // for (i in 1..20){
    //     articleService.postArticle("user$i", "title$i", "http://www.$i.com")
    // }

    articleService.articleVote("article:1", "user:21")

    println(articleService.getArticles(1))

}

class ArticleService {
    companion object {
        const val ONE_WEEK_IN_SECONDS = 7 * 86400
        const val VOTE_SCORE: Double = 432.0
        const val ARTICLE_PRE_PAGE = 25
        val jedis = Jedis("47.100.3.79", 6379)
    }

    /**
     * 用户投票方法
     * @param article 文章Hash键名 article:123456
     * @param user 用户键名 user:1234
     */
    fun articleVote(article: String, user: String): Unit {
        // 当前时间往前偏移一周, 如果文章发布时间在偏移时间之前, 则文章已过期
        val offset = System.currentTimeMillis() - ONE_WEEK_IN_SECONDS
        if (jedis.zscore("time:", article) < offset) return

        // 如果此文章的已投票用户里面添加成功, 则评分和票数也自增
        val articleId = article.split(":").last()
        if (jedis.sadd("voted:$articleId", user).toInt() != 0) {
            jedis.zincrby("score:", VOTE_SCORE, article)
            jedis.hincrBy(article, "votes", 1)
        }
    }

    /**
     * 用户投反对票方法(差评)
     * @param article 文章Hash键名 article:123456
     * @param user 用户键名 user:1234
     */
    fun articleAgainstVote(article: String, user: String): Unit {
        val offset = System.currentTimeMillis() - ONE_WEEK_IN_SECONDS
        if (jedis.zscore("time:", article) < offset) return

        val articleId = article.split(":")[-1]
        if (jedis.srem("voted:$articleId", user).toInt() != 0){
            jedis.zincrby("score:", -VOTE_SCORE, article)
            jedis.hincrBy(article, "votes", -1)
        }
    }

    /**
     * 用户发布文章方法, 发布文章默认给自己投1票
     * @param user 用户键名
     * @param title 文章标题
     * @param link 文章链接
     */
    fun postArticle(user: String, title: String, link: String): Long {
        // 生成文章ID
        val articleId = jedis.incr("articleId:")

        // 把作者添加到已投票用户集合, 并给集合设置时期时间一星期
        val voted = "voted:$articleId"
        jedis.sadd(voted, user)
        jedis.expire(voted, ONE_WEEK_IN_SECONDS)

        // 生成文章hash的键和值并添加到redis
        val article = "article:$articleId"
        val now = System.currentTimeMillis()
        val articleVal = mapOf<String, String>(
                "title" to title,
                "link" to link,
                "poster" to user,
                "time" to "$now",
                "votes" to "1"
        ).toMutableMap()
        jedis.hmset(article, articleVal)

        // 分值初始化为时间戳 + 1票的分值
        // 时间初始化为时间戳
        jedis.zadd("score:", now + VOTE_SCORE, article)
        jedis.zadd("time:", now.toDouble(), article)
        return articleId
    }

    /**
     * 获得文章页面
     * @param page 页码
     * @param order 排序依据 默认是评分
     */
    fun getArticles(page: Int, order: String = "score:"): List<Map<String, String>> {
        // 获得开始和结束的索引
        val start = ((page - 1) * 25).toLong()
        val end = start + ARTICLE_PRE_PAGE - 1

        // 从评分集合中获得多个符合的id
        val ids = jedis.zrevrange(order, start, end)
        // 拿到这些id对应的文章
        val articleList = ArrayList<Map<String, String>>()
        for (id in ids) {
            val articleData = jedis.hgetAll(id)
            articleData["id"] = id
            articleList.add(articleData)
        }
        return articleList
    }

    /**
     * 修改文章的分类, 每个类别为一个set
     * @param articleId 文章id
     * @param toAddGroups 要添加的分类
     * @param toRemoveGroups 要移除的分类
     */
    fun addRemoveGroups(articleId: String, toAddGroups: Array<String>? = null, toRemoveGroups: Array<String>? = null): Unit {
        val article = "article:$articleId"
        toAddGroups?.forEach { jedis.sadd("group:$it", article) }
        toRemoveGroups?.forEach { jedis.srem("group:$it", article) }
    }

    /**
     * 获得分组中的所有排序好的文章
     * 把所有根据排序依据排序好的文章的zSet和群组内文章的set做交集运算, 按从大到小排序, 并设置缓存时间为60s
     * @param group 目标群组
     * @param page 页码
     * @param order 排序依据
     * @return 目标群组排序好的文章
     */
    fun getGroupArticles(group: String, page: Int, order: String = "score:"): List<Map<String, String>> {
        // 缓存交集ZSet
        val key = order + group
        if (!jedis.exists(key)) {
            // 求二个集合的交集并添加到redis
            jedis.zinterstore(key, ZParams().aggregate(ZParams.Aggregate.MAX), "group:$group", order)
            jedis.expire(key, 60)
        }
        return getArticles(page, order)
    }
}