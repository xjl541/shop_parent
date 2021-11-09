package com.atguigu.controller;

import com.atguigu.result.RetVal;
import com.atguigu.search.Product;
import com.atguigu.search.SearchParam;
import com.atguigu.search.SearchResponseVo;
import com.atguigu.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
public class SearchController {
    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Autowired
    private SearchService searchService;

    @GetMapping("/createIndex")
    public RetVal createIndex(){
        elasticsearchTemplate.createIndex(Product.class);
        elasticsearchTemplate.putMapping(Product.class);
        return RetVal.ok();
    }

    @GetMapping("/onSale/{skuId}")
    public RetVal onSale(@PathVariable Long skuId){
        searchService.onSale(skuId);
        return RetVal.ok();
    }

    @GetMapping("/offSale/{skuId}")
    public RetVal offSale(@PathVariable Long skuId){
        searchService.offSale(skuId);
        return RetVal.ok();
    }

    @GetMapping("/incrHotScore/{skuId}")
    public void incrHotScore(@PathVariable Long skuId){
        searchService.incrHotScore(skuId);
    }

    @PostMapping("/searchProduct")
    public RetVal searchProduct(@RequestBody SearchParam searchParam){
        SearchResponseVo searchResponseVo = searchService.searchProduct(searchParam);
        return RetVal.ok(searchResponseVo);
    }
}
