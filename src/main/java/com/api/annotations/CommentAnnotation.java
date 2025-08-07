package com.api.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/*
 * 作用：为枚举值和类提供描述性注释
 * 用途：主要用于文档生成、配置说明、元数据管理
 * 特点：运行时保留（@Retention(RetentionPolicy.RUNTIME)）
 * 使用场景：配置属性说明、API文档生成、测试报告
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface CommentAnnotation {

    String description();

}