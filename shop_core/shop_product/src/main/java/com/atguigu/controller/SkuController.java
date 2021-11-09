package com.atguigu.controller;

import com.atguigu.client.SearchFeignClient;
import com.atguigu.entity.ProductImage;
import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.entity.SkuInfo;
import com.atguigu.result.RetVal;
import com.atguigu.service.ProductSalePropertyKeyService;
import com.atguigu.service.SkuInfoService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/product")
public class SkuController {

    @Autowired
    private ProductSalePropertyKeyService salePropertyKeyService;

    @Autowired
    private SkuInfoService skuInfoService;

    @Autowired
    private SearchFeignClient searchFeignClient;

    // 根据SPU.Id查销售属性   http://127.0.0.1/product/querySalePropertyByProductId/16
    @GetMapping("querySalePropertyByProductId/{productId}")
    public RetVal querySalePropertyByProductId(@PathVariable Long productId){
        List<ProductSalePropertyKey> salePropertyKeyList =  salePropertyKeyService.querySalePropertyByProductId(productId);
        return RetVal.ok(salePropertyKeyList);
    }

    // 根据SPU.ID查SPU图片列表   http://127.0.0.1/product/queryProductImageByProductId/16
    @GetMapping("queryProductImageByProductId/{productId}")
    public RetVal queryProductImageByProductId(@PathVariable Long productId){
        List<ProductImage> productImageList =  salePropertyKeyService.queryProductImageByProductId(productId);
        return RetVal.ok(productImageList);
    }

    // 保存SKU http://127.0.0.1/product/saveSkuInfo
    @PostMapping("saveSkuInfo")
    public RetVal saveSkuInfo(@RequestBody SkuInfo skuInfo){
        skuInfoService.saveSkuInfo(skuInfo);
        return RetVal.ok();
    }

    // 获取所有SKU的分页信息 http://127.0.0.1/product/querySkuInfoByPage/1/10
    @GetMapping("querySkuInfoByPage/{currentPageNum}/{pageSize}")
    public RetVal querySkuInfoByPage(@PathVariable Long currentPageNum,@PathVariable Long pageSize){
        Page<SkuInfo> skuInfoPage = new Page<>(currentPageNum,pageSize);
        skuInfoService.page(skuInfoPage,null);
        return RetVal.ok(skuInfoPage);
    }

    // 下架SKU商品 http://127.0.0.1/product/offSale/24
    @GetMapping("offSale/{skuId}")
    public RetVal offSale(@PathVariable Long skuId){
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);
        skuInfoService.updateById(skuInfo);
        searchFeignClient.offSale(skuId);
        return RetVal.ok();
    }

    @GetMapping("onSale/{skuId}")
    public RetVal onSale(@PathVariable Long skuId){
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);
        skuInfoService.updateById(skuInfo);
        searchFeignClient.onSale(skuId);
        return RetVal.ok();
    }
}
