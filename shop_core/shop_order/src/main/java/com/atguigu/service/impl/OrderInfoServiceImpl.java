package com.atguigu.service.impl;

import com.atguigu.client.ProductFeignClient;
import com.atguigu.entity.OrderDetail;
import com.atguigu.entity.OrderInfo;
import com.atguigu.enums.OrderStatus;
import com.atguigu.enums.ProcessStatus;
import com.atguigu.mapper.OrderInfoMapper;
import com.atguigu.service.OrderDetailService;
import com.atguigu.service.OrderInfoService;
import com.atguigu.util.HttpClientUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 * 订单表 订单表 服务实现类
 * </p>
 *
 * @author xiejl
 * @since 2021-11-12
 */
@Service
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderInfoService {

    @Autowired
    private OrderDetailService orderDetailService;


    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ProductFeignClient productFeignClient;

    @Override
    public String generateTradeNo(String userId) {
        String tradeNo = UUID.randomUUID().toString();
        String tradeNoKey = "user:" + userId + ":tradeNo";
        redisTemplate.opsForValue().set(tradeNoKey, tradeNo);
        return tradeNo;
    }

    @Override
    public void submitOrder(OrderInfo orderInfo) {

    }

    @Override
    public List<String> checkStockAndPrice(OrderInfo orderInfo) {
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        List<String> warningStringList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(orderDetailList)) {
            for (OrderDetail orderDetail : orderDetailList) {
                // 判断库存是否足够
                Long skuId = orderDetail.getSkuId();
                String skuNum = orderDetail.getSkuNum();
                // http://localhost:8100/hasStock?skuId=10221&num=2
                String stock = HttpClientUtil.doGet("http://localhost:8100/hasStock?skuId=" + skuId + "&num=" + skuNum);
                if ("0".equals(stock)) {
                    warningStringList.add(orderDetail.getSkuName() + "库存不足");
                }
                // 判断价格是否有变化
                BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
                BigDecimal orderPrice = orderDetail.getOrderPrice();
                if (orderPrice.compareTo(skuPrice) != 0) {
                    warningStringList.add(orderDetail.getSkuName() + "价格已发生变化");
                }
            }
        }
        return warningStringList;
    }

    @Override
    public Long saveOrderAndDetail(OrderInfo orderInfo) {
//a.保存订单基本信息
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //商品对外订单号 out_trade_no 给支付宝或者微信
        String outTradeNo = "atguigu" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        //订单主体信息
        orderInfo.setTradeBody("购买的商品");
        orderInfo.setCreateTime(new Date());
        //订单支付过期时间 默认30分钟过期 设置一天
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());
        //订单进程状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        baseMapper.insert(orderInfo);
        //b.保存订单详情信息
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
        }
        orderDetailService.saveBatch(orderDetailList);
        return orderInfo.getId();
    }
}
