package dev.scx.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// QueryParam
///
/// 从请求 URI 的 query 部分中, 按名称读取一个查询参数值并绑定到该参数.
///
/// 适用于单个 query parameter 的绑定, 例如 `/users?page=1` 中的 `page`.
///
/// 若未显式指定 [QueryParam#value], 默认使用参数名作为查询参数名.
///
/// @author scx567888
/// @version 0.0.1
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParam {

    /// 查询参数名称.
    ///
    /// 为空时, 默认使用参数名作为查询参数名称.
    String[] value() default {};

    /// 是否必填
    boolean required() default true;

}
