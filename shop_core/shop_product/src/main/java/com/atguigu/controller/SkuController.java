package com.atguigu.controller;

import com.atguigu.entity.ProductSalePropertyKey;
import com.atguigu.result.RetVal;
import com.atguigu.service.ProductSalePropertyKeyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
public class SkuController {

    @Autowired
    private ProductSalePropertyKeyService salePropertyKeyService;

    // 根据SPU.Id查销售属性   http://127.0.0.1/product/querySalePropertyByProductId/16
    @GetMapping("querySalePropertyByProductId/{productId}")
    public RetVal querySalePropertyByProductId(@PathVariable Long productId){
        List<ProductSalePropertyKey> salePropertyKeyList =  salePropertyKeyService.querySalePropertyByProductId(productId);
        return RetVal.ok(salePropertyKeyList);
    }

    // 根据SPU.ID查SPU图片列表   http://127.0.0.1/product/queryProductImageByProductId/16
}
