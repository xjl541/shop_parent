package com.atguigu.controller;

import com.atguigu.entity.BaseCategoryView;
import com.atguigu.entity.PlatformPropertyName;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuInfo;
import com.atguigu.service.BaseCategoryViewService;
import com.atguigu.service.PlatformPropertyNameService;
import com.atguigu.service.SkuDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sku")
public class SkuDetailController {

    @Autowired
    private SkuDetailService skuDetailService;

    @Autowired
    private BaseCategoryViewService baseCategoryViewService;

    @Autowired
    private PlatformPropertyNameService propertyNameService;

    @GetMapping("getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId){
        SkuInfo skuInfo = skuDetailService.getById(skuId);
        return skuInfo;
    }

    @GetMapping("getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id){
        BaseCategoryView baseCategoryView = baseCategoryViewService.getById(category3Id);
        return baseCategoryView;
    }

    @GetMapping("getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId){
        BigDecimal skuPrice = skuDetailService.getSkuPrice(skuId);
        return skuPrice;
    }

    @GetMapping("/getSpuSalePropertyAndSelected/{productId}/{skuId}")
    public List<ProductSalePropertyKey> getSpuSalePropertyAndSelected(@PathVariable Long productId,@PathVariable Long skuId){
        List<ProductSalePropertyKey> salePropertyKeyList =  skuDetailService.getSpuSalePropertyAndSelected(productId,skuId);
        return salePropertyKeyList;
    }

    @GetMapping("/getSalePropertyAndSkuIdMapping/{productId}")
    public Map getSalePropertyAndSkuIdMapping(@PathVariable Long productId){
        return skuDetailService.getSalePropertyAndSkuIdMapping(productId);
    }

    @GetMapping("/getPlatformPropertyBySkuId/{skuId}")
    public List<PlatformPropertyName> getPlatformPropertyBySkuId(@PathVariable Long skuId){
        List<PlatformPropertyName> platformPropertyBySkuId = propertyNameService.getPlatformPropertyBySkuId(skuId);
        return platformPropertyBySkuId;
    }

}
