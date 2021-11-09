package com.atguigu.cache;

import com.atguigu.constant.RedisConst;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
// 表示该注解是一个切面类
@Aspect
public class ShopCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(com.atguigu.cache.ShopCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint target){
        //首先需要拿到skuKey的前缀和skuId信息拼接skuKey target代表目标类
        // 1.拿到目标类的目标方法
        MethodSignature signature = (MethodSignature) target.getSignature();
        //2.通过目标方法拿到目标方法上面的注解
        ShopCache annotation = signature.getMethod().getAnnotation(ShopCache.class);
        //3.获取注解上面的前缀信息
        String prefix = annotation.prefix();
        //4.获取方法上的参数
        Object[] params = target.getArgs();


        // 拼接取值字符串从Redis中拿Price信息
        String skuKey = prefix + Arrays.asList(params).toString();
        Object retVal = redisTemplate.opsForValue().get(skuKey);
        // 如果缓存中没有则从数据库查
        if (retVal ==null) {
            String lockKey = skuKey + RedisConst.SKULOCK_SUFFIX;
            RLock lock = redissonClient.getLock(lockKey);
            try {
                boolean acquireLock = lock.tryLock(RedisConst.WAITTIN_GET_LOCK_TIME, RedisConst.LOCK_EXPIRE_TIME, TimeUnit.SECONDS);
                if (acquireLock) {
                    Object retValFromDb = target.proceed();
                    if (retValFromDb == null) {
                        // 设置一个空对象，存入Redis，避免高并发向数据库查不存在的信息
                        Object emptyRetVal = new Object();
                        redisTemplate.opsForValue().set(skuKey, emptyRetVal, RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
                        return emptyRetVal;
                    }
                    redisTemplate.opsForValue().set(skuKey, retValFromDb, RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
                    return retValFromDb;
                }
            } catch(Exception e){
                e.printStackTrace();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            } finally{
                lock.unlock();
            }
        }else {
            return retVal;
        }
        return null;
    }
}
