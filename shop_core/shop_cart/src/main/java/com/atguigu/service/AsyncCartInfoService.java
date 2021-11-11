package com.atguigu.service;

import com.atguigu.entity.CartInfo;

import java.util.List;

public interface AsyncCartInfoService {
    void updateCartInfo(CartInfo existCartInfo);

    void insertCartInfo(CartInfo existCartInfo);

    void deleteCartInfoByUserId(String userTempId);

    List<CartInfo> selectCartInfoList(String userId);

    void deleteCartInfoBySkuId(Long skuId, String userId);
}
