package com.atguigu.service;

import com.atguigu.entity.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 订单表 订单表 服务类
 * </p>
 *
 * @author xiejl
 * @since 2021-11-12
 */
public interface OrderInfoService extends IService<OrderInfo> {

    String generateTradeNo(String userId);

    void submitOrder(OrderInfo orderInfo);

    List<String> checkStockAndPrice(OrderInfo orderInfo);

    Long saveOrderAndDetail(OrderInfo orderInfo);
}
