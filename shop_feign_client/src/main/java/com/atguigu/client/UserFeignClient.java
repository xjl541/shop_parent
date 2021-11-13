package com.atguigu.client;

import com.atguigu.entity.UserAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(value = "shop-user")
public interface UserFeignClient {

    @GetMapping("/user/getAddressListByUserId/{userId}")
    public List<UserAddress> getAddressListByUserId(@PathVariable String userId);

}
