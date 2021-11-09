package com.atguigu.service.impl;

import com.atguigu.cache.ShopCache;
import com.atguigu.constant.RedisConst;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuImage;
import com.atguigu.entity.SkuInfo;
import com.atguigu.exception.SleepUtils;
import com.atguigu.mapper.SkuInfoMapper;
import com.atguigu.mapper.SkuSalePropertyValueMapper;
import com.atguigu.service.SkuDetailService;
import com.atguigu.service.SkuImageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class SkuDetailServiceImpl implements SkuDetailService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageService skuImageService;

    @Autowired
    private SkuSalePropertyValueMapper skuSalePropertyValueMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @ShopCache(prefix = "sku:info:")
    @Override
    public SkuInfo getById(Long skuId) {
//        SkuInfo skuInfo = getSkuInfoFromRedis(skuId);
//        return skuInfo;
        SkuInfo skuInfoFromDb = getSkuInfoFromDb(skuId);
        return skuInfoFromDb;
    }

    public SkuInfo getSkuInfoFromRedis(Long skuId) {
        // 拼接skuKey字符串
        String skuKey = RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKUKEY_SUFFIX;
        // 从redis中取出SkuInfo
        SkuInfo skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
        // 判断SkuInfo是否有值,没有需要从数据库中获取，添加分布式锁
        if (skuInfo == null){
            // 拿到一个UUID作为set的值，防止误删别的线程的锁
            String uuid = UUID.randomUUID().toString();
            // 拼接锁键名
            String  lockKey =RedisConst.SKUKEY_PREFIX+skuId+RedisConst.SKULOCK_SUFFIX;
            // 创建锁，接收布尔值判断是否有线程正在占用资源，保证线程安全
            Boolean acquireLock = redisTemplate.opsForValue().setIfAbsent(lockKey, uuid, 3, TimeUnit.SECONDS);
            if (acquireLock){
                // 从数据库中取出所需信息
                SkuInfo skuInfoFromDb = getSkuInfoFromDb(skuId);
                // 防止缓存穿透
                if (skuInfoFromDb == null){
                    // 设置一个空对象，存入Redis，避免高并发向数据库查不存在的信息
                    SkuInfo emptySkuInfo = new SkuInfo();
                    redisTemplate.opsForValue().set(skuKey,emptySkuInfo,RedisConst.USERKEY_TIMEOUT,TimeUnit.SECONDS);
                    return emptySkuInfo;
                }
                // 将数据存入Redis
                redisTemplate.opsForValue().set(skuKey,skuInfoFromDb,RedisConst.USERKEY_TIMEOUT,TimeUnit.SECONDS);
                // 释放锁的逻辑，需要保证判断锁有没有过期和删除锁的两步操作具有原子性，使用lua脚本。
                String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
                redisScript.setScriptText(luaScript);
                redisScript.setResultType(Long.class);
                // 执行脚本
                redisTemplate.execute(redisScript, Arrays.asList(lockKey),uuid);
                return skuInfoFromDb;
            }else{
                // 如果锁被占用了，就睡眠一段时间
                SleepUtils.sleepMills(50);
                // 然后自旋调用
                return getById(skuId);
            }
        }else{
            // redis中有数据就直接返回
            return skuInfo;
        }
    }

    private SkuInfo getSkuInfoFromDb(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        QueryWrapper<SkuImage> skuImageQueryWrapper = new QueryWrapper<>();
        skuImageQueryWrapper.eq("sku_id",skuId);
        List<SkuImage> imageList = skuImageService.list(skuImageQueryWrapper);
        skuInfo.setSkuImageList(imageList);
        return skuInfo;
    }

    @ShopCache(prefix = "sku:price:")
    @Override
    public BigDecimal getSkuPrice(Long skuId) {
//        BigDecimal price = getBigDecimalFromRedis(skuId);
//        return price;
        SkuInfo skuInfoFromDb = skuInfoMapper.selectById(skuId);
        return skuInfoFromDb.getPrice();
    }

    private BigDecimal getBigDecimalFromRedis(Long skuId) {
        // 拼接取值字符串从Redis中拿Price信息
        String priceKey = RedisConst.SKUPRICE_PREFIX + skuId + RedisConst.SKUPRICE_SUFFIX;
        BigDecimal skuPrice = (BigDecimal) redisTemplate.opsForValue().get(priceKey);
        // 如果缓存中没有则从数据库查
        if (skuPrice ==null) {
            String lockKey = RedisConst.SKUPRICE_PREFIX + skuId + RedisConst.SKUPRICE_SUFFIX;
            RLock lock = redissonClient.getLock(lockKey);
            try {
                boolean acquireLock = lock.tryLock(RedisConst.WAITTIN_GET_LOCK_TIME, RedisConst.LOCK_EXPIRE_TIME, TimeUnit.SECONDS);
                if (acquireLock) {
                    SkuInfo skuInfoFromDb = skuInfoMapper.selectById(skuId);
                    if (skuInfoFromDb == null) {
                        // 设置一个空对象，存入Redis，避免高并发向数据库查不存在的信息
                        redisTemplate.opsForValue().set(priceKey, new BigDecimal(""), RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
                        return new BigDecimal("");
                    }
                    BigDecimal fromDbPrice = skuInfoFromDb.getPrice();
                    redisTemplate.opsForValue().set(priceKey, fromDbPrice, RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
                    return fromDbPrice;
                }
            } catch(Exception e){
                e.printStackTrace();
            } finally{
                lock.unlock();
            }
        }else {
            return skuPrice;
        }
        return null;
    }
    @ShopCache(prefix = "sku:spuSaleProperty:")
    @Override
    public List<ProductSalePropertyKey> getSpuSalePropertyAndSelected(Long productId, Long skuId) {
        List<ProductSalePropertyKey> productSalePropertyKeyList  = skuInfoMapper.getSpuSalePropertyAndSelected(productId,skuId);
        return productSalePropertyKeyList;
    }

    @ShopCache(prefix = "sku:salePropertyAndSkuIdMapping:")
    @Override
    public Map getSalePropertyAndSkuIdMapping(Long productId) {
        HashMap<Object, Object> retMap = new HashMap<>();
        List<Map> valueIdMap =  skuSalePropertyValueMapper.getSalePropertyAndSkuIdMapping(productId);
        if (valueIdMap !=null){
            for (Map map : valueIdMap) {
                retMap.put(map.get("sale_property_value_id"),map.get("sku_id"));
            }
        }
        return retMap;
    }

}
