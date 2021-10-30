package com.atguigu.controller;


import com.atguigu.entity.BaseBrand;
import com.atguigu.result.RetVal;
import com.atguigu.service.BaseBrandService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.io.FilenameUtils;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 品牌表 前端控制器
 * </p>
 *
 * @author xiejl
 * @since 2021-10-29
 */
@RestController
@RequestMapping("/product/brand")
public class BaseBrandController {
    @Autowired
    private BaseBrandService brandService;

    @Value("${fastdfs.prefix}")
    private String fastdfsPrefix;

    // 分页获取品牌信息 http://127.0.0.1/product/brand/queryBrandByPage/1/10
    @GetMapping("queryBrandByPage/{currentPageNum}/{pageSize}")
    public RetVal queryBrandByPage(@PathVariable Long currentPageNum,
                                   @PathVariable Long pageSize){
        Page<BaseBrand> page = new Page<>(currentPageNum,pageSize);
        QueryWrapper<BaseBrand> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("id");
        IPage<BaseBrand> brandPage = brandService.page(page, wrapper);
        return RetVal.ok(brandPage);
    }

    @PostMapping
    public RetVal save(@RequestBody BaseBrand baseBrand){
        brandService.save(baseBrand);
        return RetVal.ok();
    }

    @PutMapping
    public RetVal updateBrand(@RequestBody BaseBrand baseBrand){
        brandService.updateById(baseBrand);
        return RetVal.ok();
    }

    @DeleteMapping("/{brandId}")
    public RetVal deleteBrand(@PathVariable Long brandId){
        brandService.removeById(brandId);
        return RetVal.ok();
    }

    @GetMapping("/{brandId}")
    public RetVal getBrand(@PathVariable Long brandId){
        BaseBrand brand = brandService.getById(brandId);
        return RetVal.ok(brand);
    }

    // 实现文件上传  http://api.gmall.com/product/brand/fileUpload
    @PostMapping("fileUpload")
    public RetVal  fileUpload(MultipartFile file) throws Exception {
        //需要一个配置文件告诉fastdfs在哪里
        String configFilePath = this.getClass().getResource("/tracker.conf").getFile();
        //初始化
        ClientGlobal.init(configFilePath);
        //创建trackerClient 客户端
        TrackerClient trackerClient = new TrackerClient();
        //用trackerClient获取连接
        TrackerServer trackerServer = trackerClient.getConnection();
        //创建StorageClient1
        StorageClient1 storageClient1 = new StorageClient1(trackerServer, null);
        //文件原始名称 aaa.jpg
        String originalFilename = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalFilename);
        //对文件实现上传
        String path = storageClient1.upload_appender_file1(file.getBytes(), extension, null);

        System.out.println("文件访问路径:"+fastdfsPrefix+path);
        return RetVal.ok(fastdfsPrefix+path);
    }

    // 获取所有品牌列表  http://127.0.0.1/product/brand/getAllBrand
    @GetMapping("getAllBrand")
    public RetVal getAllBrand(){
        List<BaseBrand> brandList =  brandService.getAllBrand();
        return RetVal.ok(brandList);
    }
}

