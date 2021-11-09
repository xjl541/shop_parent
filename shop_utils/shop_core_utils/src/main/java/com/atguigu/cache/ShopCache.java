package com.atguigu.cache;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
// 该注解的作用范围，能够放在哪里
@Target(ElementType.METHOD)
// 该注解的生命周期，在那里生效
@Retention(RetentionPolicy.RUNTIME)
public @interface ShopCache {
    String prefix() default "cache";
}
