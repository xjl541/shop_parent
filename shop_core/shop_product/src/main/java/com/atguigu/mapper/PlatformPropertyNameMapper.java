package com.atguigu.mapper;

import com.atguigu.entity.PlatformPropertyName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * <p>
 * 属性表 Mapper 接口
 * </p>
 *
 * @author xiejl
 * @since 2021-10-27
 */
public interface PlatformPropertyNameMapper extends BaseMapper<PlatformPropertyName> {

    List<PlatformPropertyName> getPlatformPropertyByCategoryId(Long category1Id, Long category2Id, Long category3Id);

    List<PlatformPropertyName> getPlatformPropertyBySkuId(Long skuId);
}
