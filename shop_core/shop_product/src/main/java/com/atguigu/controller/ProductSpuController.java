package com.atguigu.controller;


import com.atguigu.entity.BaseSaleProperty;
import com.atguigu.entity.ProductSpu;
import com.atguigu.result.RetVal;
import com.atguigu.service.BaseSalePropertyService;
import com.atguigu.service.ProductSpuService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 商品表 前端控制器
 * </p>
 *
 * @author xiejl
 * @since 2021-10-29
 */
@RestController
@RequestMapping("/product")
public class ProductSpuController {

    @Autowired
    private ProductSpuService spuService;

    @Autowired
    private BaseSalePropertyService baseSalePropertyService;

    // 根据Id获取SPU列表   http://127.0.0.1/product/queryProductSpuByPage/1/10/61
    @GetMapping("queryProductSpuByPage/{pageNum}/{pageSize}/{category3Id}")
    public RetVal queryProductSpuByPage(
            @PathVariable Long pageNum,
            @PathVariable Long pageSize,
            @PathVariable Long category3Id
    ){
        Page<ProductSpu> spuPage = new Page<>(pageNum,pageSize);
        QueryWrapper<ProductSpu> wrapper = new QueryWrapper<>();
        wrapper.eq("category3_id",category3Id);
        spuService.page(spuPage,wrapper);
        return RetVal.ok(spuPage);
    }

    // 查询所有的模子销售属性的key  http://127.0.0.1/product/queryAllSaleProperty
    @GetMapping("queryAllSaleProperty")
    public RetVal queryAllSaleProperty(){
        List<BaseSaleProperty> salePropertyList = baseSalePropertyService.list(null);
        return RetVal.ok(salePropertyList);
    }
}

