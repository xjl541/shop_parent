package com.atguigu.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.client.ProductFeignClient;
import com.atguigu.dao.ProductRepository;
import com.atguigu.entity.*;
import com.atguigu.search.*;
import com.atguigu.service.SearchService;
import lombok.SneakyThrows;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void onSale(Long skuId) {
        Product product = new Product();
        //a.商品的基本信息
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if (skuInfo != null){
            product.setId(skuInfo.getId());
            product.setProductName(skuInfo.getSkuName());
            product.setCreateTime(new Date());
            product.setPrice(skuInfo.getPrice().doubleValue());
            product.setDefaultImage(skuInfo.getSkuDefaultImg());

            // b.品牌的信息
            BaseBrand brand = productFeignClient.getBrandById(skuInfo.getBrandId());
            if (brand != null){
                product.setBrandId(brand.getId());
                product.setBrandName(brand.getBrandName());
                product.setBrandLogoUrl(brand.getBrandLogoUrl());
            }
            //c.商品的分类信息
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            if(categoryView!=null){
                product.setCategory1Id(categoryView.getCategory1Id());
                product.setCategory1Name(categoryView.getCategory1Name());
                product.setCategory2Id(categoryView.getCategory2Id());
                product.setCategory2Name(categoryView.getCategory2Name());
                product.setCategory3Id(categoryView.getCategory3Id());
                product.setCategory3Name(categoryView.getCategory3Name());
            }
            //d.平台的属性信息
            List<PlatformPropertyName> platformPropertyNameList = productFeignClient.getPlatformPropertyBySkuId(skuId);
            if (!CollectionUtils.isEmpty(platformPropertyNameList)){
                List<SearchPlatformProperty> searchPlatformPropertyList = platformPropertyNameList.stream().map(platformPropertyName -> {
                    SearchPlatformProperty searchPlatformProperty = new SearchPlatformProperty();
                    searchPlatformProperty.setPropertyKeyId(platformPropertyName.getId());
                    searchPlatformProperty.setPropertyKey(platformPropertyName.getPropertyKey());
                    PlatformPropertyValue platformPropertyValue = platformPropertyName.getPropertyValueList().get(0);
                    searchPlatformProperty.setPropertyValue(platformPropertyValue.getPropertyValue());
                    return searchPlatformProperty;
                }).collect(Collectors.toList());
                product.setPlatformProperty(searchPlatformPropertyList);
            }
            //e.把上面的数据放入es中
            productRepository.save(product);
        }
    }

    @Override
    public void offSale(Long skuId) {
        productRepository.deleteById(skuId);
    }

    @Override
    public void incrHotScore(Long skuId) {
        String hotKey="sku:hotscore";
        Double count = redisTemplate.opsForZSet().incrementScore(hotKey, skuId, 1);
        if (count%6 ==0){
            Optional<Product> optional = productRepository.findById(skuId);
            Product esProduct = optional.get();
            esProduct.setHotScore(Math.round(count));
            productRepository.save(esProduct);
        }
    }

    @SneakyThrows
    @Override
    public SearchResponseVo searchProduct(SearchParam searchParam) {
        //1.生成DSL查询语句
        SearchRequest searchRequest = this.buildQueryDsl(searchParam);
        //2.通过该语句实现查询
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //3.将查询结果封装到某个对象当中
        SearchResponseVo searchResponseVo = this.parseSearchResult(searchResponse);
        //4.其他返回参数的赋值
        searchResponseVo.setPageNo(searchParam.getPageNo());
        searchResponseVo.setPageSize(searchParam.getPageSize());
        //5.设置总页数
        boolean addPageFlag = searchResponseVo.getTotal() % searchParam.getPageSize() == 0;
        long totalPage = 0;
        if (addPageFlag){
            totalPage = searchResponseVo.getTotal() / searchParam.getPageSize();
        }else{
            totalPage = searchResponseVo.getTotal() / searchParam.getPageSize()+1 ;
        }
        searchResponseVo.setTotalPages(totalPage);
        return searchResponseVo;
    }

    private SearchResponseVo parseSearchResult(SearchResponse searchResponse) {
        SearchResponseVo searchResponseVo = new SearchResponseVo();
        SearchHits firstHits = searchResponse.getHits();
        // 设置总个数
        searchResponseVo.setTotal(firstHits.getTotalHits());
        SearchHit[] secondHits = firstHits.getHits();
        ArrayList<Product> products = new ArrayList<>();
        // 存储基本信息对象
        if (secondHits !=null && secondHits.length > 0){
            for (SearchHit secondHit : secondHits) {
                Product product = JSONObject.parseObject(secondHit.getSourceAsString(), Product.class);
                // 设置高亮
                HighlightField highlightField = secondHit.getHighlightFields().get("productName");
                if (highlightField != null){
                    Text highlightFieldProductName = highlightField.getFragments()[0];
                    product.setProductName(highlightFieldProductName.toString());
                }
                products.add(product);
            }
            searchResponseVo.setProductList(products);
        }

        // 拿到品牌信息
        ParsedLongTerms brandIdAgg = searchResponse.getAggregations().get("brandIdAgg");
        List<SearchBrandVo> searchBrandVoList = brandIdAgg.getBuckets().stream().map(bucket -> {
            SearchBrandVo searchBrandVo = new SearchBrandVo();
            String brandId = bucket.getKeyAsString();
            searchBrandVo.setBrandId(Long.parseLong(brandId));

            // 品牌名称
            ParsedStringTerms brandNameAgg = bucket.getAggregations().get("brandNameAgg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            searchBrandVo.setBrandName(brandName);

            // 拿到品牌LOGO
            ParsedStringTerms brandLogoUrlAgg = bucket.getAggregations().get("brandLogoUrlAgg");
            String brandLogoUrl = brandLogoUrlAgg.getBuckets().get(0).getKeyAsString();
            searchBrandVo.setBrandLogoUrl(brandLogoUrl);

            return searchBrandVo;
        }).collect(Collectors.toList());
        searchResponseVo.setBrandVoList(searchBrandVoList);

        //拿到平台属性信息
        ParsedNested platformPropertyAgg = searchResponse.getAggregations().get("platformPropertyAgg");
        ParsedLongTerms propertyKeyIdAgg = platformPropertyAgg.getAggregations().get("propertyKeyIdAgg");
        List<? extends Terms.Bucket> buckets = propertyKeyIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)){
            List<SearchPlatformPropertyVo> searchPlatformPropertyVoList = buckets.stream().map(bucket -> {
                SearchPlatformPropertyVo searchPlatformPropertyVo = new SearchPlatformPropertyVo();
                searchPlatformPropertyVo.setPropertyKeyId(bucket.getKeyAsNumber().longValue());

                ParsedStringTerms propertyKeyAgg = bucket.getAggregations().get("propertyKeyAgg");
                String propertyKey = propertyKeyAgg.getBuckets().get(0).getKeyAsString();
                searchPlatformPropertyVo.setPropertyKey(propertyKey);

                ParsedStringTerms propertyValueAgg = bucket.getAggregations().get("propertyValueAgg");
                List<? extends Terms.Bucket> propertyValueBuckets = propertyValueAgg.getBuckets();
                List<String> propertyValueList = propertyValueBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                searchPlatformPropertyVo.setPropertyValueList(propertyValueList);

                return searchPlatformPropertyVo;
            }).collect(Collectors.toList());
            searchResponseVo.setPlatformPropertyList(searchPlatformPropertyVoList);
        }

        return searchResponseVo;
    }

    private SearchRequest buildQueryDsl(SearchParam searchParam) {
        BoolQueryBuilder firstBoolQuery = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(searchParam.getCategory1Id())){
            TermQueryBuilder category1Id = QueryBuilders.termQuery("category1Id", searchParam.getCategory1Id());
            firstBoolQuery.filter(category1Id);
        }
        if (!StringUtils.isEmpty(searchParam.getCategory2Id())){
            TermQueryBuilder category2Id = QueryBuilders.termQuery("category2Id", searchParam.getCategory2Id());
            firstBoolQuery.filter(category2Id);
        }
        if (!StringUtils.isEmpty(searchParam.getCategory3Id())){
            TermQueryBuilder category3Id = QueryBuilders.termQuery("category3Id", searchParam.getCategory3Id());
            firstBoolQuery.filter(category3Id);
        }
        if (!StringUtils.isEmpty(searchParam.getBrandName())){
            //3.构造品牌过滤器 参数格式brandName=1:苹果
            String brandName = searchParam.getBrandName();
            String[] brand = brandName.split(":");
            if (brand.length ==2){
                TermQueryBuilder brandId = QueryBuilders.termQuery("brandId", brand[1]);
                firstBoolQuery.filter(brandId);
            }
        }
        //4.构造平台属性过滤器 参数格式 props=4:骁龙888:CPU型号&props=5:6.55-6.64英寸:屏幕尺寸
        String[] props = searchParam.getProps();
        if (props != null &&props.length>0){
            for (String prop : props) {
                String[] platformParams = prop.split(":");
                if (platformParams.length == 3){
                    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                    BoolQueryBuilder childBoolQuery = QueryBuilders.boolQuery();
                    childBoolQuery.must(QueryBuilders.termQuery("platformProperty.propertyValue", platformParams[1]));
                    childBoolQuery.must(QueryBuilders.termQuery("platformProperty.propertyKeyId", platformParams[0]));
                    boolQuery.must(QueryBuilders.nestedQuery("platformProperty",childBoolQuery, ScoreMode.None));
                    firstBoolQuery.filter(boolQuery);
                }
            }
        }
        //5.构造商品名称搜索关键词查询 参数格式 keyword=苹果
        String keyword = searchParam.getKeyword();
        if (!StringUtils.isEmpty(keyword)){
            MatchQueryBuilder productName = QueryBuilders.matchQuery("productName", keyword).operator(Operator.AND);
            firstBoolQuery.must(productName);
        }
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(firstBoolQuery);
        Integer pageSize = searchParam.getPageSize();
        int from = (searchParam.getPageNo() - 1) * pageSize;
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(searchParam.getPageSize());

        String order = searchParam.getOrder();
        /**
         * 8.构造排序  参数格式 order=2:desc
         * 1---综合(hotScore) 2----价格(price)
         */
        if (!StringUtils.isEmpty(order)){
            String[] orderParams = order.split(":");
            if (orderParams.length ==2){
                String filedName = "";
                switch (orderParams[0]){
                    case "1":
                        filedName = "hotScore";
                        break;
                    case "2":
                        filedName = "price";
                        break;
                }
                searchSourceBuilder.sort(filedName,"asc".equals(orderParams[1])? SortOrder.ASC:SortOrder.DESC);
            }
        }else{
            searchSourceBuilder.sort("hotScore",SortOrder.DESC);
        }
        // 设置高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("productName");
        highlightBuilder.preTags("<span style=color:red>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        // 设置品牌聚合;
        TermsAggregationBuilder brandIdAgg = AggregationBuilders.terms("brandIdAgg").field("brandId")
                .subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName"))
                .subAggregation(AggregationBuilders.terms("brandLogoUrlAgg").field("brandLogoUrl"));
        searchSourceBuilder.aggregation(brandIdAgg);

        // 设置平台属性聚合
        NestedAggregationBuilder nestedAggregationBuilder = AggregationBuilders.nested("platformPropertyAgg", "platformProperty")
                .subAggregation(AggregationBuilders.terms("propertyKeyIdAgg").field("platformProperty.propertyKeyId")
                        .subAggregation(AggregationBuilders.terms("propertyKeyAgg").field("platformProperty.propertyKey"))
                        .subAggregation(AggregationBuilders.terms("propertyValueAgg").field("platformProperty.propertyValue")));

        searchSourceBuilder.aggregation(nestedAggregationBuilder);

        searchSourceBuilder.fetchSource(new String[]{"id", "defaultImage", "productName", "price","hotScore"},null);
        SearchRequest searchRequest = new SearchRequest("product");
        searchRequest.types("info");
        searchRequest.source(searchSourceBuilder);
//        System.out.println(searchRequest);

        return searchRequest;
    }
}
