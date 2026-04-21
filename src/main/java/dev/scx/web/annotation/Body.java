package dev.scx.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// Body
///
/// 从整个请求体中解析出结构化数据, 并根据参数类型进行绑定.
///
/// 解析过程由请求体的 Content-Type 决定.
/// 当前仅支持以下可结构化解析的内容类型:
///
/// - `application/json` 解析为 `Node`.
/// - `application/xml` 解析为 `Node`.
/// - `application/x-www-form-urlencoded` 解析为 `ObjectNode`.
/// - `multipart/form-data` 解析为 `ObjectNode` (仅包含表单字段, 不包含文件 part).
///
/// 不支持 `text/plain`、`application/octet-stream` 以及其他原始或二进制内容类型.
/// 若需要访问原始请求体, 应直接通过 `ScxHttpServerRequest` 获取.
///
/// @author scx567888
/// @version 0.0.1
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Body {

}
