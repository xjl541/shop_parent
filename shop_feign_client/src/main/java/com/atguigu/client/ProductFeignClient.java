package com.atguigu.client;

import com.atguigu.entity.*;
import com.atguigu.fallback.ProductFallBack;
import com.atguigu.result.RetVal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(value = "shop-product", fallback = ProductFallBack.class)
public interface ProductFeignClient {

    @GetMapping("/sku/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable Long skuId);

    @GetMapping("/sku/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable Long category3Id);

    @GetMapping("/sku/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable Long skuId);

    @GetMapping("/sku/getSpuSalePropertyAndSelected/{productId}/{skuId}")
    public List<ProductSalePropertyKey> getSpuSalePropertyAndSelected(@PathVariable Long productId,@PathVariable Long skuId);

    @GetMapping("/sku/getSalePropertyAndSkuIdMapping/{productId}")
    public Map getSalePropertyAndSkuIdMapping(@PathVariable Long productId);

    @GetMapping("/product/getIndexCategoryInfo")
    public RetVal getIndexCategoryInfo();

    @GetMapping("/product/brand/getBrandById/{brandId}")
    public BaseBrand getBrandById(@PathVariable Long brandId);

    @GetMapping("/sku/getPlatformPropertyBySkuId/{skuId}")
    public List<PlatformPropertyName> getPlatformPropertyBySkuId(@PathVariable Long skuId);

    @GetMapping("/product/brand/{brandId}")
    public RetVal getBrand(@PathVariable Long brandId);

}
