package com.atguigu.service.impl;

import com.atguigu.entity.PlatformPropertyValue;
import com.atguigu.mapper.PlatformPropertyValueMapper;
import com.atguigu.service.PlatformPropertyValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 属性值表 服务实现类
 * </p>
 *
 * @author xiejl
 * @since 2021-10-27
 */
@Service
public class PlatformPropertyValueServiceImpl extends ServiceImpl<PlatformPropertyValueMapper, PlatformPropertyValue> implements PlatformPropertyValueService {

    @Override
    public List<PlatformPropertyValue> getPropertyValueByPropertyKeyId(Long propertyKeyId) {
        QueryWrapper<PlatformPropertyValue> wrapper = new QueryWrapper<>();
        wrapper.eq("property_key_id",propertyKeyId);
        List<PlatformPropertyValue> platformPropertyValues = baseMapper.selectList(wrapper);
        return platformPropertyValues;
    }
}
