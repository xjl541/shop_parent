package com.atguigu.service.impl;

import com.atguigu.entity.PlatformPropertyName;
import com.atguigu.entity.PlatformPropertyValue;
import com.atguigu.mapper.PlatformPropertyNameMapper;
import com.atguigu.service.PlatformPropertyNameService;
import com.atguigu.service.PlatformPropertyValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 属性表 服务实现类
 * </p>
 *
 * @author xiejl
 * @since 2021-10-27
 */
@Service
public class PlatformPropertyNameServiceImpl extends ServiceImpl<PlatformPropertyNameMapper, PlatformPropertyName> implements PlatformPropertyNameService {
    @Autowired
    private PlatformPropertyValueService platformPropertyValueService;

    @Override
    public List<PlatformPropertyName> getPlatformPropertyByCategoryId(Long category1Id, Long category2Id, Long category3Id) {
        List<PlatformPropertyName> platformPropertyList = baseMapper.getPlatformPropertyByCategoryId(category1Id,category2Id,category3Id);
        
        return platformPropertyList;
    }

    @Transactional
    @Override
    public void savePlatformProperty(PlatformPropertyName platformPropertyName) {
        if (platformPropertyName.getId() != null){
            baseMapper.updateById(platformPropertyName);

            QueryWrapper<PlatformPropertyValue> wrapper = new QueryWrapper<>();
            wrapper.eq("property_key_id",platformPropertyName.getId());

            platformPropertyValueService.remove(wrapper);
        }else{
            baseMapper.insert(platformPropertyName);
        }
        List<PlatformPropertyValue> propertyValueList = platformPropertyName.getPropertyValueList();

        for (PlatformPropertyValue platformPropertyValue : propertyValueList) {
            platformPropertyValue.setPropertyKeyId(platformPropertyName.getId());
        }
        platformPropertyValueService.saveBatch(propertyValueList);
    }

    @Override
    public List<PlatformPropertyName> getPlatformPropertyBySkuId(Long skuId) {
        List<PlatformPropertyName> platformPropertyNameList = baseMapper.getPlatformPropertyBySkuId(skuId);
        return platformPropertyNameList;
    }
}
