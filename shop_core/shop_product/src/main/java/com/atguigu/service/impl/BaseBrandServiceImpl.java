package com.atguigu.service.impl;

import com.atguigu.entity.BaseBrand;
import com.atguigu.mapper.BaseBrandMapper;
import com.atguigu.service.BaseBrandService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 品牌表 服务实现类
 * </p>
 *
 * @author xiejl
 * @since 2021-10-29
 */
@Service
public class BaseBrandServiceImpl extends ServiceImpl<BaseBrandMapper, BaseBrand> implements BaseBrandService {

    @Override
    public List<BaseBrand> getAllBrand() {
        List<BaseBrand> brandList = baseMapper.selectList(null);
        return brandList;
    }
}
