package com.atguigu.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.result.RetVal;
import com.atguigu.result.RetValCodeEnum;
import com.atguigu.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class AccessFilter implements GlobalFilter {

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Value("${filter.whiteList}")
    private String filterWhiteList;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        /**
         * @param exchange 服务网络交换器，存放着重要的请求-响应属性，请求实例和响应实例
         *                 不可变实例 如果想修改它 需要调用一个方法mutate方法生成一个新的实例
         * @param chain    网关过滤的链表 用于过滤器的链式调用 设计模式:责任链设计模式
         *                      https://www.cnblogs.com/ye-feng-yu/p/11107506.html
         *                 面试问题: 谈谈你对设计模式的理解(重点) 单例模式，代理，工厂，装饰模式 spring源码 多线程 JVM 分布式
         *                 (补数据结构 算法 网络编程 离散数学 线性代数 概率论)
         */
//        1.对于规定的内部接口 不允许外部调用
//        http://search.gmall.com/sku/getSkuInfo/24
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        if (antPathMatcher.match("/sku/**", path)) {
            return writeDataToBrowser(exchange, RetValCodeEnum.NO_PERMISSION);
        }
//        2.如果浏览器没有用户登录过的信息 需要提醒用户去登录
        String userId = getUserId(request);

        String userTempId =getUserTempId(request);

        if ("-1".equals(userId)){
            return writeDataToBrowser(exchange,RetValCodeEnum.NO_PERMISSION);
        }
//        3.对于某些资源(我的订单/我的购物车/我的xxx) 必须登录
//        http://search.gmall.com/order/24
        if (antPathMatcher.match("/order/**",path)){
            if (StringUtils.isEmpty(userId)){
                return writeDataToBrowser(exchange,RetValCodeEnum.NO_PERMISSION);
            }
        }
//        4.请求白名单 如果是请求白名单(addCart.html)  必须要用登录
//        http://cart.gmall.com/addCart.html?skuId=34&skuNum=1
        for (String filterWhite : filterWhiteList.split(",")) {
            if (!StringUtils.isEmpty(filterWhite)) {
                if (path.indexOf(filterWhite) != -1 && StringUtils.isEmpty(userId)) {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://passport.gmall.com/login.html?originalUrl=" + request.getURI());
                    return response.setComplete();
                }
            }
        }

        // 将request中的cookie信息取出来，赋值给自己的request
        if (!StringUtils.isEmpty(userId) || !StringUtils.isEmpty(userTempId)){
            if (!StringUtils.isEmpty(userId)){
                request.mutate().header("userId",userId).build();
            }
            if (!StringUtils.isEmpty(userTempId)){
                request.mutate().header("userTempId",userTempId).build();
            }
            //过滤器放开拦截 让下游继续执行(此时exchange里面的header做了修改)
            return chain.filter(exchange.mutate().request(request).build());
        }
        return chain.filter(exchange);
    }

    private String getUserTempId(ServerHttpRequest request) {
        List<String> headerValuelist = request.getHeaders().get("userTempId");
        String userTempId = null;
        if (!CollectionUtils.isEmpty(headerValuelist)) {
            userTempId = headerValuelist.get(0);
        } else {
            HttpCookie cookie = request.getCookies().getFirst("userTempId");
            if (cookie != null) {
                userTempId = cookie.getValue();
            }
        }
        return userTempId;
    }

    private String getUserId(ServerHttpRequest request) {
        List<String> headerValuelist = request.getHeaders().get("token");
        String token = null;
        if (!CollectionUtils.isEmpty(headerValuelist)) {
            token = headerValuelist.get(0);
        } else {
            HttpCookie cookie = request.getCookies().getFirst("token");
            if (cookie != null) {
                token = cookie.getValue();
            }
        }
        if (!StringUtils.isEmpty(token)) {
            String userKey = "user:login:" + token;

            JSONObject jsonObject = (JSONObject) redisTemplate.opsForValue().get(userKey);
//            JSONObject jsonObject = JSONObject.parseObject(userInfo);
            String loginIp = jsonObject.getString("loginIp");
            String ipAddress = IpUtil.getGatwayIpAddress(request);
            if (ipAddress.equals(loginIp)) {
                return jsonObject.getString("userId");
            } else {
                return "-1";
            }
        }
        return null;
    }

    private Mono<Void> writeDataToBrowser(ServerWebExchange exchange, RetValCodeEnum retValCodeEnum) {
        ServerHttpResponse response = exchange.getResponse();
        RetVal<Object> retVal = RetVal.build(null, retValCodeEnum);
        byte[] retBytes = JSONObject.toJSONString(retVal).getBytes(StandardCharsets.UTF_8);
        DataBuffer dataBuffer = response.bufferFactory().wrap(retBytes);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        return response.writeWith(Mono.just(dataBuffer));
    }
}
