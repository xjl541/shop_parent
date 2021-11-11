package com.atguigu.service.impl;

import com.atguigu.entity.CartInfo;
import com.atguigu.mapper.CartInfoMapper;
import com.atguigu.service.AsyncCartInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Async
@Service
public class AsyncCartInfoServiceImpl extends ServiceImpl<CartInfoMapper, CartInfo> implements AsyncCartInfoService {

    @Override
    public void updateCartInfo(CartInfo existCartInfo) {
        baseMapper.updateById(existCartInfo);
    }

    @Override
    public void insertCartInfo(CartInfo existCartInfo) {
        baseMapper.insert(existCartInfo);
    }

    @Override
    public void deleteCartInfoByUserId(String userTempId) {
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userTempId);
        baseMapper.delete(wrapper);
    }

    @Override
    public List<CartInfo> selectCartInfoList(String userId) {
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        List<CartInfo> dbCartInfoList = baseMapper.selectList(wrapper);
        return dbCartInfoList;
    }

    @Override
    public void deleteCartInfoBySkuId(Long skuId, String userId) {
        QueryWrapper<CartInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        wrapper.eq("sku_id",skuId);
        baseMapper.delete(wrapper);
    }


}
