package com.atguigu.client;

import com.atguigu.result.RetVal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "shop-order")
public interface OrderFeignClient {

    @GetMapping("/order/confirm")
    public RetVal confirm();
}
