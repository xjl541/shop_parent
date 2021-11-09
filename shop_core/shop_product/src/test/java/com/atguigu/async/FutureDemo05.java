package com.atguigu.async;

import com.atguigu.exception.SleepUtils;
import executor.MyExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FutureDemo05 {
    public static void main(String[] args) {
        thenAcceptSerial();

        SleepUtils.sleep(5);
    }

    public static void thenAcceptSerial() {
        CompletableFuture<String> supplyAsync = CompletableFuture.supplyAsync(new Supplier<String>() {
            @Override
            public String get() {
                System.out.println(Thread.currentThread().getName() + "你好supplyAsync");
                SleepUtils.sleep(2);
                return "0521";
            }
        });

        supplyAsync.thenAcceptAsync(new Consumer<String>() {
            @Override
            public void accept(String acceptVal) {
                SleepUtils.sleep(2);
                System.out.println(Thread.currentThread().getName() +"第一个线程thenAccept拿到值" + acceptVal);

            }
        }, MyExecutor.getInstance());

        supplyAsync.thenAcceptAsync(new Consumer<String>() {
            @Override
            public void accept(String acceptVal) {
                SleepUtils.sleep(2);
                System.out.println(Thread.currentThread().getName() +"第二个线程thenAccept拿到值" + acceptVal);

            }
        },MyExecutor.getInstance());

    }


}
