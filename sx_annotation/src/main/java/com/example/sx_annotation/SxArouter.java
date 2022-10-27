package com.example.sx_annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解
 *       是 Java 工程
 */
@Retention(RetentionPolicy.CLASS)//编译器框架
@Target(ElementType.TYPE)//作用在类上
public @interface SxArouter {
    String pageUrl();
    String groupName() default "";
    boolean asStarter() default false;
    boolean needLogin() default false;
}