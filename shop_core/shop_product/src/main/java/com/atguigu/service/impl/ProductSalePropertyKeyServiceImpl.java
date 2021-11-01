package com.atguigu.service.impl;

import com.atguigu.entity.ProductImage;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.mapper.ProductSalePropertyKeyMapper;
import com.atguigu.service.ProductImageService;
import com.atguigu.service.ProductSalePropertyKeyService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * spu销售属性 服务实现类
 * </p>
 *
 * @author xiejl
 * @since 2021-10-29
 */
@Service
public class ProductSalePropertyKeyServiceImpl extends ServiceImpl<ProductSalePropertyKeyMapper, ProductSalePropertyKey> implements ProductSalePropertyKeyService {

    @Autowired
    private ProductImageService productImageService;

    @Override
    public List<ProductSalePropertyKey> querySalePropertyByProductId(Long productId) {
        List<ProductSalePropertyKey> salePropertyKeyList = baseMapper.querySalePropertyByProductId(productId);
        return salePropertyKeyList;
    }

    @Override
    public List<ProductImage> queryProductImageByProductId(Long productId) {
        QueryWrapper<ProductImage> wrapper = new QueryWrapper<>();
        wrapper.eq("product_id",productId);
        return productImageService.list(wrapper);
    }
}
