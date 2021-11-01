package com.atguigu.service.impl;

import com.atguigu.entity.SkuImage;
import com.atguigu.entity.SkuInfo;
import com.atguigu.entity.SkuPlatformPropertyValue;
import com.atguigu.entity.SkuSalePropertyValue;
import com.atguigu.mapper.SkuInfoMapper;
import com.atguigu.service.SkuImageService;
import com.atguigu.service.SkuInfoService;
import com.atguigu.service.SkuPlatformPropertyValueService;
import com.atguigu.service.SkuSalePropertyValueService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * <p>
 * 库存单元表 服务实现类
 * </p>
 *
 * @author xiejl
 * @since 2021-10-30
 */
@Service
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoMapper, SkuInfo> implements SkuInfoService {

    @Autowired
    private SkuPlatformPropertyValueService platformPropertyValueService;

    @Autowired
    private SkuSalePropertyValueService skuSalePropertyValueService;

    @Autowired
    private SkuImageService skuImageService;

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        // 保存SKU信息
        baseMapper.insert(skuInfo);
        // 保存SKU的平台属性
        List<SkuPlatformPropertyValue> skuPlatformPropertyValueList = skuInfo.getSkuPlatformPropertyValueList();
        if (!CollectionUtils.isEmpty(skuPlatformPropertyValueList)){
            for (SkuPlatformPropertyValue skuPlatformPropertyValue : skuPlatformPropertyValueList) {
                skuPlatformPropertyValue.setSkuId(skuInfo.getId());
            }
            platformPropertyValueService.saveBatch(skuPlatformPropertyValueList);
        }
        // 保存SKU的销售属性
        List<SkuSalePropertyValue> skuSalePropertyValueList = skuInfo.getSkuSalePropertyValueList();
        if (!CollectionUtils.isEmpty(skuSalePropertyValueList)){
            for (SkuSalePropertyValue skuSalePropertyValue : skuSalePropertyValueList) {
                skuSalePropertyValue.setSkuId(skuInfo.getId());
                skuSalePropertyValue.setProductId(skuInfo.getProductId());
            }
            skuSalePropertyValueService.saveBatch(skuSalePropertyValueList);
        }
        // 保存SKU对应的图片
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)){
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
            }
            skuImageService.saveBatch(skuImageList);
        }
    }


}
