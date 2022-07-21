package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RedisShardedPool {
    private static ShardedJedisPool pool; //sharded jedis连接池
    private static Integer maxTotal = Integer.parseInt(
            PropertiesUtil.getProperty("redis.max.total", "20")); //最大连接数
    private static Integer maxIdle = Integer.parseInt(
            PropertiesUtil.getProperty("redis.max.idle", "10")); //在jedispool中jedis实例为idle状态的最大个数
    private static Integer minIdle = Integer.parseInt(
            PropertiesUtil.getProperty("redis.min.idle", "2")); //在jedispool中jedis实例为idle状态的最小个数
    private static boolean testOnBorrow = Boolean.parseBoolean(
            PropertiesUtil.getProperty("redis.test.borrow", "true")); //在borrow一个jedis实例的时候，是否要进行验证操作，true则可用
    private static boolean testOnReturn = Boolean.parseBoolean(
            PropertiesUtil.getProperty("redis.test.return", "true")); //在return一个jedis实例的时候，是否要进行验证操作，true则放回可用
    private static String redis1Ip = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redis1Port = Integer.parseInt(
            PropertiesUtil.getProperty("redis1.port"));
    private static String redis2Ip = PropertiesUtil.getProperty("redis2.ip");
    private static Integer redis2Port = Integer.parseInt(
            PropertiesUtil.getProperty("redis2.port"));

    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        /* 连接耗尽时是否阻塞：false抛出异常，true阻塞直到超时 */
        config.setBlockWhenExhausted(true);

        JedisShardInfo info1 = new JedisShardInfo(redis1Ip, redis1Port, 1000 * 2);
        JedisShardInfo info2 = new JedisShardInfo(redis2Ip, redis2Port, 1000 * 2);

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<>(2);
        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);

        pool = new ShardedJedisPool(config, jedisShardInfoList, Hashing.MURMUR_HASH,
                BinaryShardedJedis.DEFAULT_KEY_TAG_PATTERN);
    }

    static {
        initPool();
    }

    public static ShardedJedis getJedis() {
        return pool.getResource();
    }

    public static void returnBrokenResource(ShardedJedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    public static void returnResource(ShardedJedis jedis) {
        pool.returnResource(jedis);
    }

    public static void main(String[] args) {
        ShardedJedis jedis = pool.getResource();
        for (int i = 0; i < 20; i++) {
            jedis.set("key" + i, "value" + i);
        }
        returnResource(jedis);
//        pool.destroy(); //临时调用销毁连接池中的所有连接
        System.out.println("program is end");
    }
}
