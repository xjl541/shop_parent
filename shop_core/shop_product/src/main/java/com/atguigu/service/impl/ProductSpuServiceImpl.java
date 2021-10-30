package com.atguigu.service.impl;

import com.atguigu.entity.ProductImage;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.ProductSalePropertyValue;
import com.atguigu.entity.ProductSpu;
import com.atguigu.mapper.ProductSpuMapper;
import com.atguigu.service.ProductImageService;
import com.atguigu.service.ProductSalePropertyKeyService;
import com.atguigu.service.ProductSalePropertyValueService;
import com.atguigu.service.ProductSpuService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <p>
 * 商品表 服务实现类
 * </p>
 *
 * @author xiejl
 * @since 2021-10-29
 */
@Service
public class ProductSpuServiceImpl extends ServiceImpl<ProductSpuMapper, ProductSpu> implements ProductSpuService {

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private ProductSalePropertyKeyService salePropertyKeyService;

    @Autowired
    private ProductSalePropertyValueService salePropertyValueService;

    @Transactional
    @Override
    public void saveProductSpu(ProductSpu productSpu) {
        // 先保存SPU信息
        baseMapper.insert(productSpu);
        // 保存SPU图片
        Long spuId = productSpu.getId();
        List<ProductImage> productImageList = productSpu.getProductImageList();
        if (!CollectionUtils.isEmpty(productImageList)){
            for (ProductImage productImage : productImageList) {
                productImage.setProductId(productSpu.getId());
            }
            productImageService.saveBatch(productImageList);
        }
        // 保存SPU销售属性
        List<ProductSalePropertyKey> salePropertyKeyList = productSpu.getSalePropertyKeyList();
        if (!CollectionUtils.isEmpty(salePropertyKeyList)){
            for (ProductSalePropertyKey productSalePropertyKey : salePropertyKeyList) {
                productSalePropertyKey.setProductId(productSpu.getId());
                // 保存销售属性值
                List<ProductSalePropertyValue> salePropertyValueList = productSalePropertyKey.getSalePropertyValueList();
                if (!CollectionUtils.isEmpty(salePropertyValueList)){
                    for (ProductSalePropertyValue productSalePropertyValue : salePropertyValueList) {
                        productSalePropertyValue.setProductId(productSpu.getId());
                        productSalePropertyValue.setSalePropertyKeyName(productSalePropertyKey.getSalePropertyKeyName());
                    }
                    salePropertyValueService.saveBatch(salePropertyValueList);
                }
            }
            salePropertyKeyService.saveBatch(salePropertyKeyList);
        }
    }
}
