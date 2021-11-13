package com.atguigu.client;

import com.atguigu.entity.CartInfo;
import com.atguigu.result.RetVal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(value = "shop-cart")
public interface CartFeignClient {
    @PostMapping("/cart/addCart/{skuId}/{skuNum}")
    public RetVal addCart(@PathVariable Long skuId, @PathVariable Integer skuNum);

    @GetMapping("/cart/getSelectedProduct/{userId}")
    public List<CartInfo> getSelectedProduct(@PathVariable Long userId);
}
