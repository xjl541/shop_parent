package com.atguigu.async;

import com.atguigu.exception.SleepUtils;

import java.util.concurrent.CompletableFuture;

public class FutureDemo04 {
    public static void main(String[] args) {
        thenAcceptSerial();

        SleepUtils.sleep(10);
    }

    public static void thenAcceptSerial() {
        CompletableFuture<String> supplyAsync = CompletableFuture.supplyAsync(() -> {
            System.out.println(Thread.currentThread().getName() + "supplyAsync");

            return "0521";
        });

        supplyAsync.thenAccept((String acceptVal) ->{
            SleepUtils.sleep(2);
            System.out.println(Thread.currentThread().getName() +"第一个线程thenAccept拿到值" + acceptVal);
        });

        supplyAsync.thenAccept((String acceptVal) ->{
            SleepUtils.sleep(2);
            System.out.println(Thread.currentThread().getName() +"第二个线程thenAccept拿到值" + acceptVal);
        });
    }

}
