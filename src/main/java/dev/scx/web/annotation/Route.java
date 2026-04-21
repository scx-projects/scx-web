package dev.scx.web.annotation;

import dev.scx.http.method.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// 路由注解
///
/// - 表示一个可注册的路由处理方法.
///
/// @author scx567888
/// @version 0.0.1
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {

    /// 模板 路径, 语法参考 [dev.scx.http.routing.path_matcher.TemplatePathMatcher].
    String[] value() default {};

    /// 允许的 HTTP 方法.
    /// 默认为空, 表示不额外限制请求方法.
    HttpMethod[] methods() default {};

    /// 优先级. 越小越靠前.
    int priority() default 0;

    /// 是否按绝对路径处理当前路由.
    ///
    /// 为 `true` 时, 当前 [Route] 的路径不会与类级 [Routes] 前缀进行拼接.
    boolean absolute() default false;

    /// 路由种类.
    ///
    /// 为 WEBSOCKET_UPGRADE 时, methods 只能为空或仅包含 GET.
    RouteKind kind() default RouteKind.REQUEST;

    enum RouteKind {
        REQUEST,
        WEBSOCKET_UPGRADE
    }

}
