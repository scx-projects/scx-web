package dev.scx.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// PathCapture
///
/// 从路由路径匹配结果中, 读取一个命名捕获值并绑定到该参数.
///
/// 适用于由路由模板或路径匹配器产生的命名捕获, 例如 `/users/:id` 中的 `id`.
///
/// 若未显式指定 [PathCapture#value], 默认使用方法参数名作为捕获名称.
///
/// @author scx567888
/// @version 0.0.1
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathCapture {

    /// 捕获名称.
    ///
    /// 为空时, 默认使用参数名作为捕获名称.
    String[] value() default {};

}
