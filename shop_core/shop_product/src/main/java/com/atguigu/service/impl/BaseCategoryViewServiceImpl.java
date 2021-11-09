package com.atguigu.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.entity.BaseCategoryView;
import com.atguigu.mapper.BaseCategoryViewMapper;
import com.atguigu.service.BaseCategoryViewService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * VIEW 服务实现类
 * </p>
 *
 * @author xiejl
 * @since 2021-11-01
 */
@Service
public class BaseCategoryViewServiceImpl extends ServiceImpl<BaseCategoryViewMapper, BaseCategoryView> implements BaseCategoryViewService {

    @Override
    public List<JSONObject> getIndexCategoryInfo() {
        List<BaseCategoryView> categoryViewList = baseMapper.selectList(null);
        Map<Long, List<BaseCategoryView>>  category1Map= categoryViewList.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        int index = 1;
        // 新建返回值列表
        ArrayList<JSONObject> jsonObjectList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(category1Map)) {
            for (Map.Entry<Long, List<BaseCategoryView>> entry : category1Map.entrySet()) {
                JSONObject category1 = new JSONObject();
                Long category1key = entry.getKey();
                category1.put("index", index++);
                category1.put("categoryId", category1key);
                category1.put("categoryName", entry.getValue().get(0).getCategory1Name());
                // 获取2级分类
                List<BaseCategoryView> category2List = entry.getValue();
                ArrayList<JSONObject> category1Children = new ArrayList<>();
                Map<Long, List<BaseCategoryView>>  category2Map= category2List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
                if (!CollectionUtils.isEmpty(category2Map)) {
                    for (Map.Entry<Long, List<BaseCategoryView>> category2entry : category2Map.entrySet()) {
                        JSONObject category2 = new JSONObject();
                        Long category2Key = category2entry.getKey();
                        category2.put("categoryId", category2Key);
                        category2.put("categoryName", category2entry.getValue().get(0).getCategory2Name());

                        List<BaseCategoryView> category3List = category2entry.getValue();
                        ArrayList<JSONObject> category2Children = new ArrayList<>();
                        Map<Long, List<BaseCategoryView>>  category3Map= category3List.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));
                        if (!CollectionUtils.isEmpty(category3Map)) {
                            for (Map.Entry<Long, List<BaseCategoryView>> category3entry : category3Map.entrySet()) {
                                JSONObject category3 = new JSONObject();
                                category3.put("categoryId", category3entry.getKey());
                                category3.put("categoryName", category3entry.getValue().get(0).getCategory3Name());
                                category2Children.add(category3);
                            }
                            category2.put("categoryChild",category2Children);
                        }
                        category1Children.add(category2);
                    }
                }
                category1.put("categoryChild", category1Children);
                jsonObjectList.add(category1);
            }
        }
        return jsonObjectList;
    }
}
