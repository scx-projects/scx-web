package dev.scx.web.annotation;

import dev.scx.http.media.multi_part.MultiPartPart;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Part
///
/// 从 `multipart/*` 请求体中, 按名称选择一个或多个 part 并绑定到该参数.
///
/// 此注解仅适用于 `multipart/*` 请求体.
///
/// [Part] 绑定的是 "已解析并已缓存的 multipart part", 而非原始流式 part.
///
/// 匹配仅依据 part 名称.
///
/// 当前仅支持绑定到以下参数类型:
///
/// - [MultiPartPart].
/// - [MultiPartPart] 数组.
///
/// 若未显式指定 [Part#value], 默认使用参数名作为 part 名称.
///
/// @author scx567888
/// @version 0.0.1
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Part {

    /// part 名称.
    ///
    /// 为空时, 默认使用参数名作为 part 名称。
    String[] value() default {};

    /// 是否必填
    boolean required() default true;

}
