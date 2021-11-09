package com.atguigu.controller;

import com.atguigu.client.ProductFeignClient;
import com.atguigu.client.SearchFeignClient;
import com.atguigu.result.RetVal;
import com.atguigu.search.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class IndexController {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private SearchFeignClient searchFeignClient;

    @GetMapping({"index.html","/"})
    public String index(Model model){
        RetVal retVal = productFeignClient.getIndexCategoryInfo();
        model.addAttribute("list",retVal.getData());
        return "index/index";
    }

    @GetMapping("search.html")
    public String searchProduct( SearchParam searchParam,Model model){
        RetVal<Map> retVal = searchFeignClient.searchProduct(searchParam);
        model.addAllAttributes(retVal.getData());
        // 搜索路径参数回显
        String urlParam = pageUrlParam(searchParam);
        model.addAttribute("urlParam",urlParam);
        // 页面回显品牌信息
        String brandName = pageBrandName(searchParam.getBrandName());
        model.addAttribute("brandNameParam",brandName);
        // 页面回显平台属性信息
        List<Map<String, String>> maps = pageProps(searchParam.getProps());
        model.addAttribute("propsParamList",maps);
        // 页面回显排序信息
        Map<String, Object> orderMap = pageSortInfo(searchParam.getOrder());
        model.addAttribute("orderMap",orderMap);
        return "search/index";
    }

    private Map<String, Object> pageSortInfo(String order) {
        Map<String, Object> orderMap = new HashMap<>();
        if (!StringUtils.isEmpty(order)){
            String[] orderParams = order.split(":");
            if (orderParams.length ==2){
                orderMap.put("type",orderParams[0]);
                orderMap.put("sort",orderParams[1]);
            }
        }else {
            orderMap.put("type",1);
            orderMap.put("sort","desc");
        }
        return orderMap;
    }

    private List<Map<String, String>> pageProps(String[] props) {
        List<Map<String, String>> propList = new ArrayList<>();
        if (props != null && props.length>0){
            for (String prop : props) {
                String[] propParams = prop.split(":");
                Map<String, String> propMap = new HashMap<>();
                propMap.put("propertyKeyId",propParams[0]);
                propMap.put("propertyValue",propParams[1]);
                propMap.put("propertyKey",propParams[2]);
                propList.add(propMap);
            }
        }
        return propList;
    }

    private String pageBrandName(String brandName) {
        if (!StringUtils.isEmpty(brandName)){
            String[] brandNameParams = brandName.split(":");
            if (brandNameParams.length == 2){
                return "品牌："+brandNameParams[1];
            }
        }
        return "";
    }


    private String pageUrlParam(SearchParam searchParam) {
        StringBuilder urlParam = new StringBuilder();
        if (!StringUtils.isEmpty(searchParam.getKeyword())){
            urlParam.append("keyword=").append(searchParam.getKeyword());
        }
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())){
            if(urlParam.length()>0){
                urlParam.append("&category1Id=").append(searchParam.getCategory1Id());
            }else {
                urlParam.append("category1Id=").append(searchParam.getCategory1Id());
            }

        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())){
            if(urlParam.length()>0){
                urlParam.append("&category2Id=").append(searchParam.getCategory2Id());
            }else{
                urlParam.append("category2Id=").append(searchParam.getCategory2Id());
            }

        }
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())){
            if(urlParam.length()>0){
                urlParam.append("&category3Id=").append(searchParam.getCategory3Id());
            }else{
                urlParam.append("category3Id=").append(searchParam.getCategory3Id());
            }
        }

        if (!StringUtils.isEmpty(searchParam.getBrandName())){
            if (urlParam.length() >0){
                urlParam.append("&brandName=").append(searchParam.getBrandName());
            }
        }
        if (!StringUtils.isEmpty(searchParam.getProps())){
            if (urlParam.length() >0){
                for (String prop : searchParam.getProps()) {
                    urlParam.append("&props=").append(prop);
                }
                urlParam.append("&brandName=").append(searchParam.getBrandName());
            }
        }
        return "search.html?"+urlParam.toString();
    }
}
