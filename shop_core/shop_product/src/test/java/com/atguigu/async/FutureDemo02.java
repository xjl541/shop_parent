package com.atguigu.async;

import com.atguigu.exception.SleepUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class FutureDemo02 {
    public static void main(String[] args) {
        runAsync();
        
        SleepUtils.sleep(3);
    }

    public static void runAsync() {
        CompletableFuture.runAsync(() ->{
            System.out.println("你好runAsync");
            SleepUtils.sleep(2);
            System.out.println("另外的线程正在等待");
        }).whenComplete(new BiConsumer<Void, Throwable>() {
            @Override
            public void accept(Void acceptVal, Throwable throwable) {
                System.out.println("获取上面执行之后的返回值"+acceptVal);
                System.out.println("whenComplete接收到上面发生的异常");
            }
        });
    }

}
