package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisPool {
    private static JedisPool pool; //jedis连接池
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
    private static String redisIp = PropertiesUtil.getProperty("redis.ip");
    private static Integer redisPort = Integer.parseInt(
            PropertiesUtil.getProperty("redis.port"));
    private static void initPool() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        /* 连接耗尽时是否阻塞：false抛出异常，true阻塞直到超时 */
        config.setBlockWhenExhausted(true);
        pool = new JedisPool(config, redisIp, redisPort, 1000 * 2);
    }

    static {
        initPool();
    }

    public static Jedis getJedis() {
        return pool.getResource();
    }

    public static void returnBrokenResource(Jedis jedis) {
        pool.returnBrokenResource(jedis);
    }

    public static void returnResource(Jedis jedis) {
        pool.returnResource(jedis);
    }

    public static void main(String[] args) {
        Jedis jedis = pool.getResource();
        jedis.set("wqx", "0616");
        returnResource(jedis);
        pool.destroy(); //临时调用销毁连接池中的所有连接
        System.out.println("program is end");
    }
}
