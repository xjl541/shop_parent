package com.atguigu.service.impl;

import com.atguigu.client.ProductFeignClient;
import com.atguigu.constant.RedisConst;
import com.atguigu.entity.CartInfo;
import com.atguigu.entity.SkuInfo;
import com.atguigu.mapper.CartInfoMapper;
import com.atguigu.service.AsyncCartInfoService;
import com.atguigu.service.CartInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 购物车表 用户登录系统时更新冗余 服务实现类
 * </p>
 *
 * @author xiejl
 * @since 2021-11-09
 */
@Service
public class CartInfoServiceImpl extends ServiceImpl<CartInfoMapper, CartInfo> implements CartInfoService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AsyncCartInfoService asyncCartInfoService;

    @Override
    public void addCart(Long skuId, Integer skuNum, String userId) {
        // 获取用户Id,根据skuid查询到sku信息，将其赋值给cartInfo，保存到数据库和redis。
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("sku_id",skuId);
        CartInfo existCartInfo = baseMapper.selectOne(wrapper);
        // 如果购物车当中有此件商品就更改商品数量，如果没有就添加
        if (existCartInfo != null){
            existCartInfo.setSkuNum(existCartInfo.getSkuNum()+skuNum);
            existCartInfo.setCartPrice(productFeignClient.getSkuPrice(skuId));
//            baseMapper.updateById(existCartInfo);
            asyncCartInfoService.updateCartInfo(existCartInfo);
        }else {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

            existCartInfo = new CartInfo();
            existCartInfo.setUserId(userId);
            existCartInfo.setSkuId(skuId);
            existCartInfo.setCartPrice(skuInfo.getPrice());
            existCartInfo.setSkuNum(skuNum);
            existCartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            existCartInfo.setSkuName(skuInfo.getSkuName());
            // 默认值1 表示被选中
            existCartInfo.setIsChecked(1);

            // 插入到数据库
//            baseMapper.insert(existCartInfo);
            asyncCartInfoService.insertCartInfo(existCartInfo);
        }
        // 将查询结果信息添加到redis中
        String userCartKey = getUserCartKey(userId);
        redisTemplate.boundHashOps(userCartKey).put(skuId.toString(),existCartInfo);
        setCartKeyExpire(userCartKey);
    }

    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {
        List<CartInfo> cartInfoList = null;
        // 如果没有登陆，直接获取临时用户的购物车列表
        if (!StringUtils.isEmpty(userTempId)){
            cartInfoList = getCartInfoListByUserId(userTempId);
        }
        // 如果登陆了并且临时用户有购物车数据，则将两者合并。临时用户没有购物车数据，则直接获取登陆用户的购物车列表
        if (!StringUtils.isEmpty(userId)){
            // 先获取临时用户的购物车数据
            List<CartInfo> tempCartInfoList = getCartInfoListByUserId(userTempId);
            if (!CollectionUtils.isEmpty(tempCartInfoList) && tempCartInfoList.size() > 0){
                // 合并
                cartInfoList = mergeCartInfoList(userId, tempCartInfoList);
                // 删除redis中临时数据
                deleteNoLoginCartInfoList(userTempId);

            }else {
                 cartInfoList = getCartInfoListByUserId(userId);
            }
        }
        return cartInfoList;
    }

    @Override
    public void checkCart(Long skuId, Integer isCheckd, String userId) {
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("sku_id",skuId);
        CartInfo cartInfo = baseMapper.selectOne(wrapper);
        cartInfo.setIsChecked(isCheckd);
        asyncCartInfoService.updateCartInfo(cartInfo);

        String userCartKey = getUserCartKey(userId);
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(userCartKey);
        if (boundHashOperations.hasKey(skuId.toString())){
            CartInfo cartInfFromRedis = (CartInfo) boundHashOperations.get(skuId.toString());
            cartInfFromRedis.setIsChecked(isCheckd);
            boundHashOperations.put(skuId.toString(),cartInfFromRedis);
            setCartKeyExpire(userCartKey);
        }
    }

    @Override
    public void deleteCart(Long skuId, String userId) {
        asyncCartInfoService.deleteCartInfoBySkuId(skuId,userId);

        String userCartKey = getUserCartKey(userId);
        BoundHashOperations boundHashOperations = redisTemplate.boundHashOps(userCartKey);
        if (boundHashOperations.hasKey(userCartKey)){
            boundHashOperations.delete(skuId.toString());
        }
    }

    @Override
    public List<CartInfo> getSelectedProduct(Long userId) {
        List<CartInfo> retCartInfoList = new ArrayList<>();

        String userCartKey = getUserCartKey(userId.toString());
        List<CartInfo> cartInfoList = redisTemplate.opsForHash().values(userCartKey);
        if (!CollectionUtils.isEmpty(cartInfoList)){
            for (CartInfo cartInfo : cartInfoList) {
                Integer isChecked = cartInfo.getIsChecked();
                if (isChecked == 1){
                    retCartInfoList.add(cartInfo);
                }
            }
        }
        return retCartInfoList;
    }

    private void deleteNoLoginCartInfoList(String userTempId) {
//        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
//        wrapper.eq("user_id",userTempId);
//        baseMapper.delete(wrapper);
        asyncCartInfoService.deleteCartInfoByUserId(userTempId);

        String userCartKey = getUserCartKey(userTempId);
        Boolean aBoolean = redisTemplate.hasKey(userCartKey);
        if (aBoolean){
            redisTemplate.delete(userCartKey);
        }
    }

    // 合并购物车
    private List<CartInfo> mergeCartInfoList(String userId, List<CartInfo> tempCartInfoList) {
        List<CartInfo> userCartInfoList = getCartInfoListByUserId(userId);
        Map<Long, CartInfo> map = userCartInfoList.stream().collect(Collectors.toMap(CartInfo::getSkuId, cartInfo -> cartInfo));
        for (CartInfo cartInfo : tempCartInfoList) {
            if (map.containsKey(cartInfo.getSkuId())){
                CartInfo userCartInfo = map.get(cartInfo.getSkuId());
                userCartInfo.setSkuNum(userCartInfo.getSkuNum() + cartInfo.getSkuNum());
                userCartInfo.setIsChecked(cartInfo.getIsChecked());
//                baseMapper.updateById(userCartInfo);
                asyncCartInfoService.updateCartInfo(userCartInfo);
            }else{
                cartInfo.setUserId(userId);
                baseMapper.updateById(cartInfo);
            }
        }
        List<CartInfo> cartInfoList = queryFromDbToRedis(userId);
        return cartInfoList;
    }


    @Override
    public List<CartInfo> queryFromDbToRedis(String userId) {
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        List<CartInfo> dbCartInfoList = baseMapper.selectList(wrapper);
//        List<CartInfo> dbCartInfoList  = asyncCartInfoService.selectCartInfoList(userId);
        if (CollectionUtils.isEmpty(dbCartInfoList)){
            return dbCartInfoList;
        }

        String userCartKey = getUserCartKey(userId);
        HashMap<String,CartInfo> cartInfoMap = new HashMap<>();
        for (CartInfo cartInfo : dbCartInfoList) {
            cartInfo.setRealTimePrice(productFeignClient.getSkuPrice(cartInfo.getSkuId()));
            cartInfoMap.put(cartInfo.getSkuId().toString(),cartInfo);
        }
        redisTemplate.opsForHash().putAll(userCartKey,cartInfoMap);
        setCartKeyExpire(userCartKey);
        return dbCartInfoList;
    }

    private void setCartKeyExpire(String userCartKey) {
        redisTemplate.expire(userCartKey,RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    private List<CartInfo> getCartInfoListByUserId(String oneOfUserId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (StringUtils.isEmpty(oneOfUserId)){
            return  cartInfoList;
        }
         cartInfoList = queryFromDbToRedis(oneOfUserId);
        return cartInfoList;
    }

    private String getUserCartKey(String userId) {
        String userCartKey = RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
        return  userCartKey;
    }
}
