package com.atguigu.service;

import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 库存单元表 服务类
 * </p>
 *
 * @author xiejl
 * @since 2021-10-30
 */
public interface SkuDetailService {

    SkuInfo getById(Long skuId);

    BigDecimal getSkuPrice(Long skuId);

    List<ProductSalePropertyKey> getSpuSalePropertyAndSelected(Long productId, Long skuId);

    Map getSalePropertyAndSkuIdMapping(Long skuId);

}
