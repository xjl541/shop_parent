package com.atguigu.controller;


import com.atguigu.entity.PlatformPropertyName;
import com.atguigu.entity.PlatformPropertyValue;
import com.atguigu.result.RetVal;
import com.atguigu.service.PlatformPropertyNameService;
import com.atguigu.service.PlatformPropertyValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 属性表 前端控制器
 * </p>
 *
 * @author xiejl
 * @since 2021-10-27
 */
@RestController
@RequestMapping("/product")
//@CrossOrigin
public class PlatformPropertyNameController {

    @Autowired
    private PlatformPropertyNameService platformPropertyNameService;

    @Autowired
    private PlatformPropertyValueService platformPropertyValueService;

    @GetMapping("getPlatformPropertyByCategoryId/{category1Id}/{category2Id}/{category3Id}")
    public RetVal getPlatformPropertyByCategoryId(@PathVariable Long category1Id,
                                                  @PathVariable Long category2Id,
                                                  @PathVariable Long category3Id){
        List<PlatformPropertyName> platformPropertyList =  platformPropertyNameService.getPlatformPropertyByCategoryId(category1Id,category2Id,category3Id);
        return RetVal.ok(platformPropertyList);
    }

    // 保存平台属性   http://127.0.0.1/product/savePlatformProperty
    @PostMapping("savePlatformProperty")
    public RetVal savePlatformProperty(@RequestBody PlatformPropertyName platformPropertyName){
        try {
            platformPropertyNameService.savePlatformProperty(platformPropertyName);
            return RetVal.ok();
        } catch (Exception e) {
            e.printStackTrace();
            return RetVal.fail();
        }
    }

    // 查询平台属性名称对应的属性值   http://127.0.0.1/product/getPropertyValueByPropertyKeyId/6
    @GetMapping("getPropertyValueByPropertyKeyId/{propertyKeyId}")
    public RetVal getPropertyValueByPropertyKeyId(@PathVariable Long propertyKeyId){
        List<PlatformPropertyValue> platformPropertyValues = platformPropertyValueService.getPropertyValueByPropertyKeyId(propertyKeyId);
        return RetVal.ok(platformPropertyValues);
    }
}

