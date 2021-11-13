package com.atguigu.service;

import com.atguigu.entity.UserAddress;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户地址表 服务类
 * </p>
 *
 * @author xiejl
 * @since 2021-11-08
 */
public interface UserAddressService extends IService<UserAddress> {

    List<UserAddress> getAddressListByUserId(String userId);
}
