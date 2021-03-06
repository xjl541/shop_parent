package com.atguigu.fallback;

import com.atguigu.client.ProductFeignClient;
import com.atguigu.entity.*;
import com.atguigu.result.RetVal;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class ProductFallBack implements ProductFeignClient {


    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        // 编写降级服务
        return null;
    }

    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        return null;
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        return null;
    }

    @Override
    public List<ProductSalePropertyKey> getSpuSalePropertyAndSelected(Long productId, Long skuId) {
        return null;
    }

    @Override
    public Map getSalePropertyAndSkuIdMapping(Long productId) {
        return null;
    }

    @Override
    public RetVal getIndexCategoryInfo() {
        return null;
    }

    @Override
    public BaseBrand getBrandById(Long brandId) {
        return null;
    }


    @Override
    public List<PlatformPropertyName> getPlatformPropertyBySkuId(Long skuId) {
        return null;
    }

    @Override
    public RetVal getBrand(Long brandId) {
        return null;
    }
}
