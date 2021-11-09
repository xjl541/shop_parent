package com.atguigu.async;

import com.atguigu.exception.SleepUtils;

import java.util.concurrent.CompletableFuture;

public class FutureDemo01 {
    public static void main(String[] args) {
        supplyAsync();

        SleepUtils.sleep(3);
    }

    public static void runAsync() {
        CompletableFuture.runAsync(() ->{
            System.out.println("你好runAsync");
            SleepUtils.sleep(2);
            System.out.println("另外的线程正在等待");
        });
    }
    public static void supplyAsync() {
        CompletableFuture.supplyAsync(() ->{
            System.out.println(Thread.currentThread().getName()+"你好runAsync");
            SleepUtils.sleep(2);
            return "0521";
        });
    }

}
