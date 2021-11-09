package com.atguigu.async;

import com.atguigu.exception.SleepUtils;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class FutureDemo03 {
    public static void main(String[] args) {
        runAsync1();
        runAsync2();

        SleepUtils.sleep(3);
    }

    public static void runAsync1() {
        CompletableFuture.runAsync(() ->{
            System.out.println(Thread.currentThread().getName()+"你好runAsync1");
            SleepUtils.sleep(2);
        }).whenComplete(new BiConsumer<Void, Throwable>() {
            @Override
            public void accept(Void acceptVal, Throwable throwable) {
                System.out.println(Thread.currentThread().getName()+"获取上面执行之后的返回值1"+acceptVal);
            }
        });
    }

    public static void runAsync2() {
        CompletableFuture.runAsync(() ->{
            System.out.println(Thread.currentThread().getName()+"你好runAsync2");
            SleepUtils.sleep(2);
        }).whenComplete(new BiConsumer<Void, Throwable>() {
            @Override
            public void accept(Void acceptVal, Throwable throwable) {
                System.out.println(Thread.currentThread().getName()+"获取上面执行之后的返回值1"+acceptVal);
            }
        });
    }

}
