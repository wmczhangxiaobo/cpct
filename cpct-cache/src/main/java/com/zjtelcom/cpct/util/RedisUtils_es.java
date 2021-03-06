package com.zjtelcom.cpct.util;

import com.ctg.itrdc.cache.pool.CtgJedisPool;
import com.ctg.itrdc.cache.pool.CtgJedisPoolConfig;
import com.ctg.itrdc.cache.pool.CtgJedisPoolException;
import com.ctg.itrdc.cache.pool.ProxyJedis;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisPoolConfig;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *  集团es存储的redis配置  主要用来和bss因子实时查询和批量查询公用数据而增加
 */
@Service
public class RedisUtils_es {

    @Value("${redisConfig_ES.ip}")
    private String redisIp;

    @Value("${redisConfig_ES.port}")
    private Integer redisPort;

    @Value("${redisConfig_ES.database}")
    private Integer redisDatabase;

    @Value("${redisConfig_ES.password}")
    private String redisPassword;

//    private String redisIp="134.96.231.228";
//
//    private Integer redisPort=40201;
//
//    private Integer redisDatabase=4970;
//
//    private String redisPassword="bss_cpct_common_user#bss_cpct_common_user123";


//    private String redisIp="134.108.0.42,134.108.0.43";
//
//    private Integer redisPort=9051;
//
//    private Integer redisDatabase=0;
//
//    private String redisPassword="CRM_CPCP_003_USER#Bsscpc!1n";

    /**
     * 写入缓存
     *
     * @param key
     * @param value
     * @return
     */
    public boolean set(final String key, Object value) {
        boolean result = false;
        try {
            // 改造后方法
            result = setRedis(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * hash存储Redis
     * @param key
     * @param field
     * @param value
     * @return
     */
    public boolean hset(final String key, String field, Object value) {
        boolean result = false;
        CtgJedisPool ctgJedisPool = initCatch();
        try {
            ProxyJedis jedis = new ProxyJedis();
            try {
                jedis = ctgJedisPool.getResource();
                jedis.hset(key, field, serialize(value));
                result = true;
            } catch (Exception e) {
                System.out.println("REDIShset*********" + key);
                e.printStackTrace();
            } finally {
                jedis.close();
            }
            ctgJedisPool.close();
        } catch (Exception e) {
            System.out.println("REDIShset2*********" + key);
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 更换集团redis方法
     *
     * @param key
     * @param value
     * @return
     */
    public boolean setRedis(final String key, Object value) {
        boolean result = false;

        CtgJedisPool ctgJedisPool = initCatch();
        try {
            ProxyJedis jedis = new ProxyJedis();
            try {
                jedis = ctgJedisPool.getResource();
                //sendCommand 可能会抛出 运行时异常
                jedis.set(key, serialize(value));
                //sendCommand 可能会抛出 运行时异常
                result = true;
            } catch (Throwable je) {
                je.printStackTrace();
            } finally {
                jedis.close();
            }
            ctgJedisPool.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 写入缓存设置时效时间
     *
     * @param key
     * @param value
     * @return
     */
//    public boolean set(final String key, Object value, Long expireTime, TimeUnit timeUnit) {
//        boolean result = false;
//        try {
//            ValueOperations<Serializable, Object> operations = redisTemplate.opsForValue();
//            operations.set(key, value);
//            redisTemplate.expire(key, expireTime, timeUnit);
//            result = true;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return result;
//    }


    /**
     * 判断缓存中是否有对应的value
     *
     * @param key
     * @return
     */
    public boolean exists(final String key) {
        return existsRedis(key);
    }


    /**
     * 更换集团redis方法
     *
     * @param key
     * @return
     */
    public boolean existsRedis(final String key) {
        CtgJedisPool ctgJedisPool = initCatch();
        boolean result = false;
        try {
            ProxyJedis jedis = new ProxyJedis();
            try {
                jedis = ctgJedisPool.getResource();
                result = jedis.exists(key);
            } catch (Throwable je) {
                je.printStackTrace();
            } finally {
                jedis.close();
                ctgJedisPool.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 读取缓存
     *
     * @param key
     * @return
     */
    public Object get(final String key) {
        Object result = null;
        // 改造后方法
        result = getRedis(key);
        return result;
    }

    public boolean del(final  String key){
        boolean result = false;
        result = delRedis(key);
        return result;
    }


    /**
     * 更换集团redis方法
     *
     * @param key
     * @return
     */
    public Object getRedis(final String key) {
        CtgJedisPool ctgJedisPool = initCatch();
        Object result = null;
        try {
            ProxyJedis jedis = new ProxyJedis();
            try {
                jedis = ctgJedisPool.getResource();
                if(jedis.exists(key)) {
                    result = unserizlize(jedis.get(key));
                }
            } catch (Throwable je) {
                je.printStackTrace();
            } finally {
                jedis.close();
            }
            ctgJedisPool.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    /**
     * 更换集团redis方法
     *
     * @param key
     * @return
     */
    private boolean delRedis(final String key) {
        CtgJedisPool ctgJedisPool = initCatch();
        boolean result = false;
        try {
            ProxyJedis jedis = new ProxyJedis();
            try {
                jedis = ctgJedisPool.getResource();
                if(jedis.exists(key)) {
                    jedis.del(key);
                    result = true;
                }
            } catch (Throwable je) {
                je.printStackTrace();
            } finally {
                jedis.close();
                ctgJedisPool.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 更换集团redis方法
     *
     * @param key
     * @return
     */
    public void remove(final String key) {

        CtgJedisPool ctgJedisPool = initCatch();
        boolean result = false;
        try {
            ProxyJedis jedis = new ProxyJedis();
            try {
                jedis = ctgJedisPool.getResource();
                if (jedis.exists(key)){
                    jedis.del(key);
                }
            } catch (Throwable je) {
                je.printStackTrace();
            } finally {
                jedis.close();
                ctgJedisPool.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 哈希 添加
     *
     * @param key
     * @param hashKey
     * @param value
     */
//    public void hmSet(String key, Object hashKey, Object value) {
//        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
//        hash.put(key, hashKey, value);
//    }

    /**
     * 哈希获取数据
     *
     * @param key
     * @param hashKey
     * @return
     */
//    public Object hmGet(String key, Object hashKey) {
//        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
//        return hash.get(key, hashKey);
//    }

    /**
     * 哈希获取数据
     *
     * @param key
     * @return Map<HK, HV>
     */
//    public Object hKeys(String key) {
//        HashOperations<String, Object, Object> hash = redisTemplate.opsForHash();
//        return hash.entries(key);
//    }

    /**
     * 列表添加
     *
     * @param k
     * @param v
     */
//    public void lPush(String k, Object v) {
//        ListOperations<String, Object> list = redisTemplate.opsForList();
//        list.rightPush(k, v);
//    }

    /**
     * 列表获取
     *
     * @param k
     * @param l
     * @param l1
     * @return
     */
//    public List<Object> lRange(String k, long l, long l1) {
//        ListOperations<String, Object> list = redisTemplate.opsForList();
//        return list.range(k, l, l1);
//    }

    /**
     * 集合添加
     *
     * @param key
     * @param value
     */
//    public void add(String key, Object value) {
//        SetOperations<String, Object> set = redisTemplate.opsForSet();
//        set.add(key, value);
//    }

    /**
     * 集合获取
     *
     * @param key
     * @return
     */
//    public Set<Object> setMembers(String key) {
//        SetOperations<String, Object> set = redisTemplate.opsForSet();
//        return set.members(key);
//    }

    /**
     * 有序集合添加
     *
     * @param key
     * @param value
     * @param scoure
     */
//    public void zAdd(String key, Object value, double scoure) {
//        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
//        zset.add(key, value, scoure);
//    }

    /**
     * 有序集合获取
     *
     * @param key
     * @param scoure
     * @param scoure1
     * @return
     */
//    public Set<Object> rangeByScore(String key, double scoure, double scoure1) {
//        ZSetOperations<String, Object> zset = redisTemplate.opsForZSet();
//        return zset.rangeByScore(key, scoure, scoure1);
//    }
//
//    /**
//     * 查询该key下所有值
//     *
//     * @param key 查询的key
//     * @return Map<HK, HV>
//     */
//    public Object hget(String key) {
//        return hashOperations.entries(key);
//    }


    /**
     * 通过表达式匹配获取所有key
     */
//    public Set<Object> keys(String pattern) {
//        Set<Object> set = redisTemplate.keys(pattern);
//        return set;
//    }


    private CtgJedisPool initCatch() {
        List<HostAndPort> hostAndPortList = new ArrayList();
        // 接入机的ip和端口号
        String[] strArr = redisIp.split(",");
        for (String str : strArr) {
            HostAndPort host = new HostAndPort(str, redisPort);
            hostAndPortList.add(host);
        }
        GenericObjectPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(0); //最大空闲连接数
        poolConfig.setMaxTotal(5); // 最大连接数（空闲+使用中），不超过应用线程数，建议为应用线程数的一半
        poolConfig.setMinIdle(1); //保持的最小空闲连接数
        poolConfig.setMaxWaitMillis(3000);

        CtgJedisPoolConfig config = new CtgJedisPoolConfig(hostAndPortList);

        config.setDatabase(redisDatabase).setPassword(redisPassword).setPoolConfig(poolConfig).setPeriod(1000).setMonitorTimeout(100);

        CtgJedisPool pool = new CtgJedisPool(config);

        return pool;
    }


    public static void main(String[] args) throws CtgJedisPoolException {


        List<HostAndPort> hostAndPortList = new ArrayList();
        // 接入机的ip和端口号
        HostAndPort host = new HostAndPort("134.96.231.228", 40201);
        hostAndPortList.add(host);

        GenericObjectPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(5); //最大空闲连接数
        poolConfig.setMaxTotal(10); // 最大连接数（空闲+使用中），不超过应用线程数，建议为应用线程数的一半
        poolConfig.setMinIdle(5); //保持的最小空闲连接数
        poolConfig.setMaxWaitMillis(3000);

        CtgJedisPoolConfig config = new CtgJedisPoolConfig(hostAndPortList);

        config.setDatabase(4970).setPassword("bss_cpct_common_user#bss_cpct_common_user123").setPoolConfig(poolConfig).setPeriod(1000).setMonitorTimeout(100);

        CtgJedisPool pool = new CtgJedisPool(config);

        ProxyJedis jedis = new ProxyJedis();
        try {
            jedis = pool.getResource();
            //sendCommand 可能会抛出 运行时异常
            //jedis.set("test", "123");
            //sendCommand 可能会抛出 运行时异常
            //jedis.get("test");
            System.out.println(unserizlize(jedis.get("mktCampaignResp_test")));
            jedis.close();
        } catch (Throwable je) {
            je.printStackTrace();
            jedis.close();
        }
        pool.close();

    }

    /**
     * 对象序列化为字符串
     *
     * @param obj
     * @return
     */

    public static String serialize(Object obj) {
        String serStr = null;
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(obj);
            serStr = byteArrayOutputStream.toString("ISO-8859-1");
            serStr = java.net.URLEncoder.encode(serStr, "UTF-8");
            objectOutputStream.close();
            byteArrayOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serStr;
    }

    /**
     * 字符串反序列化为对象
     *
     * @param serStr
     * @return
     */
    public static Object unserizlize(String serStr) {
        Object newObj = null;
        try {
            if(serStr != null) {
                String redStr = java.net.URLDecoder.decode(serStr, "UTF-8");
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(redStr.getBytes("ISO-8859-1"));
                ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
                newObj = objectInputStream.readObject();
                objectInputStream.close();
                byteArrayInputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newObj;
    }
}