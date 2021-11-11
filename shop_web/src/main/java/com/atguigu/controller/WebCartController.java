package com.atguigu.controller;

import com.atguigu.client.CartFeignClient;
import com.atguigu.client.ProductFeignClient;
import com.atguigu.entity.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@Controller
public class WebCartController {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    // http://cart.gmall.com/addCart.html?skuId=29
    @RequestMapping("addCart.html")
    public String addCart(@RequestParam Long skuId, @RequestParam Integer skuNum, HttpServletRequest request){
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        cartFeignClient.addCart(skuId,skuNum);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);
        return "cart/addCart";
    }

    // http://cart.gmall.com/cart.html
    @RequestMapping("cart.html")
    public String cart(){
        return "cart/index";
    }
}
