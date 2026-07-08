package dev.scx.web.annotation;

import dev.scx.http.method.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/// 方法级路由注解.
///
/// 标记一个可注册的路由处理方法.
///
/// @author scx567888
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Route {

    /// 方法级路由模板.
    ///
    /// 默认情况下, 该模板会与类级 [Routes] 模板前缀按字面拼接.
    ///
    /// 语法参考 [dev.scx.http.routing.path_matcher.TemplatePathMatcher].
    ///
    /// 为空时, 表示当前方法不声明方法级路径模板.
    /// 如果类级 [Routes#value()] 也为空, 则最终不限制请求路径.
    /// 注意: 空值不表示根路径 `/`.
    String[] value() default {};

    /// 允许的 HTTP 方法.
    ///
    /// 默认为空, 表示不额外限制请求方法.
    ///
    /// 当 [#kind()] 为 [RouteKind#WEBSOCKET_UPGRADE] 时, 该配置会被忽略.
    HttpMethod[] methods() default {};

    /// 路由优先级.
    ///
    /// 数值越小, 优先级越高.
    int priority() default 0;

    /// 是否忽略类级 [Routes] 模板前缀.
    ///
    /// 为 `true` 时, 当前 [Route] 的模板不会与类级 [Routes] 前缀拼接.
    boolean absolute() default false;

    /// 路由种类.
    ///
    /// 为 [RouteKind#WEBSOCKET_UPGRADE] 时, 当前路由用于处理 WebSocket 升级请求,
    /// 此时 [#methods()] 不参与匹配语义.
    RouteKind kind() default RouteKind.REQUEST;

    enum RouteKind {
        REQUEST,
        WEBSOCKET_UPGRADE
    }

}
