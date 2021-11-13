package com.atguigu.controller;

import com.atguigu.client.CartFeignClient;
import com.atguigu.client.UserFeignClient;
import com.atguigu.entity.CartInfo;
import com.atguigu.entity.OrderDetail;
import com.atguigu.entity.OrderInfo;
import com.atguigu.entity.UserAddress;
import com.atguigu.result.RetVal;
import com.atguigu.service.OrderInfoService;
import com.atguigu.util.AuthContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/order")
public class OrderInfoController {

    @Autowired
    private CartFeignClient cartFeignClient;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private RedisTemplate redisTemplate;


    @GetMapping("/confirm")
    public RetVal confirm(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        // 获取地址列表
        List<UserAddress> userAddressList = userFeignClient.getAddressListByUserId(userId);
        // 获取所选购物车列表
        List<CartInfo> cartInfoList =cartFeignClient.getSelectedProduct(Long.parseLong(userId));
        BigDecimal totalMoney = new BigDecimal(0);
        int totalNum = 0;
        // 送货清单改造
        List<OrderDetail> orderDetails = new ArrayList<>();
        if (!CollectionUtils.isEmpty(cartInfoList)){
            for (CartInfo cartInfo : cartInfoList) {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setSkuId(cartInfo.getSkuId());
                orderDetail.setSkuName(cartInfo.getSkuName());
                orderDetail.setImgUrl(cartInfo.getImgUrl());
                orderDetail.setSkuNum(cartInfo.getSkuNum()+"");
                orderDetail.setOrderPrice(cartInfo.getCartPrice());
                // 商品的总金额
                totalMoney = totalMoney.add(cartInfo.getCartPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));
                // 商品的总数量
                totalNum += cartInfo.getSkuNum();
                orderDetails.add(orderDetail);
            }
        }
        Map<String, Object> retMap = new HashMap<>();
        // 地址信息
        retMap.put("userAddressList",userAddressList);
        // 订单总金额
        retMap.put("totalMoney",totalMoney);
        // 订单总数量
        retMap.put("totalNum",totalNum);
        retMap.put("detailArrayList",orderDetails);
        //生成一个流水号
        String tradeNo=orderInfoService.generateTradeNo(userId);
        retMap.put("tradeNo",tradeNo);
        return RetVal.ok(retMap);
    }

    // http://api.gmall.com/order/submitOrder?tradeNo=null
    @PostMapping("submitOrder")
    public RetVal submitOrder(@RequestBody OrderInfo orderInfo,HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        // 先判断redis中是的流水号是否一致
        String tradeNoKey = "user:" +userId +":tradeNo";
        String  redisTradeNo = (String) redisTemplate.opsForValue().get(tradeNoKey);
        String tradeNo = request.getParameter("tradeNo");
        if (!tradeNo.equals(redisTradeNo)){
            return RetVal.fail().message("不能无刷新重复提交");
        }
        redisTemplate.delete(tradeNoKey);
        // 再校验价格和库存,如果有问题需要返回异常信息
        // http://localhost:8100/hasStock
        List<String> list = orderInfoService.checkStockAndPrice(orderInfo);
        if (!CollectionUtils.isEmpty(list)){
            return RetVal.fail().message( StringUtils.join(list,","));
        }
        // 保存订单信息
        orderInfo.setUserId(Long.parseLong(userId));
        Long orderId = orderInfoService.saveOrderAndDetail(orderInfo);
        return RetVal.ok(orderId);
    }
}
