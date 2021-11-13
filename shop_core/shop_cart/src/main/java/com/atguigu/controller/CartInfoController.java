package com.atguigu.controller;


import com.atguigu.entity.CartInfo;
import com.atguigu.result.RetVal;
import com.atguigu.service.CartInfoService;
import com.atguigu.util.AuthContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * <p>
 * 购物车表 用户登录系统时更新冗余 前端控制器
 * </p>
 *
 * @author xiejl
 * @since 2021-11-09
 */
@RestController
@RequestMapping("/cart")
public class CartInfoController {

    @Autowired
    private CartInfoService cartInfoService;

    @PostMapping("/addCart/{skuId}/{skuNum}")
    public RetVal addCart(@PathVariable Long skuId, @PathVariable  Integer skuNum, HttpServletRequest request){
        System.out.println("================="+request.getHeader("userTempId"));


        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartInfoService.addCart(skuId,skuNum,userId);
        return RetVal.ok();
    }

    // http://api.gmall.com/cart/getCartList
    @GetMapping("getCartList")
    public RetVal getCartList(HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        String userTempId = AuthContextHolder.getUserTempId(request);
        List<CartInfo> cartInfoList = cartInfoService.getCartList(userId,userTempId);
        return RetVal.ok(cartInfoList);
    }

    // http://api.gmall.com/cart/checkCart/28/0
    @GetMapping("checkCart/{skuId}/{isCheckd}")
    public RetVal checkCart(@PathVariable Long skuId,@PathVariable Integer isCheckd,HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartInfoService.checkCart(skuId,isCheckd,userId);
        return RetVal.ok();
    }

    // http://api.gmall.com/cart/deleteCart/24
    @DeleteMapping("deleteCart/{skuId}")
    public RetVal deleteCart(@PathVariable Long skuId,HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)){
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartInfoService.deleteCart(skuId,userId);
        return RetVal.ok();
    }

    //5.查询选中的商品信息
    @GetMapping("getSelectedProduct/{userId}")
    public List<CartInfo> getSelectedProduct(@PathVariable Long userId){
        List<CartInfo> cartInfoList = cartInfoService.getSelectedProduct(userId);
        return cartInfoList;
    }

    //6.从数据库中查询出最新的购物车信息到redis中
    @GetMapping("queryFromDbToRedis/{userId}")
    public RetVal queryFromDbToRedis(@PathVariable String userId){
        List<CartInfo> cartInfoList = cartInfoService.queryFromDbToRedis(userId);
        return RetVal.ok(cartInfoList);
    }
}

