package com.atguigu.service;

import com.atguigu.entity.CartInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 购物车表 用户登录系统时更新冗余 服务类
 * </p>
 *
 * @author xiejl
 * @since 2021-11-09
 */
public interface CartInfoService extends IService<CartInfo> {

    void addCart(Long skuId, Integer skuNum, String userId);

    List<CartInfo> getCartList(String userId, String userTempId);

    void checkCart(Long skuId, Integer isCheckd, String userId);

    void deleteCart(Long skuId, String userId);

    List<CartInfo> getSelectedProduct(Long userId);

    List<CartInfo> queryFromDbToRedis(String userId);
}
