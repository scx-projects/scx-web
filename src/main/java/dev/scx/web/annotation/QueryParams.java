package dev.scx.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// QueryParams
///
/// 从请求 URI 的 query parameters 中解析出对象结构化数据 (`ObjectNode`), 并根据参数类型进行绑定.
///
/// @author scx567888
/// @version 0.0.1
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParams {

}
