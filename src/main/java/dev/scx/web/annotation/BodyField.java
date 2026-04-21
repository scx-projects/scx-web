package dev.scx.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// BodyField
///
/// 从结构化 Body 的对象字段中, 按字段名提取一个值并绑定到该参数.
///
/// 仅当请求体可被解析为对象结构 (ObjectNode) 时适用.
///
/// 若未显式指定 [BodyField#value], 默认使用参数名作为字段名.
///
/// @author scx567888
/// @version 0.0.1
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface BodyField {

    /// 字段名称.
    ///
    /// 为空时, 默认使用参数名作为字段名称.
    String[] value() default {};

    /// 是否必填
    boolean required() default true;

}
