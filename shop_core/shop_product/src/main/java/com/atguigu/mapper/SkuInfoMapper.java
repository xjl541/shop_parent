package com.atguigu.mapper;

import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 库存单元表 Mapper 接口
 * </p>
 *
 * @author xiejl
 * @since 2021-10-30
 */
public interface SkuInfoMapper extends BaseMapper<SkuInfo> {

    List<ProductSalePropertyKey> getSpuSalePropertyAndSelected(Long productId, Long skuId);
}
