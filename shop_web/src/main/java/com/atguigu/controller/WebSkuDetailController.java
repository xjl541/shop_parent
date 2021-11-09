package com.atguigu.controller;

import com.alibaba.fastjson.JSON;
import com.atguigu.client.ProductFeignClient;
import com.atguigu.client.SearchFeignClient;
import com.atguigu.entity.BaseCategoryView;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Controller
public class WebSkuDetailController {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private SearchFeignClient searchFeignClient;

    @RequestMapping("{skuId}.html")
    public String getSkuDetail(@PathVariable Long skuId, Model model){
        Map<String, Object> dataMap = new HashMap<>();
        //a.根据skuId获取商品sku的基本信息 getSkuInfo
        CompletableFuture<SkuInfo> skuInfoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            dataMap.put("skuInfo", skuInfo);
            return skuInfo;
        });

        //c.根据skuId获取sku的实时价格 getSkuPrice
        CompletableFuture<Void> priceFuture = CompletableFuture.runAsync(() -> {
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            dataMap.put("price", skuPrice);
        });

        //b.根据三级分类id获取sku的分类信息 getCategory3Id
        CompletableFuture<Void> categoryViewFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            Long category3Id = skuInfo.getCategory3Id();
            BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(category3Id);
            dataMap.put("categoryView", baseCategoryView);
        });

        //d.根据skuId获取所有的spu销售属性与该sku所勾选的销售属性 getSpuSalePropertyAndSelected
        CompletableFuture<Void> spuSalePropertyListFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            Long productId = skuInfo.getProductId();
            List<ProductSalePropertyKey> spuSalePropertyList = productFeignClient.getSpuSalePropertyAndSelected(productId, skuId);
            dataMap.put("spuSalePropertyList", spuSalePropertyList);
        });


        //e.查询销售属性组合所对于skuId的对于关系 getSalePropertyAndSkuIdMapping
        CompletableFuture<Void> salePropertyValueIdJsonFuture = skuInfoFuture.thenAcceptAsync(skuInfo -> {
            Long productId = skuInfo.getProductId();
            Map salePropertyAndSkuIdMap = productFeignClient.getSalePropertyAndSkuIdMapping(productId);
            dataMap.put("salePropertyValueIdJson", JSON.toJSONString(salePropertyAndSkuIdMap));
        });

        // 每次访问商品将热度加一
        CompletableFuture<Void> hotScoreFuture = CompletableFuture.runAsync(() -> {
            searchFeignClient.incrHotScore(skuId);
        });

        CompletableFuture.allOf(skuInfoFuture,
                priceFuture
                , categoryViewFuture,
                spuSalePropertyListFuture,
                salePropertyValueIdJsonFuture,
                hotScoreFuture).join();

        model.addAllAttributes(dataMap);
        return "detail/index";
    }
}
