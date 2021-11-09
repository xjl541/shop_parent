package com.atguigu.service;

import com.atguigu.entity.PlatformPropertyName;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 属性表 服务类
 * </p>
 *
 * @author xiejl
 * @since 2021-10-27
 */
public interface PlatformPropertyNameService extends IService<PlatformPropertyName> {

    List<PlatformPropertyName> getPlatformPropertyByCategoryId(Long category1Id, Long category2Id, Long category3Id);

    void savePlatformProperty(PlatformPropertyName platformPropertyName);

    List<PlatformPropertyName> getPlatformPropertyBySkuId(Long skuId);
}
